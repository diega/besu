/*
 * Copyright contributors to Hyperledger Besu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.mainnet.plan;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecAdapters;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.function.Function;

/**
 * The single source for a chain's fork activations, read by every EIP-2124 fork-ID consumer so the
 * advertised fork ID is computed in one place instead of each consumer enumerating the genesis
 * config independently, and carrying the contributed schedule effects so a fork declared once
 * reaches the fork ID and the protocol schedule alike.
 *
 * <p>Block-number and timestamp activations are kept as two separate, type-partitioned lists (each
 * sorted ascending, as EIP-2124/EIP-6122 require) and are never merged or compared against each
 * other — exactly the two lists {@link org.hyperledger.besu.ethereum.forkid.ForkIdManager} expects.
 */
public final class ProtocolSchedulePlan {

  private final List<Long> forkIdBlockNumbers;
  private final List<Long> forkIdTimestamps;
  private final List<ForkEntry> contributedEntries;

  private ProtocolSchedulePlan(
      final List<Long> forkIdBlockNumbers,
      final List<Long> forkIdTimestamps,
      final List<ForkEntry> contributedEntries) {
    this.forkIdBlockNumbers = List.copyOf(forkIdBlockNumbers);
    this.forkIdTimestamps = List.copyOf(forkIdTimestamps);
    this.contributedEntries = List.copyOf(contributedEntries);
  }

  /**
   * Builds the plan from the genesis configuration alone (no contributions): byte-for-byte the same
   * fork-ID activations as {@link GenesisConfigOptions#getForkBlockNumbers()} / {@link
   * GenesisConfigOptions#getForkBlockTimestamps()}.
   *
   * @param config the genesis configuration options
   * @return the protocol schedule plan
   */
  public static ProtocolSchedulePlan fromConfig(final GenesisConfigOptions config) {
    return create(config, List.of());
  }

  /**
   * Builds the plan from the genesis configuration and contributed fork entries. The fork-ID
   * activation lists are derived from the union of the {@link ForkIdBoundary#INCLUDED} entries —
   * the core forks the config activates, the DAO fork, and the contributions — partitioned by
   * activation kind, never across kinds, normalised distinct-and-sorted within each kind. All
   * contributed entries are retained for protocol-schedule construction.
   *
   * @param config the genesis configuration options
   * @param contributedEntries the contributed fork entries, in contribution order
   * @return the protocol schedule plan
   */
  public static ProtocolSchedulePlan create(
      final GenesisConfigOptions config, final List<ForkEntry> contributedEntries) {
    final List<ForkEntry> coreEntries = coreForkEntries(config);
    validateForkOrder(coreEntries);
    final List<ForkEntry> entries = new ArrayList<>(coreEntries);
    config.getDaoForkBlock().ifPresent(daoForkBlock -> entries.add(daoForkEntry(daoForkBlock)));
    entries.addAll(contributedEntries);

    final List<Long> blockNumbers = new ArrayList<>();
    final List<Long> timestamps = new ArrayList<>();
    for (final ForkEntry entry : entries) {
      if (entry.forkIdBoundary() == ForkIdBoundary.INCLUDED) {
        switch (entry.activation()) {
          case Activation.BlockNumber blockNumber -> blockNumbers.add(blockNumber.value());
          case Activation.Timestamp timestamp -> timestamps.add(timestamp.value());
        }
      }
    }
    return new ProtocolSchedulePlan(
        blockNumbers.stream().distinct().sorted().toList(),
        timestamps.stream().distinct().sorted().toList(),
        contributedEntries);
  }

  /**
   * The core fork entries the config activates, enumerated in the canonical milestone order (block
   * numbers first, then timestamps). Frontier at genesis is a schedule milestone but not an
   * EIP-2124 boundary; every other core fork is. The DAO fork is deliberately not part of this
   * ordered enumeration — it is config-activated outside the ordered fork sequence (see {@link
   * #daoForkEntry}).
   */
  private static List<ForkEntry> coreForkEntries(final GenesisConfigOptions config) {
    final List<ForkEntry> entries = new ArrayList<>();
    blockEntry(entries, MainnetHardforkId.FRONTIER, OptionalLong.of(0), ForkIdBoundary.EXCLUDED);
    blockEntry(entries, MainnetHardforkId.HOMESTEAD, config.getHomesteadBlockNumber());
    blockEntry(
        entries, MainnetHardforkId.TANGERINE_WHISTLE, config.getTangerineWhistleBlockNumber());
    blockEntry(entries, MainnetHardforkId.SPURIOUS_DRAGON, config.getSpuriousDragonBlockNumber());
    blockEntry(entries, MainnetHardforkId.BYZANTIUM, config.getByzantiumBlockNumber());
    blockEntry(entries, MainnetHardforkId.CONSTANTINOPLE, config.getConstantinopleBlockNumber());
    blockEntry(entries, MainnetHardforkId.PETERSBURG, config.getPetersburgBlockNumber());
    blockEntry(entries, MainnetHardforkId.ISTANBUL, config.getIstanbulBlockNumber());
    blockEntry(entries, MainnetHardforkId.MUIR_GLACIER, config.getMuirGlacierBlockNumber());
    blockEntry(entries, MainnetHardforkId.BERLIN, config.getBerlinBlockNumber());
    blockEntry(entries, MainnetHardforkId.LONDON, config.getLondonBlockNumber());
    blockEntry(entries, MainnetHardforkId.ARROW_GLACIER, config.getArrowGlacierBlockNumber());
    blockEntry(entries, MainnetHardforkId.GRAY_GLACIER, config.getGrayGlacierBlockNumber());
    blockEntry(entries, MainnetHardforkId.PARIS, config.getMergeNetSplitBlockNumber());
    timestampEntry(entries, MainnetHardforkId.SHANGHAI, config.getShanghaiTime());
    timestampEntry(entries, MainnetHardforkId.CANCUN, config.getCancunTime());
    timestampEntry(entries, MainnetHardforkId.PRAGUE, config.getPragueTime());
    timestampEntry(entries, MainnetHardforkId.OSAKA, config.getOsakaTime());
    timestampEntry(entries, MainnetHardforkId.BPO1, config.getBpo1Time());
    timestampEntry(entries, MainnetHardforkId.BPO2, config.getBpo2Time());
    timestampEntry(entries, MainnetHardforkId.BPO3, config.getBpo3Time());
    timestampEntry(entries, MainnetHardforkId.BPO4, config.getBpo4Time());
    timestampEntry(entries, MainnetHardforkId.BPO5, config.getBpo5Time());
    timestampEntry(entries, MainnetHardforkId.AMSTERDAM, config.getAmsterdamTime());
    timestampEntry(entries, MainnetHardforkId.FUTURE_EIPS, config.getFutureEipsTime());
    timestampEntry(entries, MainnetHardforkId.EXPERIMENTAL_EIPS, config.getExperimentalEipsTime());
    return entries;
  }

