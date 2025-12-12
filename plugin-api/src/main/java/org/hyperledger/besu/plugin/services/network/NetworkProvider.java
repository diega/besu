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
package org.hyperledger.besu.plugin.services.network;

import java.math.BigInteger;
import java.net.URL;
import java.util.List;

/**
 * Provider interface for custom network definitions. Plugins can implement this interface to
 * register networks that can be accessed via the --network CLI option.
 */
public interface NetworkProvider {

  /**
   * Returns the names of networks this provider supports.
   *
   * @return list of network names (e.g., "classic", "mordor")
   */
  List<String> getSupportedNetworks();

  /**
   * Returns the genesis configuration URL for the given network.
   *
   * @param networkName the network name
   * @return URL to the genesis configuration file
   */
  URL getGenesisConfig(String networkName);

  /**
   * Returns the network ID for the given network.
   *
   * @param networkName the network name
   * @return the network ID
   */
  BigInteger getNetworkId(String networkName);

  /**
   * Returns whether SNAP sync is supported for this network.
   *
   * @param networkName the network name
   * @return true if SNAP sync is supported
   */
  boolean canSnapSync(String networkName);

  /**
   * Returns optional boot nodes for discovery.
   *
   * @param networkName the network name
   * @return list of enode URLs, or empty list if none
   */
  default List<String> getBootNodes(final String networkName) {
    return List.of();
  }

  /**
   * Returns optional DNS discovery URL.
   *
   * @param networkName the network name
   * @return DNS discovery URL, or null if not available
   */
  default String getDnsDiscoveryUrl(final String networkName) {
    return null;
  }
}
