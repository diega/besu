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
package org.hyperledger.besu.plugin.classic.peervalidation;

import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;
import org.hyperledger.besu.ethereum.eth.manager.peertask.PeerTaskExecutor;
import org.hyperledger.besu.ethereum.eth.manager.peertask.PeerTaskExecutorResponseCode;
import org.hyperledger.besu.ethereum.eth.manager.peertask.PeerTaskExecutorResult;
import org.hyperledger.besu.ethereum.eth.manager.peertask.task.GetHeadersFromPeerTask;
import org.hyperledger.besu.ethereum.eth.peervalidation.PeerValidator;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.plugin.classic.protocol.ClassicBlockHeaderValidator;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassicForkPeerValidator implements PeerValidator {
  private static final Logger LOG = LoggerFactory.getLogger(ClassicForkPeerValidator.class);
  private static final long DEFAULT_CHAIN_HEIGHT_ESTIMATION_BUFFER = 10L;

  private final ProtocolSchedule protocolSchedule;
  private final PeerTaskExecutor peerTaskExecutor;
  private final long blockNumber;
  private final long chainHeightEstimationBuffer;

  public ClassicForkPeerValidator(
      final ProtocolSchedule protocolSchedule,
      final PeerTaskExecutor peerTaskExecutor,
      final long blockNumber) {
    this.protocolSchedule = protocolSchedule;
    this.peerTaskExecutor = peerTaskExecutor;
    this.blockNumber = blockNumber;
    this.chainHeightEstimationBuffer = DEFAULT_CHAIN_HEIGHT_ESTIMATION_BUFFER;
  }

  @Override
  public boolean canBeValidated(final EthPeer ethPeer) {
    return ethPeer.chainState().getEstimatedHeight() >= (blockNumber + chainHeightEstimationBuffer);
  }

  @Override
  public Duration nextValidationCheckTimeout(final EthPeer ethPeer) {
    if (!ethPeer.chainState().hasEstimatedHeight()) {
      return Duration.ofSeconds(30);
    }
    final long distanceToBlock = blockNumber - ethPeer.chainState().getEstimatedHeight();
    if (distanceToBlock < 100_000L) {
      return Duration.ofMinutes(1);
    }
    return Duration.ofMinutes(10);
  }

  @Override
  public CompletableFuture<Boolean> validatePeer(
      final EthContext ethContext, final EthPeer ethPeer) {
    return ethContext
        .getScheduler()
        .scheduleServiceTask(
            () -> {
              final GetHeadersFromPeerTask task =
                  new GetHeadersFromPeerTask(
                      blockNumber,
                      1,
                      0,
                      GetHeadersFromPeerTask.Direction.FORWARD,
                      protocolSchedule);
              final PeerTaskExecutorResult<List<BlockHeader>> taskResult =
                  peerTaskExecutor.executeAgainstPeer(task, ethPeer);

              if (taskResult.responseCode() != PeerTaskExecutorResponseCode.SUCCESS
                  || taskResult.result().isEmpty()) {
                return CompletableFuture.completedFuture(false);
              }

              final List<BlockHeader> headers = taskResult.result().get();
              if (headers.isEmpty()) {
                LOG.debug(
                    "Peer {} is invalid because Classic fork block ({}) is unavailable.",
                    ethPeer,
                    blockNumber);
                return CompletableFuture.completedFuture(false);
              }

              final BlockHeader header = headers.getFirst();
              final boolean validClassicBlock =
                  ClassicBlockHeaderValidator.validateHeaderForClassicFork(header);
              if (!validClassicBlock) {
                LOG.info(
                    "Peer {} is invalid because Classic block ({}) is invalid.",
                    ethPeer,
                    blockNumber);
              }
              return CompletableFuture.completedFuture(validClassicBlock);
            });
  }
}
