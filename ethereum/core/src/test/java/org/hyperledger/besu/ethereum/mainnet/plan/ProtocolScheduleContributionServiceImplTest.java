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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.GenesisConfigOptions;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class ProtocolScheduleContributionServiceImplTest {

  private final GenesisConfigOptions config =
      GenesisConfig.fromConfig("{\"config\": {\"chainId\": 1234}}").getConfigOptions();

  private final ProtocolScheduleContributionServiceImpl service =
      new ProtocolScheduleContributionServiceImpl();

  @Test
  public void freezeInvokesEachContributorExactlyOnce() {
    final AtomicInteger invocations = new AtomicInteger();
    service.registerContributor(
        cfg -> {
          invocations.incrementAndGet();
          return List.of(includedBlockFork("fork-a", 100));
        });

    final ProtocolSchedulePlan firstFreeze = service.freeze(config);
    final ProtocolSchedulePlan secondFreeze = service.freeze(config);

    assertThat(invocations).hasValue(1);
    assertThat(secondFreeze).isSameAs(firstFreeze);
    assertThat(firstFreeze.forkIdBlockNumbers()).containsExactly(100L);
  }

  @Test
  public void registrationAfterFreezeFailsFast() {
    final ProtocolSchedulePlan unused = service.freeze(config);

    assertThatThrownBy(() -> service.registerContributor(cfg -> List.of()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("after the ProtocolSchedulePlan is frozen");
  }

  @Test
  public void modifiersFromDifferentContributorsAtTheSameActivationAreRejected() {
    service.registerContributor(cfg -> List.of(modifierFork("fork-a", 100)));
    service.registerContributor(cfg -> List.of(modifierFork("fork-b", 100)));

    assertThatThrownBy(() -> service.freeze(config))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cross-contributor composition");
  }

  @Test
  public void aSingleContributorMayStackModifiersAtOneActivation() {
    service.registerContributor(
        cfg -> List.of(modifierFork("fork-a", 100), modifierFork("fork-b", 100)));

    final ProtocolSchedulePlan plan = service.freeze(config);

    assertThat(plan.contributedEntries()).hasSize(2);
  }

  @Test
  public void modifiersAtTheSameValueButDifferentActivationKindsDoNotCollide() {
    service.registerContributor(cfg -> List.of(modifierFork("fork-a", 100)));
    service.registerContributor(
        cfg ->
            List.of(
                new ForkEntry(
                    "fork-b",
                    new Activation.Timestamp(100),
                    new ScheduleEffect.Modifier(Function.identity()),
                    ForkIdBoundary.INCLUDED)));

    final ProtocolSchedulePlan plan = service.freeze(config);

    assertThat(plan.forkIdBlockNumbers()).containsExactly(100L);
    assertThat(plan.forkIdTimestamps()).containsExactly(100L);
  }

  private ForkEntry includedBlockFork(final String id, final long blockNumber) {
    return new ForkEntry(
        id,
        new Activation.BlockNumber(blockNumber),
        new ScheduleEffect.Modifier(Function.identity()),
        ForkIdBoundary.INCLUDED);
  }

  private ForkEntry modifierFork(final String id, final long blockNumber) {
    return includedBlockFork(id, blockNumber);
  }
}
