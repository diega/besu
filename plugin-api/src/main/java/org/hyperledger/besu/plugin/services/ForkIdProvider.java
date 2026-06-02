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
import java.util.Optional;

/**
 * Allows plugins to provide the EIP-2124 fork schedule (fork block numbers and timestamps) for
 * chains that Besu does not configure natively.
 *
 * <p>When a registered provider applies to the active chain, the fork ID manager uses its schedule
 * instead of the built-in genesis config options, enabling non-mainnet chains (e.g. Ethereum
 * Classic) to advertise correct fork IDs to peers.
 *
 * <p>The chain ID is passed to {@link #forkScheduleFor(BigInteger)} because the provider is
 * registered during the plugin {@code register()} phase, before the active chain is known; the
 * chain ID becomes available when the controller builder invokes it.
 */
public interface ForkIdProvider extends BesuService {

  /**
   * Returns the fork schedule for the given chain, if this provider applies to it.
   *
   * <p>Returning {@link Optional#empty()} scopes the provider out of the chain, so the built-in
   * genesis config lists are used instead. This lets a provider stay globally registered while only
   * acting on the networks it recognises.
   *
   * @param chainId the chain ID of the active network
   * @return the fork schedule when this provider supports the chain, otherwise {@link
   *     Optional#empty()}
   */
  Optional<ForkSchedule> forkScheduleFor(BigInteger chainId);

  /**
   * The EIP-2124 fork schedule for a chain.
   *
   * @param blockNumbers sorted, distinct fork block numbers
   * @param timestamps sorted, distinct fork timestamps
   */
  record ForkSchedule(List<Long> blockNumbers, List<Long> timestamps) {}
}
