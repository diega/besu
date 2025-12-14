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

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.ethereum.eth.manager.peertask.PeerTaskExecutor;
import org.hyperledger.besu.ethereum.eth.peervalidation.PeerValidator;
import org.hyperledger.besu.ethereum.eth.peervalidation.PeerValidatorProvider;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;

import java.util.OptionalLong;

public class ClassicPeerValidatorProvider implements PeerValidatorProvider {

  @Override
  public PeerValidator createPeerValidator(
      final ProtocolSchedule protocolSchedule,
      final PeerTaskExecutor peerTaskExecutor,
      final GenesisConfigOptions config) {
    final OptionalLong classicBlock = config.getClassicForkBlock();
    if (classicBlock.isPresent()) {
      return new ClassicForkPeerValidator(
          protocolSchedule, peerTaskExecutor, classicBlock.getAsLong());
    }
    return null;
  }
}
