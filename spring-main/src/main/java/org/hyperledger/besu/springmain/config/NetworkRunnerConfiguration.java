/*
 * Copyright Hyperledger Besu Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 *  the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.springmain.config;

import static org.hyperledger.besu.cli.DefaultCommandValues.DEFAULT_SECURITY_MODULE;

import org.hyperledger.besu.BesuInfo;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.crypto.KeyPair;
import org.hyperledger.besu.crypto.KeyPairSecurityModule;
import org.hyperledger.besu.crypto.KeyPairUtil;
import org.hyperledger.besu.crypto.NodeKey;
import org.hyperledger.besu.ethereum.chain.MutableBlockchain;
import org.hyperledger.besu.ethereum.eth.EthProtocol;
import org.hyperledger.besu.ethereum.eth.SnapProtocol;
import org.hyperledger.besu.ethereum.eth.manager.EthProtocolManager;
import org.hyperledger.besu.ethereum.eth.manager.snap.SnapProtocolManager;
import org.hyperledger.besu.ethereum.p2p.config.DiscoveryConfiguration;
import org.hyperledger.besu.ethereum.p2p.config.NetworkingConfiguration;
import org.hyperledger.besu.ethereum.p2p.config.RlpxConfiguration;
import org.hyperledger.besu.ethereum.p2p.config.SubProtocolConfiguration;
import org.hyperledger.besu.ethereum.p2p.network.DefaultP2PNetwork;
import org.hyperledger.besu.ethereum.p2p.network.NetworkRunner;
import org.hyperledger.besu.ethereum.p2p.network.NoopP2PNetwork;
import org.hyperledger.besu.ethereum.p2p.network.ProtocolManager;
import org.hyperledger.besu.ethereum.p2p.permissions.PeerPermissions;
import org.hyperledger.besu.ethereum.p2p.rlpx.connections.netty.TLSConfiguration;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.SubProtocol;
import org.hyperledger.besu.ethereum.storage.StorageProvider;
import org.hyperledger.besu.nat.NatService;
import org.hyperledger.besu.plugin.services.BesuConfiguration;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.SecurityModuleService;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModule;
import org.hyperledger.besu.services.SecurityModuleServiceImpl;
import org.hyperledger.besu.springmain.config.properties.BesuProperties;
import org.hyperledger.besu.springmain.config.properties.P2PProperties;
import org.hyperledger.besu.util.number.Fraction;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Suppliers;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Bean;

public class NetworkRunnerConfiguration {

  @Bean
  public NetworkRunner networkRunner(
      final List<SubProtocol> subProtocols,
      final List<ProtocolManager> protocolManagers,
      final MetricsSystem metricsSystem,
      final NetworkRunner.NetworkBuilder activeNetwork,
      final NetworkRunner.NetworkBuilder inactiveNetwork,
      final P2PProperties p2PProperties) {

    return NetworkRunner.builder()
        .protocolManagers(protocolManagers)
        .subProtocols(subProtocols)
        .network(p2PProperties.isEnabled() ? activeNetwork : inactiveNetwork)
        .metricsSystem(metricsSystem)
        .build();
  }

  @Bean
  public List<SubProtocol> subProtocols() {
    return List.of(EthProtocol.get());
  }

  @Bean
  private NetworkRunner.NetworkBuilder activeNetwork(
      final Vertx vertx,
      final NetworkingConfiguration networkingConfiguration,
      final PeerPermissions peerPermissions,
      final MetricsSystem metricsSystem,
      final NatService natService,
      final MutableBlockchain blockchain,
      final Optional<TLSConfiguration> p2pTLSConfiguration,
      final GenesisConfigOptions getGenesisConfigOptions,
      final NodeKey nodeKey,
      final StorageProvider storageProvider,
      final P2PProperties p2PProperties) {
    return caps ->
        DefaultP2PNetwork.builder()
            .vertx(vertx)
            .nodeKey(nodeKey)
            .config(networkingConfiguration)
            .peerPermissions(peerPermissions)
            .metricsSystem(metricsSystem)
            .supportedCapabilities(caps)
            .natService(natService)
            .randomPeerPriority(p2PProperties.isRandomPeerPriority())
            .p2pTLSConfiguration(p2pTLSConfiguration)
            .blockchain(blockchain)
            .blockNumberForks(getGenesisConfigOptions.getForkBlockNumbers())
            .storageProvider(storageProvider)
            .build();
  }

  @Bean
  private NetworkingConfiguration networkingConfiguration(
      final RlpxConfiguration rlpxConfiguration,
      final DiscoveryConfiguration discoveryConfiguration) {
    final NetworkingConfiguration networkingConfiguration = NetworkingConfiguration.create();
    networkingConfiguration.setRlpx(rlpxConfiguration).setDiscovery(discoveryConfiguration);
    return networkingConfiguration;
  }

  @Bean
  public DiscoveryConfiguration discoveryConfiguration(final P2PProperties p2PProperties) {
    return DiscoveryConfiguration.create()
        .setBindHost(p2PProperties.getP2pInterface())
        .setBindPort(p2PProperties.getPort())
        .setAdvertisedHost(p2PProperties.getHost());
  }

  @Bean
  public RlpxConfiguration rlpxConfiguration(
      final SubProtocolConfiguration subProtocolConfiguration,
      final P2PProperties p2PProperties,
      final BesuProperties besuProperties) {
    return RlpxConfiguration.create()
        .setBindHost(p2PProperties.getP2pInterface())
        .setBindPort(p2PProperties.getPort())
        .setPeerUpperBound(p2PProperties.getMaxPeers())
        .setPeerLowerBound(p2PProperties.getMinPeers())
        .setSupportedProtocols(subProtocolConfiguration.getSubProtocols())
        .setClientId(BesuInfo.nodeName(Optional.ofNullable(besuProperties.getIdentityString())))
        .setLimitRemoteWireConnectionsEnabled(besuProperties.isLimitRemoteWireConnectionsEnabled())
        .setFractionRemoteWireConnectionsAllowed(
            Fraction.fromPercentage(p2PProperties.getMaxRemoteConnectionsPercentage()).getValue());
  }

  @Bean
  public SubProtocolConfiguration subProtocolConfiguration(
      final EthProtocolManager ethProtocolManager,
      final Optional<SnapProtocolManager> maybeSnapProtocolManager) {
    final SubProtocolConfiguration subProtocolConfiguration =
        new SubProtocolConfiguration().withSubProtocol(EthProtocol.get(), ethProtocolManager);
    maybeSnapProtocolManager.ifPresent(
        snapProtocolManager -> {
          subProtocolConfiguration.withSubProtocol(SnapProtocol.get(), snapProtocolManager);
        });
    return subProtocolConfiguration;
  }

  @Bean
  public NodeKey nodeKey(final SecurityModule securityModule) {
    return new NodeKey(securityModule);
  }

  @Bean
  public SecurityModule securityModule(
      final SecurityModuleService securityModuleService, final BesuProperties besuProperties) {
    return securityModuleService
        .getByName(besuProperties.getSecurityModuleName())
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Security Module not found: " + besuProperties.getSecurityModuleName()))
        .get();
  }

  @Bean
  public SecurityModuleService securityModuleService(final SecurityModule defaultSecurityModule) {
    final SecurityModuleServiceImpl securityModuleService = new SecurityModuleServiceImpl();
    securityModuleService.register(
        DEFAULT_SECURITY_MODULE, Suppliers.memoize(() -> defaultSecurityModule));
    return securityModuleService;
  }

  @Bean
  public SecurityModule defaultSecurityModule(
      final BesuConfiguration besuConfiguration, final BesuProperties properties) {
    return new KeyPairSecurityModule(
        loadKeyPair(properties.getNodePrivateKeyFile(), besuConfiguration.getDataPath()));
  }

  public KeyPair loadKeyPair(final File nodePrivateKeyFile, final Path dataPath) {
    return KeyPairUtil.loadKeyPair(
        Optional.ofNullable(nodePrivateKeyFile)
            .orElseGet(() -> KeyPairUtil.getDefaultKeyFile(dataPath)));
  }

  @Bean
  public NetworkRunner.NetworkBuilder inactiveNetwork() {
    return caps -> new NoopP2PNetwork();
  }

  @Bean
  NetworkingConfiguration networkingConfiguration() {
    return NetworkingConfiguration.create();
  }
}
