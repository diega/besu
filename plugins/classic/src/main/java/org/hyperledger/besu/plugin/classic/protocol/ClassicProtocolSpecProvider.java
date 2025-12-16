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

import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getAghartaBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getAtlantisBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getDefuseDifficultyBombBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getDieHardBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getEcip1015BlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getGothamBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getMagnetoBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getMystiqueBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getPhoenixBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getSpiralBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getThanosBlockNumber;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.ethereum.mainnet.BalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.MilestoneRegistry;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecProvider;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import java.math.BigInteger;
import java.util.Optional;

public class ClassicProtocolSpecProvider implements ProtocolSpecProvider {

  private static final BigInteger ETC_MAINNET_CHAIN_ID = BigInteger.valueOf(61);
  private static final BigInteger MORDOR_CHAIN_ID = BigInteger.valueOf(63);

  @Override
  public boolean supports(final Optional<BigInteger> chainId) {
    return chainId
        .map(id -> id.equals(ETC_MAINNET_CHAIN_ID) || id.equals(MORDOR_CHAIN_ID))
        .orElse(false);
  }

  @Override
  public void registerMilestones(final MilestoneRegistry registry) {
    final GenesisConfigOptions config = registry.getConfig();
    final EvmConfiguration evmConfiguration = registry.getEvmConfiguration();
    final MetricsSystem metricsSystem = registry.getMetricsSystem();
    final BalConfiguration balConfiguration = registry.getBalConfiguration();
    final boolean isParallelTxProcessingEnabled = registry.isParallelTxProcessingEnabled();
    final Optional<BigInteger> chainId = registry.getChainId();
    final boolean isRevertReasonEnabled = registry.isRevertReasonEnabled();

    registry.addBlockNumberMilestone(
        ClassicHardforkId.CLASSIC_TANGERINE_WHISTLE,
        getEcip1015BlockNumber(config),
        () ->
            tangerineWhistleDefinition(
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.DIE_HARD,
        getDieHardBlockNumber(config),
        () ->
            ClassicProtocolSpecs.dieHardDefinition(
                chainId,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.GOTHAM,
        getGothamBlockNumber(config),
        () ->
            ClassicProtocolSpecs.gothamDefinition(
                chainId,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.DEFUSE_DIFFICULTY_BOMB,
        getDefuseDifficultyBombBlockNumber(config),
        () ->
            ClassicProtocolSpecs.defuseDifficultyBombDefinition(
                chainId,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.ATLANTIS,
        getAtlantisBlockNumber(config),
        () ->
            ClassicProtocolSpecs.atlantisDefinition(
                chainId,
                isRevertReasonEnabled,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.AGHARTA,
        getAghartaBlockNumber(config),
        () ->
            ClassicProtocolSpecs.aghartaDefinition(
                chainId,
                isRevertReasonEnabled,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.PHOENIX,
        getPhoenixBlockNumber(config),
        () ->
            ClassicProtocolSpecs.phoenixDefinition(
                chainId,
                isRevertReasonEnabled,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.THANOS,
        getThanosBlockNumber(config),
        () ->
            ClassicProtocolSpecs.thanosDefinition(
                chainId,
                isRevertReasonEnabled,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.MAGNETO,
        getMagnetoBlockNumber(config),
        () ->
            ClassicProtocolSpecs.magnetoDefinition(
                chainId,
                isRevertReasonEnabled,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.MYSTIQUE,
        getMystiqueBlockNumber(config),
        () ->
            ClassicProtocolSpecs.mystiqueDefinition(
                chainId,
                isRevertReasonEnabled,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));

    registry.addBlockNumberMilestone(
        ClassicHardforkId.SPIRAL,
        getSpiralBlockNumber(config),
        () ->
            ClassicProtocolSpecs.spiralDefinition(
                chainId,
                isRevertReasonEnabled,
                config,
                evmConfiguration,
                isParallelTxProcessingEnabled,
                balConfiguration,
                metricsSystem));
  }

  private ProtocolSpecBuilder tangerineWhistleDefinition(
      final GenesisConfigOptions config,
      final EvmConfiguration evmConfiguration,
      final boolean isParallelTxProcessingEnabled,
      final BalConfiguration balConfiguration,
      final MetricsSystem metricsSystem) {
    return org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSpecs.tangerineWhistleDefinition(
        config, evmConfiguration, isParallelTxProcessingEnabled, balConfiguration, metricsSystem);
  }
}
