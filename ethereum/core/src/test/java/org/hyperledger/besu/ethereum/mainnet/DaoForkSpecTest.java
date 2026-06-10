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

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

/**
 * Pins the DAO fork wiring through the real protocol spec the schedule serves — not just the rule
 * and processor in isolation: the spec at the fork block carries the block-number-guarded
 * processor, and its header validator enforces the extra-data marker exactly within {@code
 * [daoForkBlock, daoForkBlock + 10)}, even though the same spec stays in force beyond the window.
 */
public class DaoForkSpecTest {

  private static final long DAO_FORK_BLOCK = 1_000L;
  private static final Bytes NON_DAO_EXTRA_DATA = Bytes.fromHexString("0x1234");

  private final ProtocolSchedule schedule =
      MainnetProtocolSchedule.fromConfig(
          GenesisConfig.fromConfig(
                  "{\"config\": {\"homesteadBlock\": 0, \"daoForkBlock\": 1000,"
                      + " \"byzantiumBlock\": 2000, \"chainId\": 1234}}")
              .getConfigOptions(),
          EvmConfiguration.DEFAULT,
          MiningConfiguration.MINING_DISABLED,
          new BadBlockManager(),
          false,
          BalConfiguration.DEFAULT,
          new NoOpMetricsSystem());

  @Test
  public void theSpecAtTheForkBlockCarriesTheGuardedDaoProcessor() {
    assertThat(specAt(DAO_FORK_BLOCK).getBlockProcessor())
        .isInstanceOf(MainnetProtocolSpecs.DaoBlockProcessor.class);
    // The same schedule returns an unwrapped processor once the next fork takes over.
    assertThat(specAt(2_000).getBlockProcessor())
        .isNotInstanceOf(MainnetProtocolSpecs.DaoBlockProcessor.class);
  }

  @Test
  public void theSpecValidatorEnforcesTheExtraDataMarkerOnlyWithinTheWindow() {
    // Within the window the marker is required...
    assertThat(validate(DAO_FORK_BLOCK, MainnetBlockHeaderValidator.DAO_EXTRA_DATA)).isTrue();
    assertThat(validate(DAO_FORK_BLOCK, NON_DAO_EXTRA_DATA)).isFalse();
    assertThat(validate(DAO_FORK_BLOCK + 9, NON_DAO_EXTRA_DATA)).isFalse();
    // ...and from daoForkBlock + 10 the very same spec accepts arbitrary extra data.
    assertThat(validate(DAO_FORK_BLOCK + 10, NON_DAO_EXTRA_DATA)).isTrue();
  }

  private boolean validate(final long blockNumber, final Bytes extraData) {
    final BlockHeader parent =
        new BlockHeaderTestFixture().number(blockNumber - 1).gasLimit(8_000_000).buildHeader();
    final BlockHeader header =
        new BlockHeaderTestFixture()
            .number(blockNumber)
            .parentHash(parent.getHash())
            .timestamp(parent.getTimestamp() + 1)
            .gasLimit(8_000_000)
            .extraData(extraData)
            .buildHeader();
    return specAt(blockNumber)
        .getBlockHeaderValidator()
        .validateHeader(header, parent, null, HeaderValidationMode.LIGHT_DETACHED_ONLY);
  }

  private ProtocolSpec specAt(final long blockNumber) {
    return schedule.getByBlockHeader(
        new BlockHeaderTestFixture().number(blockNumber).buildHeader());
  }
}
