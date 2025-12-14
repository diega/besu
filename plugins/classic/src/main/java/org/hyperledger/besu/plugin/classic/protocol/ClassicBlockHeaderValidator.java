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
package org.hyperledger.besu.plugin.classic.protocol;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.mainnet.BlockHeaderValidator;
import org.hyperledger.besu.ethereum.mainnet.EpochCalculator;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockHeaderValidator;
import org.hyperledger.besu.ethereum.mainnet.PoWHasher;
import org.hyperledger.besu.ethereum.mainnet.headervalidationrules.ConstantFieldValidationRule;

public final class ClassicBlockHeaderValidator {

  public static final Hash CLASSIC_FORK_BLOCK_HASH =
      Hash.fromHexString("0x94365e3a8c0b35089c1d1195081fe7489b528a84b22199c916180db8b28ade7f");

  private static final long CLASSIC_FORK_BLOCK_NUMBER = 1_920_000L;

  private ClassicBlockHeaderValidator() {
    // utility class
  }

  public static BlockHeaderValidator.Builder createClassicValidator(final PoWHasher hasher) {
    return MainnetBlockHeaderValidator.createPgaBlockHeaderValidator(
            new EpochCalculator.DefaultEpochCalculator(), hasher)
        .addRule(
            new ConstantFieldValidationRule<>(
                "hash",
                h ->
                    h.getNumber() == CLASSIC_FORK_BLOCK_NUMBER
                        ? h.getBlockHash()
                        : CLASSIC_FORK_BLOCK_HASH,
                CLASSIC_FORK_BLOCK_HASH));
  }

  public static boolean validateHeaderForClassicFork(final BlockHeader header) {
    return header.getNumber() != CLASSIC_FORK_BLOCK_NUMBER
        || header.getHash().equals(CLASSIC_FORK_BLOCK_HASH);
  }
}
