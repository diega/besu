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
package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.config.GenesisConfigOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * The single source of truth for a chain's fork activations, read by every EIP-2124 fork-ID
 * consumer so the advertised fork ID is computed in one place.
 *
 * <p>This is introduced incrementally. Its fork-ID activations are the genesis-derived forks
 * unioned with the {@link ForkIdBoundary#INCLUDED} activations contributed by plugins (with no
 * contributions it is byte-for-byte equivalent to {@link
 * GenesisConfigOptions#getForkBlockNumbers()} / {@link
 * GenesisConfigOptions#getForkBlockTimestamps()}). Later phases make the plan the source of the
 * protocol schedule as well; the retained {@link #contributedEntries()} carry the spec effects for
 * that.
 *
 * <p>Block-number and timestamp activations are kept as two separate, type-partitioned lists (each
 * sorted ascending) and are never merged or compared against each other, matching what {@link
 * org.hyperledger.besu.ethereum.forkid.ForkIdManager} expects.
 */
public class ProtocolSchedulePlan {

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
   * Builds the plan from the genesis configuration alone (no plugin contributions).
   *
   * @param config the genesis configuration options
   * @return the protocol schedule plan
   */
  public static ProtocolSchedulePlan fromConfig(final GenesisConfigOptions config) {
    return create(config, List.of());
  }

  /**
   * Builds the plan from the genesis configuration and plugin-contributed fork entries. The {@link
   * ForkIdBoundary#INCLUDED} entries are folded into the fork-ID activation lists (partitioned by
   * activation kind, never across kinds); all entries are retained for protocol-schedule
   * construction.
   *
   * @param config the genesis configuration options
   * @param contributedEntries the plugin-contributed fork entries, in contribution order
   * @return the protocol schedule plan
   */
  public static ProtocolSchedulePlan create(
      final GenesisConfigOptions config, final List<ForkEntry> contributedEntries) {
    final List<Long> blockForks = new ArrayList<>(config.getForkBlockNumbers());
    final List<Long> timestampForks = new ArrayList<>(config.getForkBlockTimestamps());
    for (final ForkEntry entry : contributedEntries) {
      if (entry.forkIdBoundary() == ForkIdBoundary.INCLUDED) {
        switch (entry.activation()) {
          case Activation.BlockNumber blockNumber -> blockForks.add(blockNumber.value());
          case Activation.Timestamp timestamp -> timestampForks.add(timestamp.value());
        }
      }
    }
    return new ProtocolSchedulePlan(
        blockForks.stream().distinct().sorted().toList(),
        timestampForks.stream().distinct().sorted().toList(),
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
   * The EIP-2124 timestamp fork activations, sorted ascending.
   *
   * @return the timestamp fork activations
   */
  public List<Long> forkIdTimestamps() {
    return forkIdTimestamps;
  }

  /**
   * The plugin-contributed fork entries, retained (in contribution order) for protocol-schedule
   * construction.
   *
   * @return the contributed fork entries
   */
  public List<ForkEntry> contributedEntries() {
    return contributedEntries;
  }
}
