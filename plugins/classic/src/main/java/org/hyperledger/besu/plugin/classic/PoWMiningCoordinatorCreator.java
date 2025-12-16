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
package org.hyperledger.besu.plugin.classic;

import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getThanosBlockNumber;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.ethereum.blockcreation.DefaultBlockScheduler;
import org.hyperledger.besu.ethereum.blockcreation.MiningCoordinator;
import org.hyperledger.besu.ethereum.blockcreation.MiningCoordinatorContext;
import org.hyperledger.besu.ethereum.mainnet.EpochCalculator;
import org.hyperledger.besu.plugin.classic.mining.PoWMinerExecutor;
import org.hyperledger.besu.plugin.classic.mining.PoWMiningCoordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoWMiningCoordinatorCreator {

  private static final Logger LOG = LoggerFactory.getLogger(PoWMiningCoordinatorCreator.class);

  private static final long MINIMUM_SECONDS_SINCE_PARENT = 1L;
  private static final long TIMESTAMP_TOLERANCE_S = 15L;

  private final PoWMiningCLIOptions cliOptions;

  public PoWMiningCoordinatorCreator(final PoWMiningCLIOptions cliOptions) {
    this.cliOptions = cliOptions;
  }

  public MiningCoordinator create(final MiningCoordinatorContext context) {
    LOG.info("Creating PoW mining coordinator");

    final EpochCalculator epochCalculator =
        createEpochCalculator(context.getGenesisConfigOptions());

    final DefaultBlockScheduler blockScheduler =
        new DefaultBlockScheduler(
            MINIMUM_SECONDS_SINCE_PARENT, TIMESTAMP_TOLERANCE_S, context.getClock());

    final PoWMinerExecutor executor =
        new PoWMinerExecutor(
            context.getProtocolContext(),
            context.getProtocolSchedule(),
            context.getTransactionPool(),
            context.getMiningConfiguration(),
            blockScheduler,
            epochCalculator,
            context.getEthProtocolManager().ethContext().getScheduler(),
            cliOptions.getPowJobTimeToLive(),
            cliOptions.getMaxOmmersDepth());

    final PoWMiningCoordinator coordinator =
        new PoWMiningCoordinator(
            context.getProtocolContext().getBlockchain(),
            executor,
            context.getSyncState(),
            cliOptions.getRemoteSealersLimit(),
            cliOptions.getRemoteSealersTimeToLive());

    coordinator.addMinedBlockObserver(context.getEthProtocolManager());

    if (context.getMiningConfiguration().isMiningEnabled()) {
      coordinator.enable();
    }

    return coordinator;
  }

  private static EpochCalculator createEpochCalculator(final GenesisConfigOptions config) {
    if (getThanosBlockNumber(config).isPresent()) {
      LOG.info("Thanos hard fork detected, using ECIP-1099 epoch calculator");
      return new EpochCalculator.Ecip1099EpochCalculator();
    }
    return new EpochCalculator.DefaultEpochCalculator();
  }
}
