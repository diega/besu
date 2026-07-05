/*
 * Copyright ConsenSys AG.
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
package org.hyperledger.besu.ethereum.difficulty.fixed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.mainnet.BalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolScheduleCustomizer;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpec;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.plugin.ServiceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class FixedProtocolScheduleTest {

  @Test
  public void reportedDifficultyForAllBlocksIsAFixedValue() {

    final ProtocolSchedule schedule =
        FixedDifficultyProtocolSchedule.create(
            GenesisConfig.fromResource("/dev.json").getConfigOptions(),
            EvmConfiguration.DEFAULT,
            MiningConfiguration.MINING_DISABLED,
            new BadBlockManager(),
            false,
            BalConfiguration.DEFAULT,
            new NoOpMetricsSystem());

    final BlockHeaderTestFixture headerBuilder = new BlockHeaderTestFixture();

    final BlockHeader parentHeader = headerBuilder.number(1).buildHeader();

    assertThat(
            schedule
                .getByBlockHeader(blockHeader(0))
                .getDifficultyCalculator()
                .nextDifficulty(1, parentHeader))
        .isEqualTo(FixedDifficultyCalculators.DEFAULT_DIFFICULTY);

    assertThat(
            schedule
                .getByBlockHeader(blockHeader(500))
                .getDifficultyCalculator()
                .nextDifficulty(1, parentHeader))
        .isEqualTo(FixedDifficultyCalculators.DEFAULT_DIFFICULTY);

    assertThat(
            schedule
                .getByBlockHeader(blockHeader(500_000))
                .getDifficultyCalculator()
                .nextDifficulty(1, parentHeader))
        .isEqualTo(FixedDifficultyCalculators.DEFAULT_DIFFICULTY);
  }

  @Test
  public void customizerAdaptersAreAppliedAndComposedWithTheFixedDifficultyCalculator() {
    final Wei customReward = Wei.of(42_000_000L);
    // A customizer whose adapter shares block 0 with the fixed-difficulty calculator modifier.
    final ProtocolScheduleCustomizer customizer =
        config -> {
          final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> adapters =
              new HashMap<>();
          adapters.put(0L, builder -> builder.blockReward(customReward));
          return adapters;
        };

    final ServiceManager serviceManager = mock(ServiceManager.class);
    when(serviceManager.getService(ProtocolScheduleCustomizer.class))
        .thenReturn(Optional.of(customizer));

    final ProtocolSchedule schedule =
        FixedDifficultyProtocolSchedule.create(
            GenesisConfig.fromResource("/dev.json").getConfigOptions(),
            false,
            EvmConfiguration.DEFAULT,
            MiningConfiguration.MINING_DISABLED,
            new BadBlockManager(),
            false,
            BalConfiguration.DEFAULT,
            new NoOpMetricsSystem(),
            Optional.of(serviceManager));

    final ProtocolSpec spec = schedule.getByBlockHeader(blockHeader(0));
    final BlockHeader parentHeader = new BlockHeaderTestFixture().number(1).buildHeader();

    // the customizer's rule is enforced on the fixed-difficulty path...
    assertThat(spec.getBlockReward()).isEqualTo(customReward);
    // ...composed on top of (not replacing) the fixed-difficulty calculator at the shared block.
    assertThat(spec.getDifficultyCalculator().nextDifficulty(1, parentHeader))
        .isEqualTo(FixedDifficultyCalculators.DEFAULT_DIFFICULTY);
  }

  private BlockHeader blockHeader(final long number) {
    return new BlockHeaderTestFixture().number(number).buildHeader();
  }
}
