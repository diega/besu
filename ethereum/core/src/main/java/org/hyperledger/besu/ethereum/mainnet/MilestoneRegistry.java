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

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.datatypes.HardforkId;
import org.hyperledger.besu.ethereum.mainnet.milestones.MilestoneDefinition;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;

/**
 * Registry for milestone definitions. Allows plugins to register custom hardfork milestones that
 * will be included in the protocol schedule.
 *
 * <p>This interface is used by both the core (for Mainnet milestones) and plugins (for custom
 * network milestones like Ethereum Classic).
 */
public interface MilestoneRegistry {

  /**
   * Adds a block number-based milestone to the registry.
   *
   * @param hardforkId the hardfork identifier
   * @param blockNumber the block number at which this milestone activates, or empty if not
   *     activated
   * @param specBuilder a supplier that creates the protocol spec builder for this milestone
   */
  void addBlockNumberMilestone(
      HardforkId hardforkId, OptionalLong blockNumber, Supplier<ProtocolSpecBuilder> specBuilder);

  /**
   * Adds a timestamp-based milestone to the registry.
   *
   * @param hardforkId the hardfork identifier
   * @param timestamp the timestamp at which this milestone activates, or empty if not activated
   * @param specBuilder a supplier that creates the protocol spec builder for this milestone
   */
  void addTimestampMilestone(
      HardforkId hardforkId, OptionalLong timestamp, Supplier<ProtocolSpecBuilder> specBuilder);

  /**
   * Returns all registered milestones.
   *
   * @return the list of milestone definitions
   */
  List<MilestoneDefinition> getMilestones();

  /**
   * Returns the protocol spec factory used to create spec builders.
   *
   * @return the protocol spec factory
   */
  MainnetProtocolSpecFactory getSpecFactory();

  /**
   * Returns the genesis configuration options.
   *
   * @return the genesis config options
   */
  GenesisConfigOptions getConfig();

  /**
   * Returns the EVM configuration.
   *
   * @return the EVM configuration
   */
  EvmConfiguration getEvmConfiguration();

  /**
   * Returns the metrics system.
   *
   * @return the metrics system
   */
  MetricsSystem getMetricsSystem();

  /**
   * Returns the BAL configuration.
   *
   * @return the BAL configuration
   */
  BalConfiguration getBalConfiguration();

  /**
   * Returns whether parallel transaction processing is enabled.
   *
   * @return true if parallel tx processing is enabled
   */
  boolean isParallelTxProcessingEnabled();

  /**
   * Returns the chain ID.
   *
   * @return the chain ID, or empty if not set
   */
  Optional<BigInteger> getChainId();

  /**
   * Returns whether revert reason is enabled.
   *
   * @return true if revert reason is enabled
   */
  boolean isRevertReasonEnabled();
}
