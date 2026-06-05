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
package org.hyperledger.besu.consensus.merge;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.mainnet.Activation;
import org.hyperledger.besu.ethereum.mainnet.BalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.ForkEntry;
import org.hyperledger.besu.ethereum.mainnet.ForkIdBoundary;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedulePlan;
import org.hyperledger.besu.ethereum.mainnet.ScheduleEffect;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;

import java.util.List;

import org.junit.jupiter.api.Test;

class MergeScheduleFromPlanTest {

  private static final GenesisConfigOptions CONFIG =
      GenesisConfig.fromConfig(
              "{\"config\":{\"chainId\":1,\"homesteadBlock\":1,\"londonBlock\":1559}}")
          .getConfigOptions();

  @Test
  void pluginModifierFromThePlanIsAppliedOnThePostMergeSchedule() {
    final long activationBlock = 5_000_000L;
    final Wei customReward = Wei.of(42_000_000L);
    final ProtocolSchedulePlan plan =
        ProtocolSchedulePlan.create(
            CONFIG,
            List.of(
                new ForkEntry(
                    "custom-reward",
                    new Activation.BlockNumber(activationBlock),
                    new ScheduleEffect.Modifier(builder -> builder.blockReward(customReward)),
                    ForkIdBoundary.INCLUDED)));

    final ProtocolSchedule schedule =
        MergeProtocolSchedule.create(
            CONFIG,
            false,
            MiningConfiguration.MINING_DISABLED,
            new BadBlockManager(),
            false,
            BalConfiguration.DEFAULT,
            new NoOpMetricsSystem(),
            EvmConfiguration.DEFAULT,
            plan.scheduleSpecAdapters());

    assertThat(schedule.getByBlockHeader(blockHeader(activationBlock)).getBlockReward())
        .isEqualTo(customReward);
    assertThat(schedule.getByBlockHeader(blockHeader(activationBlock - 1)).getBlockReward())
        .isNotEqualTo(customReward);
  }

  private static BlockHeader blockHeader(final long number) {
    return new BlockHeaderTestFixture().number(number).buildHeader();
  }
}
