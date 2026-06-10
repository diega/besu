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
package org.hyperledger.besu.ethereum.mainnet.headervalidationrules;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockHeaderTestFixture;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockHeaderValidator;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;

public class DaoForkExtraDataValidationRuleTest {

  private static final long DAO_FORK_BLOCK = 1_920_000L;
  private static final Bytes NON_DAO_EXTRA_DATA = Bytes.fromHexString("0x1234");

  private final DaoForkExtraDataValidationRule rule =
      new DaoForkExtraDataValidationRule(DAO_FORK_BLOCK);

  @Test
  public void requiresTheDaoMarkerThroughoutTheForkRange() {
    for (long offset = 0; offset < DaoForkExtraDataValidationRule.DAO_EXTRA_DATA_RANGE; offset++) {
      assertThat(
              rule.validate(
                  header(DAO_FORK_BLOCK + offset, MainnetBlockHeaderValidator.DAO_EXTRA_DATA),
                  null))
          .isTrue();
      assertThat(rule.validate(header(DAO_FORK_BLOCK + offset, NON_DAO_EXTRA_DATA), null))
          .isFalse();
    }
  }

  @Test
  public void ignoresExtraDataOutsideTheForkRange() {
    assertThat(rule.validate(header(DAO_FORK_BLOCK - 1, NON_DAO_EXTRA_DATA), null)).isTrue();
    assertThat(
            rule.validate(
                header(
                    DAO_FORK_BLOCK + DaoForkExtraDataValidationRule.DAO_EXTRA_DATA_RANGE,
                    NON_DAO_EXTRA_DATA),
                null))
        .isTrue();
  }

  private BlockHeader header(final long number, final Bytes extraData) {
    return new BlockHeaderTestFixture().number(number).extraData(extraData).buildHeader();
  }
}
