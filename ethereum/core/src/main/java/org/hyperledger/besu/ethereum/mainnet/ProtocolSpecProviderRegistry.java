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

import org.hyperledger.besu.plugin.services.BesuService;

import java.util.List;

/**
 * Registry service for custom ProtocolSpecProvider registration.
 *
 * <p>Plugins use this service to register {@link ProtocolSpecProvider} implementations that provide
 * custom protocol specifications (hardforks). Multiple providers can be registered, each
 * contributing their own milestones (e.g., Classic hardforks, experimental EIPs).
 */
public interface ProtocolSpecProviderRegistry extends BesuService {

  /**
   * Registers a protocol spec provider.
   *
   * @param provider the provider to register
   */
  void registerProvider(ProtocolSpecProvider provider);

  /**
   * Gets all registered protocol spec providers.
   *
   * @return list of registered providers (empty if none registered)
   */
  List<ProtocolSpecProvider> getProviders();
}
