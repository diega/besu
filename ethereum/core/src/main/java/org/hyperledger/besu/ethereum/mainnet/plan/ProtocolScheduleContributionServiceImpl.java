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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default {@link ProtocolScheduleContributionService}. Accumulates contributors registered by
 * plugins and freezes the {@link ProtocolSchedulePlan} once, invoking every contributor exactly
 * once with the active genesis configuration. After the freeze the service is read-only:
 * registering another contributor fails fast rather than being silently dropped.
 *
 * <p>{@link #freeze(GenesisConfigOptions)} is internal to Besu — it is called once, after the
 * genesis configuration is parsed and before the controller is built. Plugins only see {@link
 * #registerContributor} through the service interface.
 */
public class ProtocolScheduleContributionServiceImpl
    implements ProtocolScheduleContributionService {

  private final List<ProtocolScheduleContributor> contributors = new ArrayList<>();
  private volatile ProtocolSchedulePlan frozenPlan;

  @Override
  public synchronized void registerContributor(final ProtocolScheduleContributor contributor) {
    if (frozenPlan != null) {
      throw new IllegalStateException(
          "Cannot register a ProtocolScheduleContributor after the ProtocolSchedulePlan is frozen");
    }
    contributors.add(contributor);
  }

  /**
   * Builds and freezes the plan on the first call, invoking each registered contributor exactly
   * once, and returns the same frozen plan on every later call. Schedule modifiers contributed by
   * two <em>different</em> contributors at the same activation are rejected — there is no implicit
   * cross-contributor precedence — while a single contributor may compose several modifiers at one
   * activation, in declared order.
   *
   * @param config the active genesis configuration
   * @return the frozen protocol schedule plan
   */
  public synchronized ProtocolSchedulePlan freeze(final GenesisConfigOptions config) {
    if (frozenPlan == null) {
      final List<ForkEntry> entries = new ArrayList<>();
      // Activation -> index of the contributor that owns a schedule modifier there.
      final Map<Activation, Integer> modifierOwner = new HashMap<>();
      for (int i = 0; i < contributors.size(); i++) {
        final List<ForkEntry> contributed = contributors.get(i).contribute(config);
        for (final ForkEntry entry : contributed) {
          if (entry.effect() instanceof ScheduleEffect.Modifier) {
            final Integer owner = modifierOwner.putIfAbsent(entry.activation(), i);
            if (owner != null && owner != i) {
              throw new IllegalStateException(
                  "Multiple contributors contributed a schedule modifier at activation "
                      + entry.activation()
                      + "; cross-contributor composition at the same activation is not supported");
            }
          }
        }
        entries.addAll(contributed);
      }
      frozenPlan = ProtocolSchedulePlan.create(config, entries);
    }
    return frozenPlan;
  }
}
