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
package org.hyperledger.besu.ethereum.mainnet.milestones;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.datatypes.HardforkId;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.mainnet.BalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSpecFactory;
import org.hyperledger.besu.ethereum.mainnet.plan.ProtocolSchedulePlan;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pins that resolving the plan's milestone entries produces exactly the milestones the legacy
 * config-based enumeration produces — same hardforks, same activations, same kinds, same order —
 * for every bundled network and the interesting synthetic configs. The legacy enumeration is
 * retained only as this baseline and is removed once it has no other callers.
 */
public class MilestoneDefinitionsEquivalenceTest {

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
  public void planMilestonesMatchTheLegacyEnumerationForEveryBundledNetwork(
      final String genesisResource) {
    assertPlanMilestonesMatchLegacy(GenesisConfig.fromResource(genesisResource).getConfigOptions());
  }

  @Test
  public void planMilestonesMatchTheLegacyEnumerationForAMergeNetSplitConfig() {
    assertPlanMilestonesMatchLegacy(
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"berlinBlock\": 10, \"londonBlock\": 10,"
                    + " \"mergeNetSplitBlock\": 20, \"shanghaiTime\": 1000,"
                    + " \"terminalTotalDifficulty\": 100, \"chainId\": 1234}}")
            .getConfigOptions());
  }

  @Test
  public void planMilestonesMatchTheLegacyEnumerationForACollidingForkConfig() {
    // Constantinople and Petersburg at the same block: the real mainnet collision case.
    assertPlanMilestonesMatchLegacy(
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"byzantiumBlock\": 5,"
                    + " \"constantinopleBlock\": 10, \"petersburgBlock\": 10, \"chainId\": 1234}}")
            .getConfigOptions());
  }

  @Test
  public void planMilestonesMatchTheLegacyEnumerationForATimestampsOnlyConfig() {
    assertPlanMilestonesMatchLegacy(
        GenesisConfig.fromConfig(
                "{\"config\": {\"shanghaiTime\": 1000, \"cancunTime\": 2000, \"chainId\": 1234}}")
            .getConfigOptions());
  }

  private void assertPlanMilestonesMatchLegacy(final GenesisConfigOptions config) {
    final MainnetProtocolSpecFactory specFactory =
        new MainnetProtocolSpecFactory(
            Optional.empty(),
            false,
            config,
            EvmConfiguration.DEFAULT,
            MiningConfiguration.MINING_DISABLED,
            false,
            BalConfiguration.DEFAULT,
            new NoOpMetricsSystem());

    final List<Milestone> fromPlan =
        MilestoneDefinitions.fromPlan(specFactory, ProtocolSchedulePlan.fromConfig(config)).stream()
            .map(Milestone::of)
            .toList();
    final List<Milestone> legacy =
        MilestoneDefinitions.createMilestoneDefinitions(specFactory, config).stream()
            .filter(definition -> definition.blockNumberOrTimestamp().isPresent())
            .map(Milestone::of)
            .toList();

    assertThat(fromPlan).containsExactlyElementsOf(legacy);
  }

  private record Milestone(HardforkId hardforkId, long activation, MilestoneType milestoneType) {
    static Milestone of(final MilestoneDefinition definition) {
      return new Milestone(
          definition.hardforkId(),
          definition.blockNumberOrTimestamp().getAsLong(),
          definition.milestoneType());
    }
  }
}
