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
package org.hyperledger.besu.config;

import java.util.Collection;
import java.util.List;

/**
 * EIP-2124 fork activations declared out of band of the genesis config keys: the block numbers and
 * timestamps of consensus changes peers must agree on, merged into the fork schedule the config
 * advertises. Block-number and timestamp activations are kept as two type-partitioned lists, as
 * EIP-2124 / EIP-6122 require; the two kinds are never compared against each other.
 *
 * @param blockNumbers the block-number fork activations
 * @param timestamps the timestamp fork activations
 */
public record ForkIdActivations(List<Long> blockNumbers, List<Long> timestamps) {

  private static final ForkIdActivations EMPTY = new ForkIdActivations(List.of(), List.of());

  /**
   * Canonical constructor; copies the lists defensively so the record stays immutable.
   *
   * @param blockNumbers the block-number fork activations
   * @param timestamps the timestamp fork activations
   */
  public ForkIdActivations(final List<Long> blockNumbers, final List<Long> timestamps) {
    this.blockNumbers = List.copyOf(blockNumbers);
    this.timestamps = List.copyOf(timestamps);
  }

  /**
   * Fork activations consisting solely of block numbers.
   *
   * @param blockNumbers the block-number fork activations
   * @return the fork activations
   */
  public static ForkIdActivations ofBlockNumbers(final Collection<Long> blockNumbers) {
    return new ForkIdActivations(List.copyOf(blockNumbers), List.of());
  }

  /**
   * No fork activations.
   *
   * @return the empty fork activations
   */
  public static ForkIdActivations empty() {
    return EMPTY;
  }
}
