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
package org.hyperledger.besu.plugin.services;

import java.math.BigInteger;
import java.util.List;

/**
 * Allows plugins to provide fork block and timestamp lists for EIP-2124 fork ID computation.
 *
 * <p>When registered, the fork ID manager will use these lists instead of the built-in genesis
 * config options, enabling non-mainnet chains (e.g. Ethereum Classic) to advertise correct fork IDs
 * to peers.
 *
 * <p>The chain ID is passed to each method because the provider is registered during the plugin
 * {@code register()} phase, before the active chain is known. The chain ID becomes available when
 * the controller builder invokes these methods.
 */
public interface ForkIdProvider extends BesuService {

  /**
   * Returns whether this provider should be applied for the given chain ID.
   *
   * <p>Implementations can scope themselves to specific networks while still being globally
   * registered during plugin startup.
   *
   * @param chainId the chain ID of the active network
   * @return {@code true} if this provider applies to the chain, {@code false} otherwise
   */
  boolean supportsChainId(BigInteger chainId);

  /**
   * Returns the list of fork block numbers for EIP-2124 fork ID computation.
   *
   * @param chainId the chain ID of the active network
   * @return sorted, distinct list of fork block numbers
   */
  List<Long> getForkBlockNumbers(BigInteger chainId);

  /**
   * Returns the list of fork timestamps for EIP-2124 fork ID computation.
   *
   * @param chainId the chain ID of the active network
   * @return sorted, distinct list of fork timestamps
   */
  List<Long> getForkTimestamps(BigInteger chainId);
}
