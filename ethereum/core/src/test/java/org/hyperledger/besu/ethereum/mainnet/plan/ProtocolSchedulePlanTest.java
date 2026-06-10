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

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ProtocolSchedulePlanTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/mainnet.json",
        "/sepolia.json",
        "/hoodi.json",
        "/ephemery.json",
        "/dev.json",
        "/future.json",
        "/experimental.json",
        "/lukso.json",
        "/linea-mainnet.json",
        "/linea-sepolia.json"
      })
  public void forkIdActivationsMatchTheGenesisGettersForEveryBundledNetwork(
      final String genesisResource) {
    assertPlanMatchesConfig(GenesisConfig.fromResource(genesisResource).getConfigOptions());
  }

  @Test
  public void forkIdActivationsMatchTheGenesisGettersForADaoConfig() {
    assertPlanMatchesConfig(
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"daoForkBlock\": 3, \"eip150Block\": 14,"
                    + " \"byzantiumBlock\": 16, \"chainId\": 1234}}")
            .getConfigOptions());
  }

  @Test
  public void forkIdActivationsMatchTheGenesisGettersForAMergeNetSplitConfig() {
    assertPlanMatchesConfig(
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"berlinBlock\": 10, \"londonBlock\": 10,"
                    + " \"mergeNetSplitBlock\": 20, \"shanghaiTime\": 1000,"
                    + " \"terminalTotalDifficulty\": 100, \"chainId\": 1234}}")
            .getConfigOptions());
  }

  @Test
  public void includedContributionsFoldIntoTheForkIdListsPartitionedByActivationKind() {
    final GenesisConfigOptions config =
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"shanghaiTime\": 1000, \"chainId\": 1234}}")
            .getConfigOptions();

    final ProtocolSchedulePlan plan =
        ProtocolSchedulePlan.create(
            config,
            List.of(
                entry("block-fork", new Activation.BlockNumber(500), ForkIdBoundary.INCLUDED),
                entry("timestamp-fork", new Activation.Timestamp(500), ForkIdBoundary.INCLUDED),
                entry("excluded-fork", new Activation.BlockNumber(700), ForkIdBoundary.EXCLUDED),
                entry("duplicate-fork", new Activation.BlockNumber(2), ForkIdBoundary.INCLUDED)));

    // Partitioned by kind, never compared across kinds; distinct and sorted within each kind;
    // EXCLUDED entries are schedule-only.
    assertThat(plan.forkIdBlockNumbers()).containsExactly(2L, 500L);
    assertThat(plan.forkIdTimestamps()).containsExactly(500L, 1000L);
  }

  @Test
  public void scheduleSpecAdaptersComposeSameActivationModifiersInContributionOrder() {
    final GenesisConfigOptions config =
        GenesisConfig.fromConfig("{\"config\": {\"chainId\": 1234}}").getConfigOptions();
    final List<String> applied = new ArrayList<>();

    final ProtocolSchedulePlan plan =
        ProtocolSchedulePlan.create(
            config,
            List.of(
                modifierEntry("first", 100, tracing(applied, "first")),
                modifierEntry("second", 100, tracing(applied, "second"))));

    final ProtocolSpecBuilder unused =
        plan.scheduleSpecAdapters().getModifierForBlock(100).apply(null);
    assertThat(applied).containsExactly("first", "second");
  }

  @Test
  public void excludedContributionsReachTheScheduleButNotTheForkId() {
    final GenesisConfigOptions config =
        GenesisConfig.fromConfig("{\"config\": {\"homesteadBlock\": 2, \"chainId\": 1234}}")
            .getConfigOptions();
    final Function<ProtocolSpecBuilder, ProtocolSpecBuilder> modifier = builder -> builder;

    final ProtocolSchedulePlan plan =
        ProtocolSchedulePlan.create(
            config,
            List.of(
                new ForkEntry(
                    "schedule-only-fork",
                    new Activation.BlockNumber(500),
                    new ScheduleEffect.Modifier(modifier),
                    ForkIdBoundary.EXCLUDED)));

    // The activation is not advertised, but the schedule effect is in force.
    assertThat(plan.forkIdBlockNumbers()).containsExactly(2L);
    assertThat(plan.scheduleSpecAdapters().getModifierForBlock(500)).isSameAs(modifier);
  }

  private ForkEntry entry(
      final String id, final Activation activation, final ForkIdBoundary boundary) {
    return new ForkEntry(
        id, activation, new ScheduleEffect.Modifier(Function.identity()), boundary);
  }

  private ForkEntry modifierEntry(
      final String id,
      final long blockNumber,
      final Function<ProtocolSpecBuilder, ProtocolSpecBuilder> modifier) {
    return new ForkEntry(
        id,
        new Activation.BlockNumber(blockNumber),
        new ScheduleEffect.Modifier(modifier),
        ForkIdBoundary.INCLUDED);
  }

  private Function<ProtocolSpecBuilder, ProtocolSpecBuilder> tracing(
      final List<String> applied, final String name) {
    return builder -> {
      applied.add(name);
      return builder;
    };
  }

  private void assertPlanMatchesConfig(final GenesisConfigOptions config) {
    final ProtocolSchedulePlan plan = ProtocolSchedulePlan.fromConfig(config);
    assertThat(plan.forkIdBlockNumbers()).isEqualTo(config.getForkBlockNumbers());
    assertThat(plan.forkIdTimestamps()).isEqualTo(config.getForkBlockTimestamps());
  }
}
