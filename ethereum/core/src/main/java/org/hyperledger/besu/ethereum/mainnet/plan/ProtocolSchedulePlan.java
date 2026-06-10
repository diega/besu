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

import java.util.List;

/**
 * The single source for a chain's fork activations, read by every EIP-2124 fork-ID consumer so the
 * advertised fork ID is computed in one place instead of each consumer enumerating the genesis
 * config independently.
 *
 * <p>Block-number and timestamp activations are kept as two separate, type-partitioned lists (each
 * sorted ascending, as EIP-2124/EIP-6122 require) and are never merged or compared against each
 * other — exactly the two lists {@link org.hyperledger.besu.ethereum.forkid.ForkIdManager} expects.
 */
public final class ProtocolSchedulePlan {

  private final List<Long> forkIdBlockNumbers;
  private final List<Long> forkIdTimestamps;

  private ProtocolSchedulePlan(
      final List<Long> forkIdBlockNumbers, final List<Long> forkIdTimestamps) {
    this.forkIdBlockNumbers = List.copyOf(forkIdBlockNumbers);
    this.forkIdTimestamps = List.copyOf(forkIdTimestamps);
  }

  /**
   * Builds the plan from the genesis configuration: byte-for-byte the same fork-ID activations as
   * {@link GenesisConfigOptions#getForkBlockNumbers()} / {@link
   * GenesisConfigOptions#getForkBlockTimestamps()}.
   *
   * @param config the genesis configuration options
   * @return the protocol schedule plan
   */
  public static ProtocolSchedulePlan fromConfig(final GenesisConfigOptions config) {
    return new ProtocolSchedulePlan(config.getForkBlockNumbers(), config.getForkBlockTimestamps());
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
}
