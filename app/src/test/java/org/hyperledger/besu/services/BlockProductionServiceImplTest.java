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
package org.hyperledger.besu.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.chain.MutableBlockchain;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderBuilder;
import org.hyperledger.besu.ethereum.core.BlockchainSetupUtil;
import org.hyperledger.besu.ethereum.core.Difficulty;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.eth.sync.BlockBroadcaster;
import org.hyperledger.besu.ethereum.mainnet.ScheduleBasedBlockHeaderFunctions;
import org.hyperledger.besu.plugin.data.CandidateBlock;
import org.hyperledger.besu.plugin.services.storage.DataStorageFormat;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlockProductionServiceImplTest {

  private static final Address COINBASE =
      Address.fromHexString("0x0000000000000000000000000000000000000042");
  private static final Bytes EXTRA_DATA = Bytes.fromHexString("0xc0ffee");

  private BlockchainSetupUtil setup;
  private BlockBroadcaster blockBroadcaster;
  private BlockProductionServiceImpl service;

  @BeforeEach
  void setUp() {
    setup = BlockchainSetupUtil.forTesting(DataStorageFormat.BONSAI);
    setup.importAllBlocks();
    blockBroadcaster = mock(BlockBroadcaster.class);
    service =
        new BlockProductionServiceImpl(
            setup.getProtocolContext(),
            setup.getProtocolSchedule(),
            setup.getTransactionPool(),
            setup.getScheduler(),
            blockBroadcaster,
            MiningConfiguration.newDefault());
  }

  @Test
  void createsUnsealedCandidateOnChainHead() {
    final BlockHeader parent = setup.getBlockchain().getChainHeadHeader();
    final long timestamp = parent.getTimestamp() + 1;

    final CandidateBlock candidate = service.createCandidateBlock(timestamp, COINBASE, EXTRA_DATA);

    final BlockHeader header = (BlockHeader) candidate.getBlockHeader();
    assertThat(header.getNumber()).isEqualTo(parent.getNumber() + 1);
    assertThat(header.getParentHash()).isEqualTo(parent.getHash());
    assertThat(header.getCoinbase()).isEqualTo(COINBASE);
    assertThat(header.getExtraData()).isEqualTo(EXTRA_DATA);
    assertThat(header.getNonce()).isZero();

    final Difficulty expectedDifficulty =
        Difficulty.of(
            setup
                .getProtocolSchedule()
                .getForNextBlockHeader(parent, timestamp)
                .getDifficultyCalculator()
                .nextDifficulty(timestamp, parent));
    assertThat(header.getDifficulty()).isEqualTo(expectedDifficulty);
  }

  @Test
  void publishesValidBlockAndAnnouncesIt() {
    final BlockHeader parent = setup.getBlockchain().getChainHeadHeader();
    final CandidateBlock candidate =
        service.createCandidateBlock(parent.getTimestamp() + 1, COINBASE, EXTRA_DATA);

    final boolean published =
        service.publishBlock(candidate.getBlockHeader(), candidate.getBlockBody());

    assertThat(published).isTrue();
    final MutableBlockchain blockchain = setup.getBlockchain();
    assertThat(blockchain.getChainHeadBlockNumber()).isEqualTo(parent.getNumber() + 1);
    assertThat(blockchain.getChainHeadHeader().getCoinbase()).isEqualTo(COINBASE);
    verify(blockBroadcaster).propagate(any(Block.class), any(Difficulty.class));
  }

  @Test
  void refusesToPublishAnInvalidBlock() {
    final BlockHeader parent = setup.getBlockchain().getChainHeadHeader();
    final CandidateBlock candidate =
        service.createCandidateBlock(parent.getTimestamp() + 1, COINBASE, EXTRA_DATA);
    final BlockHeader tamperedHeader =
        BlockHeaderBuilder.fromHeader((BlockHeader) candidate.getBlockHeader())
            .gasUsed(((BlockHeader) candidate.getBlockHeader()).getGasUsed() + 1)
            .blockHeaderFunctions(
                ScheduleBasedBlockHeaderFunctions.create(setup.getProtocolSchedule()))
            .buildBlockHeader();

    final boolean published = service.publishBlock(tamperedHeader, candidate.getBlockBody());

    assertThat(published).isFalse();
    assertThat(setup.getBlockchain().getChainHeadBlockNumber()).isEqualTo(parent.getNumber());
    verify(blockBroadcaster, never()).propagate(any(), any());
  }
}
