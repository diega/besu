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
package org.hyperledger.besu.ethereum.mainnet.milestones;

import static org.hyperledger.besu.ethereum.mainnet.milestones.MilestoneDefinition.createBlockNumberMilestone;
import static org.hyperledger.besu.ethereum.mainnet.milestones.MilestoneDefinition.createTimestampMilestone;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.datatypes.HardforkId;
import org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId;
import org.hyperledger.besu.ethereum.mainnet.BalConfiguration;
import org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSpecFactory;
import org.hyperledger.besu.ethereum.mainnet.MilestoneRegistry;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;
import org.hyperledger.besu.evm.internal.EvmConfiguration;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;

/** Provides milestone definitions for the Ethereum Mainnet and Classic networks. */
public class MilestoneDefinitions {

  /**
   * Creates and populates a milestone registry with Ethereum Mainnet milestones.
   *
   * <p>Classic milestones can be contributed by plugins via the MilestoneRegistry interface.
   *
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   * @param evmConfiguration the EVM configuration
   * @param metricsSystem the metrics system
   * @param balConfiguration the BAL configuration
   * @param isParallelTxProcessingEnabled whether parallel tx processing is enabled
   * @param chainId the chain ID
   * @return a populated milestone registry
   */
  public static MilestoneRegistry createMilestoneRegistry(
      final MainnetProtocolSpecFactory specFactory,
      final GenesisConfigOptions config,
      final EvmConfiguration evmConfiguration,
      final MetricsSystem metricsSystem,
      final BalConfiguration balConfiguration,
      final boolean isParallelTxProcessingEnabled,
      final Optional<BigInteger> chainId) {
    MilestoneRegistry registry =
        new MilestoneRegistryImpl(
            specFactory,
            config,
            evmConfiguration,
            metricsSystem,
            balConfiguration,
            isParallelTxProcessingEnabled,
            chainId);
    registerMainnetMilestones(registry, specFactory, config);
    // NOTE: Classic milestones will be contributed by ClassicProtocolSpecProvider plugin
    // For now, we still register them here until the plugin is ready
    registerClassicMilestones(registry, specFactory, config);
    return registry;
  }

  /**
   * Creates milestone definitions for Mainnet and Classic networks.
   *
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   * @return a list of milestone definitions
   * @deprecated Use {@link #createMilestoneRegistry} instead
   */
  @Deprecated
  public static List<MilestoneDefinition> createMilestoneDefinitions(
      final MainnetProtocolSpecFactory specFactory, final GenesisConfigOptions config) {
    List<MilestoneDefinition> milestones = new ArrayList<>();
    milestones.addAll(createMainnetMilestoneDefinitions(specFactory, config));
    milestones.addAll(createClassicMilestoneDefinitions(specFactory, config));
    return milestones;
  }

  /**
   * Registers milestone definitions for the Mainnet networks to the registry.
   *
   * @param registry the milestone registry to register to
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   */
  private static void registerMainnetMilestones(
      final MilestoneRegistry registry,
      final MainnetProtocolSpecFactory specFactory,
      final GenesisConfigOptions config) {
    // Add block number milestones first
    registerMainnetBlockNumberMilestones(registry, specFactory, config);
    // Then add timestamp milestones
    registerMainnetTimestampMilestones(registry, specFactory, config);
  }

  /**
   * Registers block number milestones for the Mainnet to the registry.
   *
   * @param registry the milestone registry to register to
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   */
  private static void registerMainnetBlockNumberMilestones(
      final MilestoneRegistry registry,
      final MainnetProtocolSpecFactory specFactory,
      final GenesisConfigOptions config) {
    registry.addBlockNumberMilestone(
        MainnetHardforkId.FRONTIER, OptionalLong.of(0), specFactory::frontierDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.HOMESTEAD,
        config.getHomesteadBlockNumber(),
        specFactory::homesteadDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.TANGERINE_WHISTLE,
        config.getTangerineWhistleBlockNumber(),
        specFactory::tangerineWhistleDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.SPURIOUS_DRAGON,
        config.getSpuriousDragonBlockNumber(),
        specFactory::spuriousDragonDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.BYZANTIUM,
        config.getByzantiumBlockNumber(),
        specFactory::byzantiumDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.CONSTANTINOPLE,
        config.getConstantinopleBlockNumber(),
        specFactory::constantinopleDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.PETERSBURG,
        config.getPetersburgBlockNumber(),
        specFactory::petersburgDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.ISTANBUL,
        config.getIstanbulBlockNumber(),
        specFactory::istanbulDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.MUIR_GLACIER,
        config.getMuirGlacierBlockNumber(),
        specFactory::muirGlacierDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.BERLIN, config.getBerlinBlockNumber(), specFactory::berlinDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.LONDON, config.getLondonBlockNumber(), specFactory::londonDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.ARROW_GLACIER,
        config.getArrowGlacierBlockNumber(),
        specFactory::arrowGlacierDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.GRAY_GLACIER,
        config.getGrayGlacierBlockNumber(),
        specFactory::grayGlacierDefinition);
    registry.addBlockNumberMilestone(
        MainnetHardforkId.PARIS,
        config.getMergeNetSplitBlockNumber(),
        specFactory::parisDefinition);
  }

