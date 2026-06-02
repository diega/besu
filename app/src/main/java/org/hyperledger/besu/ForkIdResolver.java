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
package org.hyperledger.besu;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.plugin.services.ForkIdProvider;
import org.hyperledger.besu.plugin.services.ForkIdProvider.ForkSchedule;

import java.util.List;
import java.util.Optional;

/**
 * Resolves the EIP-2124 fork lists for a chain: the registered {@link ForkIdProvider}'s schedule
 * when the chain advertises a chain ID that the provider supports, otherwise the genesis config's
 * lists.
 *
 * <p>Encapsulates the provider-vs-genesis selection shared by {@link RunnerBuilder} and {@link
 * org.hyperledger.besu.controller.BesuControllerBuilder}: the provider is consulted once, and the
 * genesis fallback stays lazy.
 */
public final class ForkIdResolver {

  private final Optional<ForkSchedule> schedule;
  private final GenesisConfigOptions genesisConfig;

  /**
   * @param provider the optional registered provider
   * @param genesisConfig the active network's genesis config, which is both the source of the chain
   *     ID and the fallback fork lists
   */
  public ForkIdResolver(
      final Optional<ForkIdProvider> provider, final GenesisConfigOptions genesisConfig) {
    this.genesisConfig = genesisConfig;
    // A provider can only apply when the chain advertises a chain ID it supports; without one (or
    // with no applicable provider) the genesis config lists are used.
    this.schedule =
        genesisConfig
            .getChainId()
            .flatMap(chainId -> provider.flatMap(candidate -> candidate.forkScheduleFor(chainId)));
  }

  /**
   * Returns the fork block numbers for EIP-2124 fork ID computation.
   *
   * @return the provider's list when it applies to the chain, otherwise the genesis config's list
   */
  public List<Long> blockNumberForks() {
    return schedule.map(ForkSchedule::blockNumbers).orElseGet(genesisConfig::getForkBlockNumbers);
  }

  /**
   * Returns the fork timestamps for EIP-2124 fork ID computation.
   *
   * @return the provider's list when it applies to the chain, otherwise the genesis config's list
   */
  public List<Long> timestampForks() {
    return schedule.map(ForkSchedule::timestamps).orElseGet(genesisConfig::getForkBlockTimestamps);
  }
}
