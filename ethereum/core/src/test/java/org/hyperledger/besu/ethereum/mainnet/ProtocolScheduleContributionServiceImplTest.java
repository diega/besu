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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.GenesisConfigOptions;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

class ProtocolScheduleContributionServiceImplTest {

  // genesis-declared forks: homestead block 10, shanghai timestamp 1000.
  private static final GenesisConfigOptions CONFIG =
      GenesisConfig.fromConfig("{\"config\":{\"homesteadBlock\":10,\"shanghaiTime\":1000}}")
          .getConfigOptions();

  private static ForkEntry entry(
      final String id, final Activation activation, final ForkIdBoundary boundary) {
    return new ForkEntry(
        id, activation, new ScheduleEffect.Modifier(Function.identity()), boundary);
  }

  @Test
  void includedContributionsAreFoldedIntoForkIdByActivationKind() {
    final ProtocolScheduleContributionServiceImpl service =
        new ProtocolScheduleContributionServiceImpl();
    service.registerContributor(
        config ->
            List.of(
                entry("block-fork", new Activation.BlockNumber(22L), ForkIdBoundary.INCLUDED),
                entry("ts-fork", new Activation.Timestamp(2000L), ForkIdBoundary.INCLUDED),
                entry("schedule-only", new Activation.BlockNumber(33L), ForkIdBoundary.EXCLUDED)));

    final ProtocolSchedulePlan plan = service.freeze(CONFIG);

    // genesis fork (10) + the INCLUDED block contribution (22); the EXCLUDED one (33) is not a
    // fork-ID boundary
    assertThat(plan.forkIdBlockNumbers()).containsExactly(10L, 22L);
    // genesis timestamp (1000) + the INCLUDED timestamp contribution (2000)
    assertThat(plan.forkIdTimestamps()).containsExactly(1000L, 2000L);
    // all entries (INCLUDED and EXCLUDED) are retained for protocol-schedule construction
    assertThat(plan.contributedEntries()).hasSize(3);
  }

  @Test
  void contributorsAreInvokedExactlyOnceAcrossRepeatedFreezes() {
    final AtomicInteger invocations = new AtomicInteger();
    final ProtocolScheduleContributionServiceImpl service =
        new ProtocolScheduleContributionServiceImpl();
    service.registerContributor(
        config -> {
          invocations.incrementAndGet();
          return List.of();
        });

    final ProtocolSchedulePlan first = service.freeze(CONFIG);
    final ProtocolSchedulePlan second = service.freeze(CONFIG);

    assertThat(invocations).hasValue(1);
    assertThat(second).isSameAs(first);
  }

  @Test
  void registeringAfterFreezeFailsFast() {
    final ProtocolScheduleContributionServiceImpl service =
        new ProtocolScheduleContributionServiceImpl();
    service.freeze(CONFIG);

    assertThatThrownBy(() -> service.registerContributor(config -> List.of()))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void differentContributorsModifyingTheSameActivationFailFast() {
    final ProtocolScheduleContributionServiceImpl service =
        new ProtocolScheduleContributionServiceImpl();
    service.registerContributor(
        config -> List.of(entry("a", new Activation.BlockNumber(100L), ForkIdBoundary.INCLUDED)));
    service.registerContributor(
        config -> List.of(entry("b", new Activation.BlockNumber(100L), ForkIdBoundary.INCLUDED)));

    assertThatThrownBy(() -> service.freeze(CONFIG)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void sameContributorMayModifyOneActivationMoreThanOnce() {
    final ProtocolScheduleContributionServiceImpl service =
        new ProtocolScheduleContributionServiceImpl();
    service.registerContributor(
        config ->
            List.of(
                entry("a", new Activation.BlockNumber(100L), ForkIdBoundary.INCLUDED),
                entry("b", new Activation.BlockNumber(100L), ForkIdBoundary.INCLUDED)));

    final ProtocolSchedulePlan plan = service.freeze(CONFIG);

    assertThat(plan.contributedEntries()).hasSize(2);
  }
}