  /**
   * Registers timestamp milestones for the Mainnet to the registry.
   *
   * @param registry the milestone registry to register to
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   */
  private static void registerMainnetTimestampMilestones(
      final MilestoneRegistry registry,
      final MainnetProtocolSpecFactory specFactory,
      final GenesisConfigOptions config) {
    registry.addTimestampMilestone(
        MainnetHardforkId.SHANGHAI, config.getShanghaiTime(), specFactory::shanghaiDefinition);
    registry.addTimestampMilestone(
        MainnetHardforkId.CANCUN, config.getCancunTime(), specFactory::cancunDefinition);
    registry.addTimestampMilestone(
        MainnetHardforkId.CANCUN_EOF, config.getCancunEOFTime(), specFactory::cancunEOFDefinition);
    registry.addTimestampMilestone(
        MainnetHardforkId.PRAGUE, config.getPragueTime(), specFactory::pragueDefinition);
    registry.addTimestampMilestone(
        MainnetHardforkId.OSAKA, config.getOsakaTime(), specFactory::osakaDefinition);
    registry.addTimestampMilestone(
        MainnetHardforkId.BPO1, config.getBpo1Time(), specFactory::bpo1Definition);
    registry.addTimestampMilestone(
        MainnetHardforkId.BPO2, config.getBpo2Time(), specFactory::bpo2Definition);
    registry.addTimestampMilestone(
        MainnetHardforkId.BPO3, config.getBpo3Time(), specFactory::bpo3Definition);
    registry.addTimestampMilestone(
        MainnetHardforkId.BPO4, config.getBpo4Time(), specFactory::bpo4Definition);
    registry.addTimestampMilestone(
        MainnetHardforkId.BPO5, config.getBpo5Time(), specFactory::bpo5Definition);
    registry.addTimestampMilestone(
        MainnetHardforkId.AMSTERDAM, config.getAmsterdamTime(), specFactory::amsterdamDefinition);
    registry.addTimestampMilestone(
        MainnetHardforkId.FUTURE_EIPS,
        config.getFutureEipsTime(),
        specFactory::futureEipsDefinition);
    registry.addTimestampMilestone(
        MainnetHardforkId.EXPERIMENTAL_EIPS,
        config.getExperimentalEipsTime(),
        specFactory::experimentalEipsDefinition);
  }

