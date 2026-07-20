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
package org.hyperledger.besu.plugin.services;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.plugin.Unstable;
import org.hyperledger.besu.plugin.data.BlockBody;
import org.hyperledger.besu.plugin.data.BlockHeader;
import org.hyperledger.besu.plugin.data.CandidateBlock;

import org.apache.tuweni.bytes.Bytes;

/**
 * A service that lets plugins drive the production of blocks whose sealing happens outside of
 * Besu's built-in consensus mechanisms.
 *
 * <p>Networks whose consensus is provided by a plugin (for example externally sealed networks) need
 * two capabilities that are otherwise internal to Besu: assembling a candidate block on top of the
 * chain head, and getting a sealed block into the chain and out to peers. This service provides
 * exactly those two operations.
 *
 * <p>All consensus-specific behaviour flows from the node's protocol schedule: the candidate
 * difficulty comes from the schedule's difficulty calculator, gas limits from its gas limit
 * calculator, and block rewards from its block processor. The service itself is consensus agnostic.
 */
@Unstable
public interface BlockProductionService extends BesuService {

  /**
   * Builds an unsealed candidate block on top of the current chain head.
   *
   * <p>Transactions are selected from the transaction pool. The candidate header carries the
   * difficulty prescribed by the protocol schedule and placeholder seal fields (zero nonce and zero
   * mix hash); sealing them is the caller's responsibility.
   *
   * @param timestamp the timestamp of the candidate block, must be greater than the parent's
   * @param coinbase the beneficiary of the block reward
   * @param extraData the extra data to embed in the candidate header
   * @return the unsealed candidate block
   */
  CandidateBlock createCandidateBlock(long timestamp, Address coinbase, Bytes extraData);

  /**
   * Atomically imports a locally produced, fully sealed block and announces it to peers.
   *
   * <p>The block is imported with full validation as prescribed by the node's protocol schedule.
   * Only if the import succeeds is the block announced to peers; a block that fails validation is
   * neither imported nor announced. Whether the imported block becomes the new chain head is
   * decided by the blockchain's fork choice rule, exactly as if the block had been received from a
   * peer.
   *
   * @param blockHeader the sealed block header
   * @param blockBody the block body
   * @return true if the block was imported and announced, false if it failed validation
   */
  boolean publishBlock(BlockHeader blockHeader, BlockBody blockBody);
}
