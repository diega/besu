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
package org.hyperledger.besu.ethereum.mainnet.plan;

import org.hyperledger.besu.config.GenesisConfigOptions;

import java.util.List;

/**
 * Contributes additional forks (for example a network plugin's network-specific hard-forks) to the
 * {@link ProtocolSchedulePlan}.
 *
 * <p>A contributor is registered early (it computes nothing at registration) and is invoked exactly
 * once with the active genesis configuration when the plan is frozen, before the controller is
 * built. Because the entries are config-parametric, a contributor returns nothing for chains it
 * does not target.
 */
@FunctionalInterface
public interface ProtocolScheduleContributor {

  /**
   * Returns the fork entries this contributor adds for the given chain, in declared order (the
   * order is significant when several modifiers from this contributor share an activation).
   *
   * @param config the active genesis configuration
   * @return the contributed fork entries (empty for a chain this contributor does not target)
   */
  List<ForkEntry> contribute(GenesisConfigOptions config);
}
