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
package org.hyperledger.besu.plugin.classic;

import org.hyperledger.besu.plugin.services.network.NetworkProvider;

import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Network provider for Ethereum Classic networks (Classic mainnet and Mordor testnet). */
public class ClassicNetworkProvider implements NetworkProvider {

  private static final Map<String, NetworkConfig> NETWORKS =
      Map.of(
          "classic",
          new NetworkConfig(
              ClassicNetworkProvider.class.getResource("/classic.json"), BigInteger.valueOf(1)),
          "mordor",
          new NetworkConfig(
              ClassicNetworkProvider.class.getResource("/mordor.json"), BigInteger.valueOf(7)));

  @Override
  public List<String> getSupportedNetworks() {
    return List.copyOf(NETWORKS.keySet());
  }

  @Override
  public URL getGenesisConfig(final String networkName) {
    return NETWORKS.get(networkName.toLowerCase(Locale.ROOT)).genesisUrl();
  }

  @Override
  public BigInteger getNetworkId(final String networkName) {
    return NETWORKS.get(networkName.toLowerCase(Locale.ROOT)).networkId();
  }

  @Override
  public boolean canSnapSync(final String networkName) {
    // Classic networks support snap sync
    return true;
  }

  private record NetworkConfig(URL genesisUrl, BigInteger networkId) {}
}
