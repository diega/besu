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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.eth.manager.ChainState;
import org.hyperledger.besu.ethereum.eth.manager.EthContext;
import org.hyperledger.besu.ethereum.eth.manager.EthPeer;
import org.hyperledger.besu.ethereum.eth.manager.EthScheduler;
import org.hyperledger.besu.ethereum.eth.manager.peertask.PeerTaskExecutor;
import org.hyperledger.besu.ethereum.eth.manager.peertask.PeerTaskExecutorResponseCode;
import org.hyperledger.besu.ethereum.eth.manager.peertask.PeerTaskExecutorResult;
import org.hyperledger.besu.ethereum.eth.manager.peertask.task.GetHeadersFromPeerTask;
import org.hyperledger.besu.ethereum.eth.peervalidation.PeerValidator;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.plugin.classic.protocol.ClassicBlockHeaderValidator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class ClassicForkPeerValidatorTest {

  private PeerTaskExecutor peerTaskExecutor;
  private ProtocolSchedule protocolSchedule;
  private EthContext ethContext;
  private EthScheduler ethScheduler;
  private EthPeer peer;
  private ChainState chainState;

  @BeforeEach
  @SuppressWarnings("unchecked")
  public void beforeTest() {
    peerTaskExecutor = mock(PeerTaskExecutor.class);
    protocolSchedule = mock(ProtocolSchedule.class);
    ethContext = mock(EthContext.class);
    ethScheduler = mock(EthScheduler.class);
    peer = mock(EthPeer.class);
    chainState = mock(ChainState.class);

    when(ethContext.getScheduler()).thenReturn(ethScheduler);
    when(peer.chainState()).thenReturn(chainState);

    // Make scheduler execute tasks immediately
    when(ethScheduler.scheduleServiceTask(any(Supplier.class)))
        .thenAnswer(
            invocation -> {
              Supplier<CompletableFuture<?>> task = invocation.getArgument(0);
              return task.get();
            });
  }

  private static final long CLASSIC_FORK_BLOCK_NUMBER = 1_920_000L;

  @Test
  public void validatePeer_responsivePeerOnRightSideOfFork() {
    final long classicBlockNumber = CLASSIC_FORK_BLOCK_NUMBER;

    // Mock the header to return the correct hash
    final BlockHeader headerWithCorrectHash = mock(BlockHeader.class);
    when(headerWithCorrectHash.getNumber()).thenReturn(classicBlockNumber);
    when(headerWithCorrectHash.getHash())
        .thenReturn(ClassicBlockHeaderValidator.CLASSIC_FORK_BLOCK_HASH);

    when(chainState.getEstimatedHeight()).thenReturn(classicBlockNumber + 10);

    final PeerValidator validator =
        new ClassicForkPeerValidator(protocolSchedule, peerTaskExecutor, classicBlockNumber);

    when(peerTaskExecutor.executeAgainstPeer(any(GetHeadersFromPeerTask.class), any(EthPeer.class)))
        .thenReturn(
            new PeerTaskExecutorResult<>(
                Optional.of(List.of(headerWithCorrectHash)),
                PeerTaskExecutorResponseCode.SUCCESS,
                List.of(peer)));

    final CompletableFuture<Boolean> result = validator.validatePeer(ethContext, peer);

    ArgumentCaptor<GetHeadersFromPeerTask> getHeadersTaskCaptor =
        ArgumentCaptor.forClass(GetHeadersFromPeerTask.class);
    verify(peerTaskExecutor).executeAgainstPeer(getHeadersTaskCaptor.capture(), any(EthPeer.class));
    assertThat(getHeadersTaskCaptor.getValue().getBlockNumber()).isEqualTo(classicBlockNumber);
    assertThat(result).isDone();
    assertThat(result).isCompletedWithValue(true);
  }

  @Test
  public void validatePeer_responsivePeerOnWrongSideOfFork() {
    final long classicBlockNumber = CLASSIC_FORK_BLOCK_NUMBER;

    // Create a header with a wrong hash (simulating ETH side of fork)
    final BlockHeader headerWithWrongHash = mock(BlockHeader.class);
    when(headerWithWrongHash.getNumber()).thenReturn(classicBlockNumber);
    when(headerWithWrongHash.getHash()).thenReturn(Hash.EMPTY);

    when(chainState.getEstimatedHeight()).thenReturn(classicBlockNumber + 10);

    final PeerValidator validator =
        new ClassicForkPeerValidator(protocolSchedule, peerTaskExecutor, classicBlockNumber);

    when(peerTaskExecutor.executeAgainstPeer(any(GetHeadersFromPeerTask.class), any(EthPeer.class)))
        .thenReturn(
            new PeerTaskExecutorResult<>(
                Optional.of(List.of(headerWithWrongHash)),
                PeerTaskExecutorResponseCode.SUCCESS,
                List.of(peer)));

    final CompletableFuture<Boolean> result = validator.validatePeer(ethContext, peer);

    ArgumentCaptor<GetHeadersFromPeerTask> getHeadersTaskCaptor =
        ArgumentCaptor.forClass(GetHeadersFromPeerTask.class);
    verify(peerTaskExecutor).executeAgainstPeer(getHeadersTaskCaptor.capture(), any(EthPeer.class));
    assertThat(getHeadersTaskCaptor.getValue().getBlockNumber()).isEqualTo(classicBlockNumber);
    assertThat(result).isDone();
    assertThat(result).isCompletedWithValue(false);
  }

  @Test
  public void validatePeer_responsivePeerDoesNotHaveBlockWhenPastForkHeight() {
    final long classicBlockNumber = CLASSIC_FORK_BLOCK_NUMBER;

    when(chainState.getEstimatedHeight()).thenReturn(classicBlockNumber + 10);

    final PeerValidator validator =
        new ClassicForkPeerValidator(protocolSchedule, peerTaskExecutor, classicBlockNumber);

    when(peerTaskExecutor.executeAgainstPeer(any(GetHeadersFromPeerTask.class), any(EthPeer.class)))
        .thenReturn(
            new PeerTaskExecutorResult<>(
                Optional.of(Collections.emptyList()),
                PeerTaskExecutorResponseCode.SUCCESS,
                List.of(peer)));

    final CompletableFuture<Boolean> result = validator.validatePeer(ethContext, peer);

    assertThat(result).isDone();
    assertThat(result).isCompletedWithValue(false);
  }
}
