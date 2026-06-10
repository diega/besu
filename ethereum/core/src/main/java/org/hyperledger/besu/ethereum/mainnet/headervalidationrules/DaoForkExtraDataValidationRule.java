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

import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.mainnet.DetachedBlockHeaderValidationRule;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockHeaderValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Requires the {@code extraData} of the blocks in the DAO fork range to carry the canonical
 * "dao-hard-fork" marker, so that pro-fork nodes reject blocks mined on the no-fork side of the
 * split. Blocks outside {@code [daoForkBlock, daoForkBlock + 10)} are not affected. This mirrors
 * how go-ethereum ({@code misc.VerifyDAOHeaderExtraData}) and Nethermind ({@code
 * HeaderValidator.ValidateExtraData}) implement the check: a block-number-ranged rule rather than
 * dedicated fork specs.
 */
public class DaoForkExtraDataValidationRule implements DetachedBlockHeaderValidationRule {
  private static final Logger LOG = LoggerFactory.getLogger(DaoForkExtraDataValidationRule.class);

  /** The number of consecutive blocks from the DAO fork block that must carry the marker. */
  public static final long DAO_EXTRA_DATA_RANGE = 10L;

  private final long daoForkBlock;

  /**
   * Creates the rule.
   *
   * @param daoForkBlock the block at which the DAO fork activates
   */
  public DaoForkExtraDataValidationRule(final long daoForkBlock) {
    this.daoForkBlock = daoForkBlock;
  }

  @Override
  public boolean validate(final BlockHeader header, final BlockHeader parent) {
    final long number = header.getNumber();
    if (number < daoForkBlock || number >= daoForkBlock + DAO_EXTRA_DATA_RANGE) {
      return true;
    }
    if (!header.getExtraData().equals(MainnetBlockHeaderValidator.DAO_EXTRA_DATA)) {
      LOG.info(
          "Invalid block header: block {} is within the DAO fork extra-data range but its"
              + " extraData ({}) is not the DAO fork marker ({}).",
          number,
          header.getExtraData(),
          MainnetBlockHeaderValidator.DAO_EXTRA_DATA);
      return false;
    }
    return true;
  }
}
