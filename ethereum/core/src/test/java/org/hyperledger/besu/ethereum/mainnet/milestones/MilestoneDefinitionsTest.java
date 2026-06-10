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
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.ARROW_GLACIER;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.BERLIN;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.BPO1;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.BPO2;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.BYZANTIUM;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.CANCUN;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.CONSTANTINOPLE;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.FRONTIER;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.GRAY_GLACIER;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.HOMESTEAD;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.ISTANBUL;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.LONDON;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.MUIR_GLACIER;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.OSAKA;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.PARIS;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.PETERSBURG;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.PRAGUE;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.SHANGHAI;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.SPURIOUS_DRAGON;
import static org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId.TANGERINE_WHISTLE;
import static org.hyperledger.besu.ethereum.mainnet.milestones.MilestoneType.BLOCK_NUMBER;
import static org.hyperledger.besu.ethereum.mainnet.milestones.MilestoneType.TIMESTAMP;

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

/**
 * Golden oracle for the plan-driven milestone enumeration: the exact hardforks, activations, kinds
 * and order the schedule is built from, pinned as literals.
 */
public class MilestoneDefinitionsTest {

  @Test
  public void mainnetMilestonesAreTheCanonicalOnes() {
    assertThat(milestonesOf(GenesisConfig.mainnet().getConfigOptions()))
        .containsExactly(
            new Milestone(FRONTIER, 0, BLOCK_NUMBER),
            new Milestone(HOMESTEAD, 1_150_000, BLOCK_NUMBER),
            new Milestone(TANGERINE_WHISTLE, 2_463_000, BLOCK_NUMBER),
            new Milestone(SPURIOUS_DRAGON, 2_675_000, BLOCK_NUMBER),
            new Milestone(BYZANTIUM, 4_370_000, BLOCK_NUMBER),
            // mainnet.json does not declare constantinopleBlock; Petersburg carries the milestone.
            new Milestone(PETERSBURG, 7_280_000, BLOCK_NUMBER),
            new Milestone(ISTANBUL, 9_069_000, BLOCK_NUMBER),
            new Milestone(MUIR_GLACIER, 9_200_000, BLOCK_NUMBER),
            new Milestone(BERLIN, 12_244_000, BLOCK_NUMBER),
            new Milestone(LONDON, 12_965_000, BLOCK_NUMBER),
            new Milestone(ARROW_GLACIER, 13_773_000, BLOCK_NUMBER),
            new Milestone(GRAY_GLACIER, 15_050_000, BLOCK_NUMBER),
            new Milestone(SHANGHAI, 1_681_338_455, TIMESTAMP),
            new Milestone(CANCUN, 1_710_338_135, TIMESTAMP),
            new Milestone(PRAGUE, 1_746_612_311, TIMESTAMP),
            new Milestone(OSAKA, 1_764_798_551, TIMESTAMP),
            new Milestone(BPO1, 1_765_290_071, TIMESTAMP),
            new Milestone(BPO2, 1_767_747_671, TIMESTAMP));
  }

  @Test
  public void mergeNetSplitBlockSchedulesParisAsABlockMilestone() {
    final GenesisConfigOptions config =
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"berlinBlock\": 10, \"londonBlock\": 10,"
                    + " \"mergeNetSplitBlock\": 20, \"shanghaiTime\": 1000,"
                    + " \"terminalTotalDifficulty\": 100, \"chainId\": 1234}}")
            .getConfigOptions();

    assertThat(milestonesOf(config))
        .containsExactly(
            new Milestone(FRONTIER, 0, BLOCK_NUMBER),
            new Milestone(HOMESTEAD, 2, BLOCK_NUMBER),
            new Milestone(BERLIN, 10, BLOCK_NUMBER),
            new Milestone(LONDON, 10, BLOCK_NUMBER),
            new Milestone(PARIS, 20, BLOCK_NUMBER),
            new Milestone(SHANGHAI, 1000, TIMESTAMP));
  }

  @Test
  public void collidingForksKeepTheirEnumerationOrder() {
    // Constantinople and Petersburg at the same block: the real mainnet collision case -- the
    // schedule's last-wins flattening depends on this order.
    final GenesisConfigOptions config =
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"byzantiumBlock\": 5,"
                    + " \"constantinopleBlock\": 10, \"petersburgBlock\": 10, \"chainId\": 1234}}")
            .getConfigOptions();

    assertThat(milestonesOf(config))
        .containsExactly(
            new Milestone(FRONTIER, 0, BLOCK_NUMBER),
            new Milestone(HOMESTEAD, 2, BLOCK_NUMBER),
            new Milestone(BYZANTIUM, 5, BLOCK_NUMBER),
            new Milestone(CONSTANTINOPLE, 10, BLOCK_NUMBER),
            new Milestone(PETERSBURG, 10, BLOCK_NUMBER));
  }

  @Test
  public void timestampOnlyConfigsScheduleFrontierAtGenesis() {
    final GenesisConfigOptions config =
        GenesisConfig.fromConfig(
                "{\"config\": {\"shanghaiTime\": 1000, \"cancunTime\": 2000, \"chainId\": 1234}}")
            .getConfigOptions();

    assertThat(milestonesOf(config))
        .containsExactly(
            new Milestone(FRONTIER, 0, BLOCK_NUMBER),
            new Milestone(SHANGHAI, 1000, TIMESTAMP),
            new Milestone(CANCUN, 2000, TIMESTAMP));
  }

  private List<Milestone> milestonesOf(final GenesisConfigOptions config) {
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
    return MilestoneDefinitions.fromPlan(specFactory, ProtocolSchedulePlan.fromConfig(config))
        .stream()
        .map(
            definition ->
                new Milestone(
                    definition.hardforkId(),
                    definition.blockNumberOrTimestamp().getAsLong(),
                    definition.milestoneType()))
        .toList();
  }

  private record Milestone(HardforkId hardforkId, long activation, MilestoneType milestoneType) {}
}
