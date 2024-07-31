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

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.chain.Blockchain;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;

import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes32;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ClassicECIP1559BlockProcessorTest {

  static Stream<Arguments> testCases() {
    return Stream.of(
        arguments(named("Genesis", new TestCase(1, 0, Wei.ONE))),
        arguments(named("FirstFeeBasedBlock", new TestCase(1, 1, Wei.ONE))),
        arguments(named("FeeBasedBlock", new TestCase(1, 2, Wei.of(2)))));
  }

  @ParameterizedTest
  @MethodSource("testCases")
  void coinbaseReward(final TestCase testCase) {
    final MainnetTransactionProcessor transactionProcessor =
        mock(MainnetTransactionProcessor.class);
    final AbstractBlockProcessor.TransactionReceiptFactory transactionReceiptFactory =
        mock(AbstractBlockProcessor.TransactionReceiptFactory.class);
    final ProtocolSchedule protocolSchedule = mock(ProtocolSchedule.class);
    final Blockchain testBlockchain =
        mockSingleBranchBlockchain(testCase.noBaseFeeBlocks(), testCase.baseFeeBlocks());
    final Wei blockReward = Wei.ONE;
    final BlockProcessor blockProcessor =
        new ClassicECIP1559BlockProcessor(
            transactionProcessor,
            transactionReceiptFactory,
            blockReward,
            BlockHeader::getCoinbase,
            true,
            OptionalLong.empty(),
            protocolSchedule);
    final BlockHeader genesisHeader = testBlockchain.getChainHeadHeader();
    final Wei genesisCoinbaseReward =
        blockProcessor.getCoinbaseReward(
            testBlockchain,
            blockReward,
            genesisHeader.getParentHash(),
            genesisHeader.getNumber(),
            0);
    Assertions.assertThat(genesisCoinbaseReward).isEqualTo(testCase.expectedRewardForChainHead());
  }

  private Blockchain mockSingleBranchBlockchain(
      final long noBaseFeeBlocks, final long baseFeeBlocks) {
    final LinkedHashMap<Hash, BlockHeader> hashToHeader = new LinkedHashMap<>();
    Hash parentHash = Hash.ZERO;
    for (long blockNumber = 0; blockNumber < noBaseFeeBlocks; blockNumber++) {
      final Hash hash = Hash.wrap(Bytes32.random());
      hashToHeader.put(
          hash,
          new BlockHeaderTestFixture().number(blockNumber).parentHash(parentHash).buildHeader());
      parentHash = hash;
    }
    for (long blockNumber = noBaseFeeBlocks;
        blockNumber < noBaseFeeBlocks + baseFeeBlocks;
        blockNumber++) {
      final Hash hash = Hash.wrap(Bytes32.random());
      hashToHeader.put(
          hash,
          new BlockHeaderTestFixture()
              .number(blockNumber)
              .parentHash(parentHash)
              .baseFeePerGas(Wei.ONE)
              .gasUsed(1)
              .buildHeader());
      parentHash = hash;
    }
    final Blockchain blockchain = mock(Blockchain.class);
    when(blockchain.getBlockHeaderSafe(any(Hash.class)))
        .thenAnswer(
            invocation -> {
              final Hash hash = invocation.getArgument(0);
              return Optional.ofNullable(hashToHeader.get(hash));
            });
    when(blockchain.getChainHeadHeader())
        .thenAnswer(invocation -> hashToHeader.lastEntry().getValue());
    return blockchain;
  }

  record TestCase(long noBaseFeeBlocks, long baseFeeBlocks, Wei expectedRewardForChainHead) {}
}
