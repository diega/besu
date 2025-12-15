/*
 * Copyright contributors to Besu.
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

import org.hyperledger.besu.ethereum.forkid.ForkBlockNumbersProvider;
import org.hyperledger.besu.ethereum.forkid.ForkBlockNumbersProviderRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForkBlockNumbersProviderRegistryImpl implements ForkBlockNumbersProviderRegistry {

  private static final Logger LOG =
      LoggerFactory.getLogger(ForkBlockNumbersProviderRegistryImpl.class);

  private ForkBlockNumbersProvider provider;

  @Override
  public void registerProvider(final ForkBlockNumbersProvider provider) {
    if (this.provider != null) {
      LOG.warn(
          "ForkBlockNumbersProvider already registered ({}), ignoring new registration ({})",
          this.provider.getClass().getName(),
          provider.getClass().getName());
      return;
    }
    this.provider = provider;
    LOG.debug("Registered ForkBlockNumbersProvider: {}", provider.getClass().getName());
  }

  @Override
  public ForkBlockNumbersProvider getProvider() {
    return provider;
  }
}
