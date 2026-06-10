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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.Block;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.core.InMemoryKeyValueStorageProvider;
import org.hyperledger.besu.evm.worldstate.WorldUpdater;
import org.hyperledger.besu.plugin.services.worldstate.MutableWorldState;

import org.junit.jupiter.api.Test;

public class DaoBlockProcessorTest {

  private static final long DAO_FORK_BLOCK = 1_920_000L;
  // The first entry of daoAddresses.json and the refund contract it drains into.
  private static final Address DAO_ACCOUNT =
      Address.fromHexString("0xd4fe7bc31cedb7bfb8a345f31e668033056b2728");
  private static final Address DAO_REFUND_CONTRACT =
      Address.fromHexString("0xbf4ed7b27f1d666546e30d74d50d173d20bca754");

  private final BlockProcessor wrapped = mock(BlockProcessor.class);
  private final MainnetProtocolSpecs.DaoBlockProcessor processor =
      new MainnetProtocolSpecs.DaoBlockProcessor(wrapped, DAO_FORK_BLOCK);

  @Test
  public void appliesTheIrregularStateChangeAtTheForkBlock() {
    final MutableWorldState worldState = worldStateWithDaoBalance(Wei.of(42));

    processor.processBlock(null, null, worldState, block(DAO_FORK_BLOCK));

    assertThat(worldState.get(DAO_ACCOUNT).getBalance()).isEqualTo(Wei.ZERO);
    assertThat(worldState.get(DAO_REFUND_CONTRACT).getBalance()).isEqualTo(Wei.of(42));
    verify(wrapped)
        .processBlock(
            any(),
            any(),
            any(),
            any(Block.class),
            any(AbstractBlockProcessor.PreprocessingFunction.class));
  }

  @Test
  public void leavesEveryOtherBlockUntouched() {
    final MutableWorldState worldState = worldStateWithDaoBalance(Wei.of(42));

    processor.processBlock(null, null, worldState, block(DAO_FORK_BLOCK + 1));

    assertThat(worldState.get(DAO_ACCOUNT).getBalance()).isEqualTo(Wei.of(42));
    assertThat(worldState.get(DAO_REFUND_CONTRACT)).isNull();
    verify(wrapped)
        .processBlock(
            any(),
            any(),
            any(),
            any(Block.class),
            any(AbstractBlockProcessor.PreprocessingFunction.class));
  }

  private MutableWorldState worldStateWithDaoBalance(final Wei balance) {
    final MutableWorldState worldState = InMemoryKeyValueStorageProvider.createInMemoryWorldState();
    final WorldUpdater updater = worldState.updater();
    updater.createAccount(DAO_ACCOUNT, 0, balance);
    updater.commit();
    return worldState;
  }

  private Block block(final long number) {
    return new Block(new BlockHeaderTestFixture().number(number).buildHeader(), BlockBody.empty());
  }
}