  /**
   * Validates that the core fork activations do not regress along the canonical enumeration — the
   * same single running watermark the legacy schedule construction applies, carried from the block
   * forks into the timestamp forks. The DAO fork and contributed entries are not part of the
   * ordered sequence and are not validated, matching the legacy behaviour.
   */
  private static void validateForkOrder(final List<ForkEntry> coreEntries) {
    long lastForkBlock = 0;
    for (final ForkEntry entry : coreEntries) {
      final long activation = entry.activation().value();
      if (lastForkBlock > activation) {
        throw new RuntimeException(
            String.format(
                "Genesis Config Error: '%s' is scheduled for milestone %d but it must be on or after milestone %d.",
                entry.id(), activation, lastForkBlock));
      }
      lastForkBlock = activation;
    }
  }

  /**
   * The DAO fork as a plan entry: an EIP-2124 boundary whose schedule effect is Homestead plus the
   * block-number-guarded DAO behaviours (see {@code MainnetProtocolSpecs.daoForkDefinition}). The
   * guarded behaviours are not derivable from a hardfork id, so the schedule resolves this entry
   * specially; the built-in reference records the rule set it overlays.
   */
  private static ForkEntry daoForkEntry(final long daoForkBlock) {
    return new ForkEntry(
        "DAOForkBlock",
        new Activation.BlockNumber(daoForkBlock),
        new ScheduleEffect.BuiltInHardfork(MainnetHardforkId.HOMESTEAD),
        ForkIdBoundary.INCLUDED);
  }

  private static void blockEntry(
      final List<ForkEntry> entries,
      final MainnetHardforkId hardforkId,
      final OptionalLong activation) {
    blockEntry(entries, hardforkId, activation, ForkIdBoundary.INCLUDED);
  }

  private static void blockEntry(
      final List<ForkEntry> entries,
      final MainnetHardforkId hardforkId,
      final OptionalLong activation,
      final ForkIdBoundary boundary) {
    activation.ifPresent(
        value ->
            entries.add(
                new ForkEntry(
                    hardforkId.name(),
                    new Activation.BlockNumber(value),
                    new ScheduleEffect.BuiltInHardfork(hardforkId),
                    boundary)));
  }

  private static void timestampEntry(
      final List<ForkEntry> entries,
      final MainnetHardforkId hardforkId,
      final OptionalLong activation) {
    activation.ifPresent(
        value ->
            entries.add(
                new ForkEntry(
                    hardforkId.name(),
                    new Activation.Timestamp(value),
                    new ScheduleEffect.BuiltInHardfork(hardforkId),
                    ForkIdBoundary.INCLUDED)));
  }

  /**
   * The EIP-2124 block-number fork activations, sorted ascending.
   *
   * @return the block-number fork activations
   */
  public List<Long> forkIdBlockNumbers() {
    return forkIdBlockNumbers;
  }

  /**
   * The EIP-6122 timestamp fork activations, sorted ascending.
   *
   * @return the timestamp fork activations
   */
  public List<Long> forkIdTimestamps() {
    return forkIdTimestamps;
  }

  /**
   * The contributed fork entries, retained (in contribution order) for protocol-schedule
   * construction.
   *
   * @return the contributed fork entries
   */
  public List<ForkEntry> contributedEntries() {
    return contributedEntries;
  }

  /**
   * Projects the contributed {@link ScheduleEffect.Modifier} entries into a {@link
   * ProtocolSpecAdapters} for the schedule builders to fold in (via {@link
   * ProtocolSpecAdapters#composedWith}). Modifiers sharing an activation are composed in
   * contribution order rather than one replacing the other, so nothing is dropped.
   *
   * @return the contributed spec adapters
   */
  public ProtocolSpecAdapters scheduleSpecAdapters() {
    final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> modifiers = new HashMap<>();
    for (final ForkEntry entry : contributedEntries) {
      if (entry.effect() instanceof ScheduleEffect.Modifier modifier) {
        modifiers.merge(
            entry.activation().value(), modifier.modifier(), (base, next) -> base.andThen(next));
      }
    }
    return new ProtocolSpecAdapters(modifiers);
  }
}
