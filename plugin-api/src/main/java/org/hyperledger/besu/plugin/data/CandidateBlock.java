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
package org.hyperledger.besu.plugin.data;

/**
 * An unsealed candidate block produced by the {@link
 * org.hyperledger.besu.plugin.services.BlockProductionService}.
 *
 * <p>The header carries placeholder seal fields (zero nonce and zero mix hash); sealing them is the
 * caller's responsibility.
 */
public class CandidateBlock {
  final BlockHeader blockHeader;
  final BlockBody blockBody;

  /**
   * Constructs a new CandidateBlock instance.
   *
   * @param blockHeader the unsealed block header
   * @param blockBody the block body
   */
  public CandidateBlock(final BlockHeader blockHeader, final BlockBody blockBody) {
    this.blockHeader = blockHeader;
    this.blockBody = blockBody;
  }

  /**
   * Gets the unsealed block header.
   *
   * @return the unsealed block header
   */
  public BlockHeader getBlockHeader() {
    return blockHeader;
  }

  /**
   * Gets the block body.
   *
   * @return the block body
   */
  public BlockBody getBlockBody() {
    return blockBody;
  }
}
