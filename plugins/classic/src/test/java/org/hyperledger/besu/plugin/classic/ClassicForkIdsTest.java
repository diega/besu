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
package org.hyperledger.besu.plugin.classic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.consensus.merge.MergeProtocolSchedule;
import org.hyperledger.besu.consensus.merge.PostMergeContext;
import org.hyperledger.besu.consensus.merge.TransitionProtocolSchedule;
import org.hyperledger.besu.consensus.merge.TransitionUtils;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.chain.GenesisState;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.MilestoneStreamingProtocolSchedule;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.forkid.ForkId;
import org.hyperledger.besu.ethereum.forkid.ForkIdManager;
import org.hyperledger.besu.ethereum.mainnet.BalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.DefaultProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecProvider;
import org.hyperledger.besu.ethereum.trie.pathbased.bonsai.cache.CodeCache;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.plugin.classic.protocol.ClassicProtocolSpecProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests ForkId calculation for Classic networks. These tests were moved from the core module when
 * Classic protocol specs were extracted to the plugin.
 */
@ExtendWith(MockitoExtension.class)
public class ClassicForkIdsTest {
  private static final PostMergeContext postMergeContext = new PostMergeContext();

  public static Collection<Object[]> parameters() {
    return List.of(
        new Object[] {
          "/mordor.json",
          List.of(
              new ForkId(Bytes.ofUnsignedInt(0x175782aaL), 301243L),
              new ForkId(Bytes.ofUnsignedInt(0x604f6ee1L), 999983L),
              new ForkId(Bytes.ofUnsignedInt(0xf42f5539L), 2520000L),
              new ForkId(Bytes.ofUnsignedInt(0x66b5c286L), 3985893),
              new ForkId(Bytes.ofUnsignedInt(0x92b323e0L), 5520000L),
              new ForkId(Bytes.ofUnsignedInt(0x8c9b1797L), 9957000L),
              new ForkId(Bytes.ofUnsignedInt(0x3a6b00d7L), 0L),
              new ForkId(Bytes.ofUnsignedInt(0x3a6b00d7L), 0L))
        },
        new Object[] {
          "/classic.json",
          List.of(
              // Milestones: [0, 1150000, 2500000, 3000000, 5000000, 5900000, 8772000, 9573000,
              //              10500839, 11700000, 13189133, 14525000, 19250000, MAX_VALUE]
              new ForkId(Bytes.ofUnsignedInt(0xfc64ec04L), 1150000L), // Frontier -> Homestead
              new ForkId(Bytes.ofUnsignedInt(0x97c2c34cL), 2500000L), // Homestead -> ECIP1015
              new ForkId(Bytes.ofUnsignedInt(0xdb06803fL), 3000000L), // ECIP1015 -> DieHard
              new ForkId(Bytes.ofUnsignedInt(0xaff4bed4L), 5000000L), // DieHard -> Gotham
              new ForkId(
                  Bytes.ofUnsignedInt(0xf79a63c0L), 5900000L), // Gotham -> DefuseDifficultyBomb
              new ForkId(
                  Bytes.ofUnsignedInt(0x744899d6L), 8772000L), // DefuseDifficultyBomb -> Atlantis
              new ForkId(Bytes.ofUnsignedInt(0x518b59c6L), 9573000L), // Atlantis -> Agharta
              new ForkId(Bytes.ofUnsignedInt(0x7ba22882L), 10500839L), // Agharta -> Phoenix
              new ForkId(Bytes.ofUnsignedInt(0x9007bfccL), 11700000L), // Phoenix -> Thanos
              new ForkId(Bytes.ofUnsignedInt(0xdb63a1caL), 13189133), // Thanos -> Magneto
              new ForkId(Bytes.ofUnsignedInt(0x0f6bf187L), 14525000L), // Magneto -> Mystique
              new ForkId(Bytes.ofUnsignedInt(0x7fd1bb25L), 19250000L), // Mystique -> Spiral
              new ForkId(Bytes.ofUnsignedInt(0xbe46d57cL), 0L), // Spiral (no next)
              new ForkId(Bytes.ofUnsignedInt(0xbe46d57cL), 0L)) // MAX_VALUE
        });
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testForkId(final String genesisFile, final List<ForkId> expectedForkIds) {
    final GenesisConfig genesisConfig =
        GenesisConfig.fromConfig(ClassicForkIdsTest.class.getResource(genesisFile));
    final MilestoneStreamingTransitionProtocolSchedule schedule = createSchedule(genesisConfig);
    final GenesisState genesisState =
        GenesisState.fromConfig(genesisConfig, schedule, new CodeCache());
    final Blockchain mockBlockchain = mock(Blockchain.class);
    final BlockHeader mockBlockHeader = mock(BlockHeader.class);

    when(mockBlockchain.getGenesisBlock()).thenReturn(genesisState.getBlock());

    final AtomicLong blockNumber = new AtomicLong();
    when(mockBlockchain.getChainHeadHeader()).thenReturn(mockBlockHeader);
    when(mockBlockHeader.getNumber()).thenAnswer(o -> blockNumber.get());

    // Combine forks from config and Classic provider
    final ClassicForkBlockNumbersProvider provider = new ClassicForkBlockNumbersProvider();
    final List<Long> allForkBlockNumbers = new ArrayList<>(genesisConfig.getForkBlockNumbers());
    allForkBlockNumbers.addAll(provider.getForkBlockNumbers(genesisConfig.getConfigOptions()));

    final ForkIdManager forkIdManager =
        new ForkIdManager(mockBlockchain, allForkBlockNumbers, List.of());

    final List<ForkId> actualForkIds =
        Streams.concat(schedule.streamMilestoneBlocks(), Stream.of(Long.MAX_VALUE))
            .map(
                block -> {
                  blockNumber.set(block);
                  return forkIdManager.getForkIdForChainHead();
                })
            .collect(Collectors.toList());

    assertThat(actualForkIds).containsExactlyElementsOf(expectedForkIds);
  }

  private static MilestoneStreamingTransitionProtocolSchedule createSchedule(
      final GenesisConfig genesisConfig) {
    final GenesisConfigOptions configOptions = genesisConfig.getConfigOptions();
    final List<ProtocolSpecProvider> providers = List.of(new ClassicProtocolSpecProvider());

    MilestoneStreamingProtocolSchedule preMergeProtocolSchedule =
        new MilestoneStreamingProtocolSchedule(
            (DefaultProtocolSchedule)
                MainnetProtocolSchedule.fromConfig(
                    configOptions,
                    Optional.empty(),
                    Optional.empty(),
                    MiningConfiguration.MINING_DISABLED,
                    new BadBlockManager(),
                    false,
                    BalConfiguration.DEFAULT,
                    new NoOpMetricsSystem(),
                    providers));
    MilestoneStreamingProtocolSchedule postMergeProtocolSchedule =
        new MilestoneStreamingProtocolSchedule(
            (DefaultProtocolSchedule)
                MergeProtocolSchedule.create(
                    configOptions,
                    false,
                    MiningConfiguration.MINING_DISABLED,
                    new BadBlockManager(),
                    false,
                    BalConfiguration.DEFAULT,
                    new NoOpMetricsSystem(),
                    EvmConfiguration.DEFAULT));
    final MilestoneStreamingTransitionProtocolSchedule schedule =
        new MilestoneStreamingTransitionProtocolSchedule(
            preMergeProtocolSchedule, postMergeProtocolSchedule);
    return schedule;
  }

  public static class MilestoneStreamingTransitionProtocolSchedule
      extends TransitionProtocolSchedule {

    private final TransitionUtils<MilestoneStreamingProtocolSchedule> transitionUtils;

    public MilestoneStreamingTransitionProtocolSchedule(
        final MilestoneStreamingProtocolSchedule preMergeProtocolSchedule,
        final MilestoneStreamingProtocolSchedule postMergeProtocolSchedule) {
      super(preMergeProtocolSchedule, postMergeProtocolSchedule, postMergeContext);
      transitionUtils =
          new TransitionUtils<>(
              preMergeProtocolSchedule, postMergeProtocolSchedule, postMergeContext);
    }

    public Stream<Long> streamMilestoneBlocks() {
      return transitionUtils.dispatchFunctionAccordingToMergeState(
          MilestoneStreamingProtocolSchedule::streamMilestoneBlocks);
    }
  }

  @Test
  void dryRunDetector() {
    assertThat(true)
        .withFailMessage("This test is here so gradle --dry-run executes this class")
        .isTrue();
  }
}
