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

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.plugin.ServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default {@link ProtocolScheduleContributionService}. Accumulates contributors registered by
 * plugins and freezes the {@link ProtocolSchedulePlan} once — the first time {@link
 * #freeze(GenesisConfigOptions)} is called — invoking every contributor exactly once. After the
 * first freeze it is read-only and {@link #registerContributor} fails fast.
 *
 * <p>{@code freeze} is internal to Besu (called once when the controller is built); plugins only
 * see {@link #registerContributor} through the service interface.
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
   * once, and returns the same frozen plan on every later call.
   *
   * @param config the active genesis configuration
   * @return the frozen protocol schedule plan
   */
  public synchronized ProtocolSchedulePlan freeze(final GenesisConfigOptions config) {
    if (frozenPlan == null) {
      final List<ForkEntry> entries = new ArrayList<>();
      for (final ProtocolScheduleContributor contributor : contributors) {
        entries.addAll(contributor.contribute(config));
      }
      frozenPlan = ProtocolSchedulePlan.create(config, entries);
    }
    return frozenPlan;
  }

  /**
   * Resolves the frozen plan from the service manager, freezing it on first use; if no contribution
   * service is registered the plan is built from the genesis configuration alone. This is the entry
   * point fork-ID and schedule consumers use so they all read one plan.
   *
   * @param serviceManager the service manager to resolve the contribution service from, if any
   * @param config the active genesis configuration
   * @return the protocol schedule plan
   */
  public static ProtocolSchedulePlan resolvePlan(
      final Optional<ServiceManager> serviceManager, final GenesisConfigOptions config) {
    return serviceManager
        .flatMap(manager -> manager.getService(ProtocolScheduleContributionService.class))
        .filter(ProtocolScheduleContributionServiceImpl.class::isInstance)
        .map(ProtocolScheduleContributionServiceImpl.class::cast)
        .map(service -> service.freeze(config))
        .orElseGet(() -> ProtocolSchedulePlan.fromConfig(config));
  }
}
