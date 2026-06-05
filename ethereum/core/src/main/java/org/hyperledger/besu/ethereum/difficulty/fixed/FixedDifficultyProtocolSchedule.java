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
package org.hyperledger.besu.ethereum.difficulty.fixed;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.ethereum.chain.BadBlockManager;
import org.hyperledger.besu.ethereum.core.MiningConfiguration;
import org.hyperledger.besu.ethereum.mainnet.BalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSchedule;
import org.hyperledger.besu.ethereum.mainnet.ProtocolScheduleBuilder;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecAdapters;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A ProtocolSchedule which behaves similarly to pre-merge MainNet, but with a much reduced
 * difficulty.
 */
public class FixedDifficultyProtocolSchedule {

  public static ProtocolSchedule create(
      final GenesisConfigOptions config,
      final boolean isRevertReasonEnabled,
      final EvmConfiguration evmConfiguration,
      final MiningConfiguration miningConfiguration,
      final BadBlockManager badBlockManager,
      final boolean isParallelTxProcessingEnabled,
      final BalConfiguration balConfiguration,
      final MetricsSystem metricsSystem) {
    return create(
        config,
        isRevertReasonEnabled,
        evmConfiguration,
        miningConfiguration,
        badBlockManager,
        isParallelTxProcessingEnabled,
        balConfiguration,
        metricsSystem,
        new ProtocolSpecAdapters(new HashMap<>()));
  }

  /**
   * Create a fixed-difficulty protocol schedule, folding in plugin-contributed spec adapters
   * composed over the fixed-difficulty calculator.
   *
   * @param config the genesis config options
   * @param isRevertReasonEnabled whether to store the revert reason
   * @param evmConfiguration the evm configuration
   * @param miningConfiguration the mining configuration
   * @param badBlockManager the bad block manager
   * @param isParallelTxProcessingEnabled whether parallel tx processing is enabled
   * @param balConfiguration the block access list configuration
   * @param metricsSystem the metrics system
   * @param pluginSpecAdapters plugin-contributed spec adapters to fold into the schedule
   * @return a configured fixed-difficulty protocol schedule
   */
  public static ProtocolSchedule create(
      final GenesisConfigOptions config,
      final boolean isRevertReasonEnabled,
      final EvmConfiguration evmConfiguration,
      final MiningConfiguration miningConfiguration,
      final BadBlockManager badBlockManager,
      final boolean isParallelTxProcessingEnabled,
      final BalConfiguration balConfiguration,
      final MetricsSystem metricsSystem,
      final ProtocolSpecAdapters pluginSpecAdapters) {
    final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> modifiers = new HashMap<>();
    modifiers.put(
        0L, builder -> builder.difficultyCalculator(FixedDifficultyCalculators.calculator(config)));
    pluginSpecAdapters.stream()
        .forEach(
            entry ->
                modifiers.merge(
                    entry.getKey(), entry.getValue(), (base, next) -> base.andThen(next)));
    return new ProtocolScheduleBuilder(
            config,
            Optional.empty(),
            new ProtocolSpecAdapters(modifiers),
            isRevertReasonEnabled,
            evmConfiguration,
            miningConfiguration,
            badBlockManager,
            isParallelTxProcessingEnabled,
            balConfiguration,
            metricsSystem)
        .createProtocolSchedule();
  }

  public static ProtocolSchedule create(
      final GenesisConfigOptions config,
      final EvmConfiguration evmConfiguration,
      final MiningConfiguration miningConfiguration,
      final BadBlockManager badBlockManager,
      final boolean isParallelTxProcessingEnabled,
      final BalConfiguration balConfiguration,
      final MetricsSystem metricsSystem) {
    return create(
        config,
        false,
        evmConfiguration,
        miningConfiguration,
        badBlockManager,
        isParallelTxProcessingEnabled,
        balConfiguration,
        metricsSystem);
  }
}
