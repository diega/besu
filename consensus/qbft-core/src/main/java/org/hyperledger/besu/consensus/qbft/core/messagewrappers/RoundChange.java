/*
 * Copyright ConsenSys AG.
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
package org.hyperledger.besu.consensus.qbft.core.messagewrappers;

import org.hyperledger.besu.consensus.common.bft.messagewrappers.BftMessage;
import org.hyperledger.besu.consensus.common.bft.payload.SignedData;
import org.hyperledger.besu.consensus.qbft.core.payload.PreparePayload;
import org.hyperledger.besu.consensus.qbft.core.payload.PreparedRoundMetadata;
import org.hyperledger.besu.consensus.qbft.core.payload.RoundChangePayload;
import org.hyperledger.besu.consensus.qbft.core.types.QbftBlock;
import org.hyperledger.besu.consensus.qbft.core.types.QbftBlockCodec;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.rlp.RLPInput;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

/** The Round change payload message. */
public class RoundChange extends BftMessage<RoundChangePayload> {

  private final Optional<QbftBlock> proposedBlock;
  private final QbftBlockCodec blockEncoder;
  private final List<SignedData<PreparePayload>> prepares;

  /**
   * Instantiates a new Round change.
   *
   * @param payload the payload
   * @param proposedBlock the proposed block
   * @param blockEncoder the qbft block encoder
   * @param prepares the prepares
   */
  public RoundChange(
      final SignedData<RoundChangePayload> payload,
      final Optional<QbftBlock> proposedBlock,
      final QbftBlockCodec blockEncoder,
      final List<SignedData<PreparePayload>> prepares) {
    super(payload);
    this.proposedBlock = proposedBlock;
    this.blockEncoder = blockEncoder;
    this.prepares = prepares;
  }

  /**
   * Gets proposed block.
   *
   * @return the proposed block
   */
  public Optional<QbftBlock> getProposedBlock() {
    return proposedBlock;
  }

  /**
   * Gets list of Prepare payload signed data.
   *
   * @return the prepares
   */
  public List<SignedData<PreparePayload>> getPrepares() {
    return prepares;
  }

  /**
   * Gets prepared round metadata.
   *
   * @return the prepared round metadata
   */
  public Optional<PreparedRoundMetadata> getPreparedRoundMetadata() {
    return getPayload().getPreparedRoundMetadata();
  }

  /**
   * Gets prepared round.
   *
   * @return the prepared round
   */
  public Optional<Integer> getPreparedRound() {
    return getPayload().getPreparedRoundMetadata().map(PreparedRoundMetadata::getPreparedRound);
  }

  @Override
  public Bytes encode() {
    final BytesValueRLPOutput rlpOut = new BytesValueRLPOutput();
    rlpOut.startList();
    getSignedPayload().writeTo(rlpOut);
    proposedBlock.ifPresentOrElse(pb -> blockEncoder.writeTo(pb, rlpOut), rlpOut::writeEmptyList);
    rlpOut.writeList(prepares, SignedData::writeTo);
    rlpOut.endList();
    return rlpOut.encoded();
  }

  /**
   * Decode.
   *
   * @param data the data
   * @param blockEncoder the qbft block encoder
   * @return the round change
   */
  public static RoundChange decode(final Bytes data, final QbftBlockCodec blockEncoder) {

    final RLPInput rlpIn = RLP.input(data);
    rlpIn.enterList();
    final SignedData<RoundChangePayload> payload = readPayload(rlpIn, RoundChangePayload::readFrom);

    final Optional<QbftBlock> block;
    if (rlpIn.nextIsList() && rlpIn.nextSize() == 0) {
      rlpIn.skipNext();
      block = Optional.empty();
    } else {
      block = Optional.of(blockEncoder.readFrom(rlpIn));
    }

    final List<SignedData<PreparePayload>> prepares =
        rlpIn.readList(r -> readPayload(r, PreparePayload::readFrom));
    rlpIn.leaveList();

    return new RoundChange(payload, block, blockEncoder, prepares);
  }
}
