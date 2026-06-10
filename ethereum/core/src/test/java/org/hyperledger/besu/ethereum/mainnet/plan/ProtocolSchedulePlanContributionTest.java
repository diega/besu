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
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.mainnet.BalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpec;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/**
 * The decisive end-to-end check for the contribution seam: a fork declared once, through a single
 * {@code contribute(config)} invocation, must reach the advertised fork ID <em>and</em> the
 * protocol schedule — the duality is dead, not relocated.
 */
public class ProtocolSchedulePlanContributionTest {

  private static final long CONTRIBUTED_FORK_BLOCK = 1_000L;

  @Test
  public void aSingleContributionReachesTheForkIdAndTheSchedule() {
    final GenesisConfigOptions config =
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 0, \"berlinBlock\": 0, \"londonBlock\": 0,"
                    + " \"chainId\": 1234}}")
            .getConfigOptions();

    final AtomicInteger invocations = new AtomicInteger();
    final ProtocolScheduleContributionServiceImpl service =
        new ProtocolScheduleContributionServiceImpl();
    service.registerContributor(
        cfg -> {
          invocations.incrementAndGet();
          return List.of(
              new ForkEntry(
                  "contributed-fork",
                  new Activation.BlockNumber(CONTRIBUTED_FORK_BLOCK),
                  new ScheduleEffect.Modifier(builder -> builder.blockReward(Wei.of(42))),
                  ForkIdBoundary.INCLUDED));
        });

    final ProtocolSchedulePlan plan = service.freeze(config);

    // One invocation...
    assertThat(invocations).hasValue(1);

    // ...feeding the fork ID...
    assertThat(plan.forkIdBlockNumbers()).contains(CONTRIBUTED_FORK_BLOCK);

    // ...and the schedule, composed over the resolved floor spec rather than replacing it.
    final ProtocolSchedule schedule =
        MainnetProtocolSchedule.fromConfig(
            config,
            Optional.empty(),
            Optional.empty(),
            MiningConfiguration.MINING_DISABLED,
            new BadBlockManager(),
            false,
            BalConfiguration.DEFAULT,
            new NoOpMetricsSystem(),
            plan.scheduleSpecAdapters());
    final ProtocolSpec contributedSpec =
        schedule.getByBlockHeader(blockHeader(CONTRIBUTED_FORK_BLOCK));
    assertThat(contributedSpec.getBlockReward()).isEqualTo(Wei.of(42));
    assertThat(contributedSpec.getFeeMarket().implementsBaseFee()).isTrue();
    assertThat(schedule.getByBlockHeader(blockHeader(CONTRIBUTED_FORK_BLOCK - 1)).getBlockReward())
        .isNotEqualTo(Wei.of(42));
  }

  @Test
  public void contributionsAtSuccessiveActivationsAccumulateInTheSchedule() {
    // The dominant contributor case: several network forks at different activations, each
    // building on the rules in force since the previous one.
    final GenesisConfigOptions config =
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 0, \"berlinBlock\": 0, \"londonBlock\": 0,"
                    + " \"chainId\": 1234}}")
            .getConfigOptions();
    final ProtocolScheduleContributionServiceImpl service =
        new ProtocolScheduleContributionServiceImpl();
    service.registerContributor(
        cfg ->
            List.of(
                new ForkEntry(
                    "first-fork",
                    new Activation.BlockNumber(1_000),
                    new ScheduleEffect.Modifier(builder -> builder.blockReward(Wei.of(42))),
                    ForkIdBoundary.INCLUDED),
                new ForkEntry(
                    "second-fork",
                    new Activation.BlockNumber(2_000),
                    new ScheduleEffect.Modifier(builder -> builder.isPoS(true)),
                    ForkIdBoundary.INCLUDED)));

    final ProtocolSchedule schedule =
        MainnetProtocolSchedule.fromConfig(
            config,
            Optional.empty(),
            Optional.empty(),
            MiningConfiguration.MINING_DISABLED,
            new BadBlockManager(),
            false,
            BalConfiguration.DEFAULT,
            new NoOpMetricsSystem(),
            service.freeze(config).scheduleSpecAdapters());

    final ProtocolSpec firstSpec = schedule.getByBlockHeader(blockHeader(1_000));
    assertThat(firstSpec.getBlockReward()).isEqualTo(Wei.of(42));
    assertThat(firstSpec.isPoS()).isFalse();

    // The first fork's rules stay in force when the second activates.
    final ProtocolSpec secondSpec = schedule.getByBlockHeader(blockHeader(2_000));
    assertThat(secondSpec.getBlockReward()).isEqualTo(Wei.of(42));
    assertThat(secondSpec.isPoS()).isTrue();
  }

  private BlockHeader blockHeader(final long number) {
    return new BlockHeaderTestFixture().number(number).buildHeader();
  }
}
