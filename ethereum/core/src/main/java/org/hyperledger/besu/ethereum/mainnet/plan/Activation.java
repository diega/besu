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

/**
 * The point at which a fork activates: either a block number or a unix timestamp. Typing the
 * activation removes the block-versus-timestamp ambiguity of a bare {@code long} and lets fork-ID
 * activations be partitioned by kind (as EIP-2124 / EIP-6122 require) without ever comparing a
 * block number against a timestamp.
 */
public sealed interface Activation permits Activation.BlockNumber, Activation.Timestamp {

  /**
   * The raw activation value (a block number or a timestamp, depending on the kind).
   *
   * @return the activation value
   */
  long value();

  /**
   * An activation expressed as a block number.
   *
   * @param value the block number
   */
  record BlockNumber(long value) implements Activation {}

  /**
   * An activation expressed as a unix timestamp (seconds).
   *
   * @param value the timestamp
   */
  record Timestamp(long value) implements Activation {}
}
