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
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecAdapters;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
   * Builds the plan from the genesis configuration and contributed fork entries. The {@link
   * ForkIdBoundary#INCLUDED} entries are folded into the fork-ID activation lists (partitioned by
   * activation kind, never across kinds, normalised distinct-and-sorted within each kind); all
   * entries are retained for protocol-schedule construction.
   *
   * @param config the genesis configuration options
   * @param contributedEntries the contributed fork entries, in contribution order
   * @return the protocol schedule plan
   */
  public static ProtocolSchedulePlan create(
      final GenesisConfigOptions config, final List<ForkEntry> contributedEntries) {
    final List<Long> blockNumbers = new ArrayList<>(config.getForkBlockNumbers());
    final List<Long> timestamps = new ArrayList<>(config.getForkBlockTimestamps());
    for (final ForkEntry entry : contributedEntries) {
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
