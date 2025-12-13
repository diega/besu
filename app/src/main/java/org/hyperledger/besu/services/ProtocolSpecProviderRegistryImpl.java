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

import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecProvider;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecProviderRegistry;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolSpecProviderRegistryImpl implements ProtocolSpecProviderRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(ProtocolSpecProviderRegistryImpl.class);

  private final List<ProtocolSpecProvider> providers = new ArrayList<>();

  @Override
  public void registerProvider(final ProtocolSpecProvider provider) {
    providers.add(provider);
    LOG.debug("Registered ProtocolSpecProvider: {}", provider.getClass().getName());
  }

  @Override
  public List<ProtocolSpecProvider> getProviders() {
    return providers;
  }
}
