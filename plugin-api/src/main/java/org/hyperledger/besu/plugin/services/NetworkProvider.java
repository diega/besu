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

import org.hyperledger.besu.plugin.Unstable;

import java.math.BigInteger;
import java.net.URL;
import java.util.Set;

/**
 * A service that allows plugins to provide network definitions for custom networks.
 *
 * <p>When the core network resolution mechanism (e.g., {@code --network=<name>}) does not recognize
 * a network name, it will consult registered {@code NetworkProvider}s as a fallback. This allows
 * plugins to add support for additional networks without modifying the core enum.
 */
@Unstable
public interface NetworkProvider extends BesuService {

  /**
   * Returns the set of network names this provider supports. Names are case-insensitive.
   *
   * @return the set of supported network names
   */
  Set<String> supportedNetworks();

  /**
   * Returns the genesis configuration resource URL for the given network name.
   *
   * @param networkName the network name (case-insensitive, must be in {@link #supportedNetworks()})
   * @return the URL to the genesis JSON configuration
   */
  URL genesisConfig(String networkName);

  /**
   * Returns the network ID for the given network name.
   *
   * @param networkName the network name (case-insensitive, must be in {@link #supportedNetworks()})
   * @return the network ID
   */
  BigInteger networkId(String networkName);

  /**
   * Returns the chain ID for the given network name.
   *
   * @param networkName the network name (case-insensitive, must be in {@link #supportedNetworks()})
   * @return the chain ID
   */
  BigInteger chainId(String networkName);

  /**
   * Returns whether the network supports snap sync.
   *
   * @param networkName the network name
   * @return {@code true} if snap sync is supported
   */
  default boolean canSnapSync(final String networkName) {
    return false;
  }

  /**
   * Returns the target gas limit for the network.
   *
   * @param networkName the network name
   * @return the target gas limit
   */
  default long targetGasLimit(final String networkName) {
    return 60_000_000L;
  }
}
