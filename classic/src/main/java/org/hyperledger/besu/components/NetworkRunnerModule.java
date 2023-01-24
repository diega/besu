package org.hyperledger.besu.components;

import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;
import org.hyperledger.besu.cli.options.stable.NodePrivateKeyFileOption;
import org.hyperledger.besu.cli.options.stable.P2PDiscoveryOptions;
import org.hyperledger.besu.crypto.KeyPair;
import org.hyperledger.besu.crypto.KeyPairSecurityModule;
import org.hyperledger.besu.crypto.KeyPairUtil;
import org.hyperledger.besu.ethereum.eth.EthProtocol;
import org.hyperledger.besu.ethereum.p2p.network.NetworkRunner;
import org.hyperledger.besu.ethereum.p2p.network.NoopP2PNetwork;
import org.hyperledger.besu.ethereum.p2p.network.ProtocolManager;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.metrics.vertx.VertxMetricsAdapterFactory;
import org.hyperledger.besu.plugin.services.BesuConfiguration;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.SecurityModuleService;
import org.hyperledger.besu.plugin.services.securitymodule.SecurityModule;
import org.hyperledger.besu.services.SecurityModuleServiceImpl;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hyperledger.besu.cli.DefaultCommandValues.DEFAULT_SECURITY_MODULE;

@Module
public class NetworkRunnerModule {

  private final P2PDiscoveryOptions p2PDiscoveryOptions;
  private final NodePrivateKeyFileOption nodePrivateKeyFileOption;

  public NetworkRunnerModule(final P2PDiscoveryOptions p2PDiscoveryOptions, final NodePrivateKeyFileOption nodePrivateKeyFileOption) {
    this.p2PDiscoveryOptions = p2PDiscoveryOptions;
    this.nodePrivateKeyFileOption = nodePrivateKeyFileOption;
  }

  @Provides public NetworkRunner getNetworkRunner(final List<ProtocolManager> protocolManagers,
                                                  final MetricsSystem metricsSystem,
                                                  final NetworkRunner.NetworkBuilder network) {
    return NetworkRunner.builder()
        .protocolManagers(protocolManagers)
        .subProtocols(List.of(EthProtocol.get()))
        .network(network)
        .metricsSystem(metricsSystem)
        .build();
  }

  @Provides public List<ProtocolManager> protocolManagers() {
    return Collections.emptyList();
  }

  @Provides public NetworkRunner.NetworkBuilder networkBuilder(final Vertx vertx) {
    if (p2PDiscoveryOptions.isP2pEnabled()) {
      return caps -> new NoopP2PNetwork();
    } else {
      return caps -> new NoopP2PNetwork();
    }
  }

  @Provides public MetricsSystem metricsSystem() {
    return new NoOpMetricsSystem();
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

  public SecurityModuleService securityModuleService(final SecurityModule defaultSecurityModule) {
    final SecurityModuleServiceImpl securityModuleService = new SecurityModuleServiceImpl();
    securityModuleService.register(
        DEFAULT_SECURITY_MODULE, Suppliers.memoize(() -> defaultSecurityModule));
    return securityModuleService;
  }

  public SecurityModule defaultSecurityModule(
      final BesuConfiguration besuConfiguration, final BesuProperties properties) {
    return new KeyPairSecurityModule(
        loadKeyPair(nodePrivateKeyFileOption.getNodePrivateKeyFile(), besuConfiguration.getDataPath()));
  }

  public KeyPair loadKeyPair(final File nodePrivateKeyFile, final Path dataPath) {
    return KeyPairUtil.loadKeyPair(
        Optional.ofNullable(nodePrivateKeyFile)
            .orElseGet(() -> KeyPairUtil.getDefaultKeyFile(dataPath)));
  }

  @Provides public Vertx vertx(final MetricsSystem metricsSystem) {
    return Vertx.vertx(new VertxOptions()
        .setPreferNativeTransport(true)
        .setMetricsOptions(
            new MetricsOptions()
                .setEnabled(true)
                .setFactory(new VertxMetricsAdapterFactory(metricsSystem))));
  }
}
