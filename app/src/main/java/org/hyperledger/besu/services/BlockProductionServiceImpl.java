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

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.ethereum.ProtocolContext;
import org.hyperledger.besu.ethereum.blockcreation.GenericBlockCreator;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.ImmutableMiningConfiguration;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.eth.manager.EthScheduler;
import org.hyperledger.besu.ethereum.eth.sync.BlockBroadcaster;
import org.hyperledger.besu.ethereum.eth.transactions.TransactionPool;
import org.hyperledger.besu.ethereum.mainnet.HeaderValidationMode;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.plugin.data.BlockBody;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.CandidateBlock;
import org.hyperledger.besu.plugin.services.BlockProductionService;

import org.apache.tuweni.bytes.Bytes;

/** Implementation of {@link BlockProductionService}. */
public class BlockProductionServiceImpl implements BlockProductionService {

  private final ProtocolContext protocolContext;
  private final ProtocolSchedule protocolSchedule;
  private final TransactionPool transactionPool;
  private final EthScheduler ethScheduler;
  private final BlockBroadcaster blockBroadcaster;
  private final MiningConfiguration miningConfiguration;

  /**
   * Creates a new BlockProductionServiceImpl.
   *
   * @param protocolContext the protocol context
   * @param protocolSchedule the protocol schedule
   * @param transactionPool the transaction pool to select transactions from
   * @param ethScheduler the scheduler used during transaction selection
   * @param blockBroadcaster the broadcaster used to announce published blocks to peers
   * @param miningConfiguration the node's mining configuration
   */
  public BlockProductionServiceImpl(
      final ProtocolContext protocolContext,
      final ProtocolSchedule protocolSchedule,
      final TransactionPool transactionPool,
      final EthScheduler ethScheduler,
      final BlockBroadcaster blockBroadcaster,
      final MiningConfiguration miningConfiguration) {
    this.protocolContext = protocolContext;
    this.protocolSchedule = protocolSchedule;
    this.transactionPool = transactionPool;
    this.ethScheduler = ethScheduler;
    this.blockBroadcaster = blockBroadcaster;
    this.miningConfiguration = miningConfiguration;
  }

  @Override
  public CandidateBlock createCandidateBlock(
      final long timestamp, final Address coinbase, final Bytes extraData) {
    final org.hyperledger.besu.ethereum.core.BlockHeader parentHeader =
        protocolContext.getBlockchain().getChainHeadHeader();
    final GenericBlockCreator blockCreator =
        new GenericBlockCreator(
            blockMiningConfiguration(coinbase, extraData),
            (blockTimestamp, pendingHeader) -> coinbase,
            parent -> extraData,
            transactionPool,
            protocolContext,
            protocolSchedule,
            ethScheduler);
    final Block block = blockCreator.createBlock(timestamp, parentHeader).getBlock();
    return new CandidateBlock(block.getHeader(), block.getBody());
  }

  @Override
  public boolean publishBlock(final BlockHeader blockHeader, final BlockBody blockBody) {
    final Block block =
        new Block(
            (org.hyperledger.besu.ethereum.core.BlockHeader) blockHeader,
            (org.hyperledger.besu.ethereum.core.BlockBody) blockBody);
    final var importResult =
        protocolSchedule
            .getByBlockHeader(block.getHeader())
            .getBlockImporter()
            .importBlock(protocolContext, block, HeaderValidationMode.FULL);
    if (!importResult.isImported()) {
      return false;
    }
    protocolContext
        .getBlockchain()
        .getTotalDifficultyByHash(block.getHash())
        .ifPresent(totalDifficulty -> blockBroadcaster.propagate(block, totalDifficulty));
    return true;
  }

  /**
   * Builds a per-block mining configuration so that the candidate coinbase and extra data flow into
   * the block header without mutating the shared node configuration.
   */
  private MiningConfiguration blockMiningConfiguration(
      final Address coinbase, final Bytes extraData) {
    return ImmutableMiningConfiguration.builder()
        .transactionSelectionService(miningConfiguration.getTransactionSelectionService())
        .mutableInitValues(
            ImmutableMiningConfiguration.MutableInitValues.builder()
                .coinbase(coinbase)
                .extraData(extraData)
                .minTransactionGasPrice(miningConfiguration.getMinTransactionGasPrice())
                .minPriorityFeePerGas(miningConfiguration.getMinPriorityFeePerGas())
                .targetGasLimit(miningConfiguration.getTargetGasLimit())
                .build())
        .build();
  }
}
