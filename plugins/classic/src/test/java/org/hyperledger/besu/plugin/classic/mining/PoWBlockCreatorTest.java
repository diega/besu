/*
 * Copyright contributors to Besu.
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
package org.hyperledger.besu.plugin.classic.mining;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.blockcreation.BlockCreator.BlockCreationResult;
import org.hyperledger.besu.ethereum.blockcreation.RandomNonceGenerator;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.ExecutionContextTestFixture;
import org.hyperledger.besu.ethereum.core.ImmutableMiningConfiguration;
import org.hyperledger.besu.ethereum.core.ImmutableMiningConfiguration.MutableInitValues;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.difficulty.fixed.FixedDifficultyProtocolSchedule;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPool;
import org.hyperledger.besu.ethereum.mainnet.EpochCalculator;
import org.hyperledger.besu.ethereum.mainnet.ImmutableBalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.PoWHasher;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.plugin.classic.PoWMiningCLIOptions;
import org.hyperledger.besu.testutil.DeterministicEthScheduler;
import org.hyperledger.besu.util.Subscribers;

import java.math.BigInteger;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class PoWBlockCreatorTest {

  private static final String GENESIS_WITH_LOW_DIFFICULTY =
      """
      {
        "config": {
          "chainId": 1,
          "homesteadBlock": 0,
          "ethash": {
            "fixeddifficulty": 1
          }
        },
        "difficulty": "0x1",
        "gasLimit": "0x1000000",
        "alloc": {
          "0x0000000000000000000000000000000000000001": {
            "balance": "1000000000000000000000"
          }
        }
      }
      """;

  @Test
  void createsValidBlockWithPoW() {
    final GenesisConfig genesisConfig = GenesisConfig.fromConfig(GENESIS_WITH_LOW_DIFFICULTY);

    // Create a protocol schedule that respects the fixeddifficulty from genesis config
    final ProtocolSchedule protocolSchedule =
        FixedDifficultyProtocolSchedule.create(
            genesisConfig.getConfigOptions(),
            EvmConfiguration.DEFAULT,
            MiningConfiguration.MINING_DISABLED,
            new BadBlockManager(),
            false,
            ImmutableBalConfiguration.builder().build(),
            new NoOpMetricsSystem());

    final ExecutionContextTestFixture executionContext =
        ExecutionContextTestFixture.builder(genesisConfig)
            .protocolSchedule(protocolSchedule)
            .build();

    final MiningConfiguration miningConfig =
        ImmutableMiningConfiguration.builder()
            .mutableInitValues(
                MutableInitValues.builder()
                    .coinbase(Address.fromHexString("0x0000000000000000000000000000000000000001"))
                    .minTransactionGasPrice(Wei.ZERO)
                    .nonceGenerator(new RandomNonceGenerator())
                    .build())
            .build();

    final PoWSolver solver =
        new PoWSolver(
            miningConfig,
            PoWHasher.ETHASH_LIGHT,
            Subscribers.none(),
            new EpochCalculator.DefaultEpochCalculator(),
            PoWMiningCLIOptions.DEFAULT_POW_JOB_TTL,
            PoWMiningCLIOptions.DEFAULT_MAX_OMMERS_DEPTH);

    // TransactionPool not used when we pass explicit transactions list
    final TransactionPool transactionPool = mock(TransactionPool.class);

    final PoWBlockCreator blockCreator =
        new PoWBlockCreator(
            miningConfig,
            parent -> miningConfig.getExtraData(),
            transactionPool,
            executionContext.getProtocolContext(),
            executionContext.getProtocolSchedule(),
            solver,
            new DeterministicEthScheduler());

    final BlockHeader parentHeader = executionContext.getBlockchain().getChainHeadHeader();
    final long timestamp = parentHeader.getTimestamp() + 1;
    final BlockCreationResult result =
        blockCreator.createBlock(
            Collections.emptyList(), Collections.emptyList(), timestamp, parentHeader);

    final Block block = result.getBlock();

    // Verify block was created
    assertThat(block).isNotNull();
    assertThat(block.getHeader().getNumber()).isEqualTo(1);
    assertThat(block.getHeader().getParentHash()).isEqualTo(parentHeader.getHash());

    // Verify PoW solution is present (computed by ETHASH_LIGHT)
    assertThat(block.getHeader().getNonce()).isNotEqualTo(0L);
    assertThat(block.getHeader().getMixHash()).isNotEqualTo(Hash.ZERO);

    // Verify the difficulty
    final BigInteger difficulty = block.getHeader().getDifficulty().toBigInteger();
    assertThat(difficulty).isEqualTo(BigInteger.ONE);

    // The block should be importable (validates PoW internally)
    executionContext.getBlockchain().appendBlock(block, Collections.emptyList());
    assertThat(executionContext.getBlockchain().getChainHead().getHeight()).isEqualTo(1);
  }
}
