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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.plugin.ServiceManager;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * Proves the effect of registering the {@link ProtocolScheduleContributionService} with the plugin
 * context: {@link ProtocolScheduleContributionServiceImpl#resolvePlan} folds a registered
 * contributor's activations into the plan, and falls back to the genesis configuration (leaving the
 * fork ID unchanged) when the service is absent. Registering the service in production is what wires
 * the present branch in for every fork-ID and schedule consumer.
 */
class ProtocolSchedulePlanActivationTest {

  // genesis-declared fork: homestead at block 10.
  private static final GenesisConfigOptions CONFIG =
      GenesisConfig.fromConfig("{\"config\":{\"homesteadBlock\":10}}").getConfigOptions();

  @Test
  void withoutTheServiceTheForkIdFallsBackToTheGenesisConfig() {
    final ProtocolSchedulePlan plan =
        ProtocolScheduleContributionServiceImpl.resolvePlan(Optional.empty(), CONFIG);

    assertThat(plan.forkIdBlockNumbers()).containsExactly(10L);
    assertThat(plan.contributedEntries()).isEmpty();
  }

  @Test
  void withTheRegisteredServiceAContributorsActivationsAreFoldedIn() {
    final ProtocolScheduleContributionServiceImpl service =
        new ProtocolScheduleContributionServiceImpl();
    service.registerContributor(
        config ->
            List.of(
                new ForkEntry(
                    "plugin-fork",
                    new Activation.BlockNumber(22L),
                    new ScheduleEffect.Modifier(Function.identity()),
                    ForkIdBoundary.INCLUDED)));
    final ServiceManager serviceManager = mock(ServiceManager.class);
    when(serviceManager.getService(ProtocolScheduleContributionService.class))
        .thenReturn(Optional.of(service));

    final ProtocolSchedulePlan plan =
        ProtocolScheduleContributionServiceImpl.resolvePlan(
            Optional.of(serviceManager), CONFIG);

    // genesis fork (10) unioned with the contributor's INCLUDED activation (22)
    assertThat(plan.forkIdBlockNumbers()).containsExactly(10L, 22L);
    assertThat(plan.contributedEntries()).hasSize(1);
  }
}
