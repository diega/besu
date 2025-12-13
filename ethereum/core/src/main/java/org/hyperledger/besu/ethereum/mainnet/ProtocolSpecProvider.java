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

import java.math.BigInteger;
import java.util.Optional;

/**
 * Extension point for plugins to provide custom protocol specifications. Plugins can register
 * custom hardfork milestones for networks like Ethereum Classic.
 *
 * <p>Implementations are discovered via ServiceLoader and called during protocol schedule
 * construction.
 */
public interface ProtocolSpecProvider {

  /**
   * Returns true if this provider supports the given chain ID. Implementations should check if the
   * chain ID matches their target network (e.g., 61 for Ethereum Classic).
   *
   * @param chainId the chain ID from genesis config, or empty if not set
   * @return true if this provider should contribute milestones for this chain
   */
  boolean supports(Optional<BigInteger> chainId);

  /**
   * Registers custom milestone definitions to the registry. Called during protocol schedule
   * construction for providers that support the current chain.
   *
   * <p>Implementations should use registry.addBlockNumberMilestone() and
   * registry.addTimestampMilestone() to add their hardforks.
   *
   * @param registry the milestone registry to add milestones to
   */
  void registerMilestones(MilestoneRegistry registry);
}
