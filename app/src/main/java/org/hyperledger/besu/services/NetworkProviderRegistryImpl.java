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
package org.hyperledger.besu.services;

import org.hyperledger.besu.plugin.services.network.NetworkProvider;
import org.hyperledger.besu.plugin.services.network.NetworkProviderRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of NetworkProviderRegistry that manages network provider registrations. */
public class NetworkProviderRegistryImpl implements NetworkProviderRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(NetworkProviderRegistryImpl.class);

  private final List<NetworkProvider> providers = new ArrayList<>();

  @Override
  public void registerProvider(final NetworkProvider provider) {
    providers.add(provider);
    LOG.debug("Registered NetworkProvider for networks: {}", provider.getSupportedNetworks());
  }

  @Override
  public Optional<NetworkProvider> getProviderFor(final String networkName) {
    final String normalizedName = networkName.toLowerCase(Locale.ROOT);
    return providers.stream()
        .filter(
            p ->
                p.getSupportedNetworks().stream()
                    .anyMatch(n -> n.toLowerCase(Locale.ROOT).equals(normalizedName)))
        .findFirst();
  }

  @Override
  public Set<String> getAvailableNetworks() {
    final Set<String> networks = new HashSet<>();
    for (final NetworkProvider provider : providers) {
      networks.addAll(provider.getSupportedNetworks());
    }
    return networks;
  }
}
