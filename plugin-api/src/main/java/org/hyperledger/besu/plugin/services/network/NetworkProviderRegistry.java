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

import org.hyperledger.besu.plugin.services.BesuService;

import java.util.Optional;
import java.util.Set;

/**
 * Service for registering custom network providers. Plugins can use this service to register {@link
 * NetworkProvider} implementations that define custom networks.
 */
public interface NetworkProviderRegistry extends BesuService {

  /**
   * Registers a network provider.
   *
   * @param provider the network provider to register
   */
  void registerProvider(NetworkProvider provider);

  /**
   * Gets a provider that supports the given network name.
   *
   * @param networkName the network name to look up
   * @return the provider if found, empty otherwise
   */
  Optional<NetworkProvider> getProviderFor(String networkName);

  /**
   * Gets all available network names from all registered providers.
   *
   * @return set of all available network names
   */
  Set<String> getAvailableNetworks();
}