  /**
   * Registers Classic milestones to the registry. This will be moved to the Classic plugin.
   *
   * @param registry the milestone registry to register to
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   */
  private static void registerClassicMilestones(
      final MilestoneRegistry registry,
      final MainnetProtocolSpecFactory specFactory,
      final GenesisConfigOptions config) {
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.CLASSIC_TANGERINE_WHISTLE,
        config.getEcip1015BlockNumber(),
        specFactory::tangerineWhistleDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.DIE_HARD,
        config.getDieHardBlockNumber(),
        specFactory::dieHardDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.GOTHAM,
        config.getGothamBlockNumber(),
        specFactory::gothamDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.DEFUSE_DIFFICULTY_BOMB,
        config.getDefuseDifficultyBombBlockNumber(),
        specFactory::defuseDifficultyBombDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.ATLANTIS,
        config.getAtlantisBlockNumber(),
        specFactory::atlantisDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.AGHARTA,
        config.getAghartaBlockNumber(),
        specFactory::aghartaDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.PHOENIX,
        config.getPhoenixBlockNumber(),
        specFactory::phoenixDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.THANOS,
        config.getThanosBlockNumber(),
        specFactory::thanosDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.MAGNETO,
        config.getMagnetoBlockNumber(),
        specFactory::magnetoDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.MYSTIQUE,
        config.getMystiqueBlockNumber(),
        specFactory::mystiqueDefinition);
    registry.addBlockNumberMilestone(
        HardforkId.ClassicHardforkId.SPIRAL,
        config.getSpiralBlockNumber(),
        specFactory::spiralDefinition);
  }

  /**
   * Returns the milestone definitions for the Classic network.
   *
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   * @return a list of milestone definitions for the Classic network
   */
  private static List<MilestoneDefinition> createClassicMilestoneDefinitions(
      final MainnetProtocolSpecFactory specFactory, final GenesisConfigOptions config) {
    return List.of(
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.CLASSIC_TANGERINE_WHISTLE,
            config.getEcip1015BlockNumber(),
            specFactory::tangerineWhistleDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.DIE_HARD,
            config.getDieHardBlockNumber(),
            specFactory::dieHardDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.GOTHAM,
            config.getGothamBlockNumber(),
            specFactory::gothamDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.DEFUSE_DIFFICULTY_BOMB,
            config.getDefuseDifficultyBombBlockNumber(),
            specFactory::defuseDifficultyBombDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.ATLANTIS,
            config.getAtlantisBlockNumber(),
            specFactory::atlantisDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.AGHARTA,
            config.getAghartaBlockNumber(),
            specFactory::aghartaDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.PHOENIX,
            config.getPhoenixBlockNumber(),
            specFactory::phoenixDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.THANOS,
            config.getThanosBlockNumber(),
            specFactory::thanosDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.MAGNETO,
            config.getMagnetoBlockNumber(),
            specFactory::magnetoDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.MYSTIQUE,
            config.getMystiqueBlockNumber(),
            specFactory::mystiqueDefinition),
        createBlockNumberMilestone(
            HardforkId.ClassicHardforkId.SPIRAL,
            config.getSpiralBlockNumber(),
            specFactory::spiralDefinition));
  }

  /**
   * Creates the milestone definitions for the Mainnet networks.
   *
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   * @return a list of milestone definitions for the Mainnet
   */
  private static List<MilestoneDefinition> createMainnetMilestoneDefinitions(
      final MainnetProtocolSpecFactory specFactory, final GenesisConfigOptions config) {
    List<MilestoneDefinition> milestones = new ArrayList<>();
    // Add block number milestones first
    milestones.addAll(createMainnetBlockNumberMilestones(specFactory, config));
    // Then add timestamp milestones
    milestones.addAll(createMainnetTimestampMilestones(specFactory, config));
    return milestones;
  }

  /**
   * Creates block number milestones for the Mainnet.
   *
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   * @return a list of block number milestones
   */
  private static List<MilestoneDefinition> createMainnetBlockNumberMilestones(
      final MainnetProtocolSpecFactory specFactory, final GenesisConfigOptions config) {
    return List.of(
        createBlockNumberMilestone(
            MainnetHardforkId.FRONTIER, OptionalLong.of(0), specFactory::frontierDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.HOMESTEAD,
            config.getHomesteadBlockNumber(),
            specFactory::homesteadDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.TANGERINE_WHISTLE,
            config.getTangerineWhistleBlockNumber(),
            specFactory::tangerineWhistleDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.SPURIOUS_DRAGON,
            config.getSpuriousDragonBlockNumber(),
            specFactory::spuriousDragonDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.BYZANTIUM,
            config.getByzantiumBlockNumber(),
            specFactory::byzantiumDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.CONSTANTINOPLE,
            config.getConstantinopleBlockNumber(),
            specFactory::constantinopleDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.PETERSBURG,
            config.getPetersburgBlockNumber(),
            specFactory::petersburgDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.ISTANBUL,
            config.getIstanbulBlockNumber(),
            specFactory::istanbulDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.MUIR_GLACIER,
            config.getMuirGlacierBlockNumber(),
            specFactory::muirGlacierDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.BERLIN, config.getBerlinBlockNumber(), specFactory::berlinDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.LONDON, config.getLondonBlockNumber(), specFactory::londonDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.ARROW_GLACIER,
            config.getArrowGlacierBlockNumber(),
            specFactory::arrowGlacierDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.GRAY_GLACIER,
            config.getGrayGlacierBlockNumber(),
            specFactory::grayGlacierDefinition),
        createBlockNumberMilestone(
            MainnetHardforkId.PARIS,
            config.getMergeNetSplitBlockNumber(),
            specFactory::parisDefinition));
  }

  /**
   * Creates timestamp milestones for the Mainnet.
   *
   * @param specFactory the protocol spec factory
   * @param config the genesis config options
   * @return a list of timestamp milestones
   */
  private static List<MilestoneDefinition> createMainnetTimestampMilestones(
      final MainnetProtocolSpecFactory specFactory, final GenesisConfigOptions config) {
    return List.of(
        createTimestampMilestone(
            MainnetHardforkId.SHANGHAI, config.getShanghaiTime(), specFactory::shanghaiDefinition),
        createTimestampMilestone(
            MainnetHardforkId.CANCUN, config.getCancunTime(), specFactory::cancunDefinition),
        createTimestampMilestone(
            MainnetHardforkId.CANCUN_EOF,
            config.getCancunEOFTime(),
            specFactory::cancunEOFDefinition),
        createTimestampMilestone(
            MainnetHardforkId.PRAGUE, config.getPragueTime(), specFactory::pragueDefinition),
        createTimestampMilestone(
            MainnetHardforkId.OSAKA, config.getOsakaTime(), specFactory::osakaDefinition),
        createTimestampMilestone(
            MainnetHardforkId.BPO1, config.getBpo1Time(), specFactory::bpo1Definition),
        createTimestampMilestone(
            MainnetHardforkId.BPO2, config.getBpo2Time(), specFactory::bpo2Definition),
        createTimestampMilestone(
            MainnetHardforkId.BPO3, config.getBpo3Time(), specFactory::bpo3Definition),
        createTimestampMilestone(
            MainnetHardforkId.BPO4, config.getBpo4Time(), specFactory::bpo4Definition),
        createTimestampMilestone(
            MainnetHardforkId.BPO5, config.getBpo5Time(), specFactory::bpo5Definition),
        createTimestampMilestone(
            MainnetHardforkId.AMSTERDAM,
            config.getAmsterdamTime(),
            specFactory::amsterdamDefinition),
        createTimestampMilestone(
            MainnetHardforkId.FUTURE_EIPS,
            config.getFutureEipsTime(),
            specFactory::futureEipsDefinition),
        createTimestampMilestone(
            MainnetHardforkId.EXPERIMENTAL_EIPS,
            config.getExperimentalEipsTime(),
            specFactory::experimentalEipsDefinition));
  }

  /**
   * Default implementation of MilestoneRegistry that collects milestone definitions.
   *
   * <p>This implementation is used to collect milestones from Mainnet, Classic, and future plugin
   * sources. It provides a mutable registry that can be extended by plugins.
   */
  private static class MilestoneRegistryImpl implements MilestoneRegistry {
    private final List<MilestoneDefinition> milestones = new ArrayList<>();
    private final MainnetProtocolSpecFactory specFactory;
    private final GenesisConfigOptions config;
    private final EvmConfiguration evmConfiguration;
    private final MetricsSystem metricsSystem;
    private final BalConfiguration balConfiguration;
    private final boolean isParallelTxProcessingEnabled;
    private final Optional<BigInteger> chainId;

    MilestoneRegistryImpl(
        final MainnetProtocolSpecFactory specFactory,
        final GenesisConfigOptions config,
        final EvmConfiguration evmConfiguration,
        final MetricsSystem metricsSystem,
        final BalConfiguration balConfiguration,
        final boolean isParallelTxProcessingEnabled,
        final Optional<BigInteger> chainId) {
      this.specFactory = specFactory;
      this.config = config;
      this.evmConfiguration = evmConfiguration;
      this.metricsSystem = metricsSystem;
      this.balConfiguration = balConfiguration;
      this.isParallelTxProcessingEnabled = isParallelTxProcessingEnabled;
      this.chainId = chainId;
    }

    @Override
    public void addBlockNumberMilestone(
        final HardforkId hardforkId,
        final OptionalLong blockNumber,
        final Supplier<ProtocolSpecBuilder> specBuilder) {
      milestones.add(createBlockNumberMilestone(hardforkId, blockNumber, specBuilder));
    }

    @Override
    public void addTimestampMilestone(
        final HardforkId hardforkId,
        final OptionalLong timestamp,
        final Supplier<ProtocolSpecBuilder> specBuilder) {
      milestones.add(createTimestampMilestone(hardforkId, timestamp, specBuilder));
    }

    @Override
    public List<MilestoneDefinition> getMilestones() {
      return milestones;
    }

    @Override
    public MainnetProtocolSpecFactory getSpecFactory() {
      return specFactory;
    }

    @Override
    public GenesisConfigOptions getConfig() {
      return config;
    }

    @Override
    public EvmConfiguration getEvmConfiguration() {
      return evmConfiguration;
    }

    @Override
    public MetricsSystem getMetricsSystem() {
      return metricsSystem;
    }

    @Override
    public BalConfiguration getBalConfiguration() {
      return balConfiguration;
    }

    @Override
    public boolean isParallelTxProcessingEnabled() {
      return isParallelTxProcessingEnabled;
    }

    @Override
    public Optional<BigInteger> getChainId() {
      return chainId;
    }

    @Override
    public boolean isRevertReasonEnabled() {
      return specFactory.isRevertReasonEnabled();
    }
  }
}
