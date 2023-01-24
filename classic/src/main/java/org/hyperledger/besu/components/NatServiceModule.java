package org.hyperledger.besu.components;

import dagger.Module;
import dagger.Provides;
import org.hyperledger.besu.cli.options.stable.JsonRPCHttpOptions;
import org.hyperledger.besu.cli.options.stable.P2PDiscoveryOptions;
import org.hyperledger.besu.cli.options.unstable.NatOptions;
import org.hyperledger.besu.nat.NatMethod;
import org.hyperledger.besu.nat.NatService;
import org.hyperledger.besu.nat.core.NatManager;
import org.hyperledger.besu.nat.docker.DockerDetector;
import org.hyperledger.besu.nat.docker.DockerNatManager;
import org.hyperledger.besu.nat.kubernetes.KubernetesDetector;
import org.hyperledger.besu.nat.upnp.UpnpNatManager;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

@Module
public class NatServiceModule {

  private final NatMethod natMethod;
  private final NatOptions natOptions;
  private final P2PDiscoveryOptions p2PDiscoveryOptions;
  private final JsonRPCHttpOptions jsonRPCHttpOptions;

  @Inject
  public NatServiceModule(final NatMethod natMethod, final NatOptions natOptions, final P2PDiscoveryOptions p2PDiscoveryOptions, final JsonRPCHttpOptions jsonRPCHttpOptions) {
    this.natMethod = natMethod;
    this.natOptions = natOptions;
    this.p2PDiscoveryOptions = p2PDiscoveryOptions;
    this.jsonRPCHttpOptions = jsonRPCHttpOptions;
  }

  @Provides
  public Optional<NatManager> getNatManager() {
    NatMethod detectedNatMethod = natMethod;
    if (natMethod == NatMethod.AUTO) {
      detectedNatMethod =
          NatService.autoDetectNatMethod(new KubernetesDetector(), new DockerDetector());
    }
    switch (detectedNatMethod) {
      case UPNP:
        return Optional.of(new UpnpNatManager());
      case DOCKER:
        return Optional.of(
            new DockerNatManager(
                p2PDiscoveryOptions.getP2pHost(),
                p2PDiscoveryOptions.getP2pPort(),
                jsonRPCHttpOptions.getRpcHttpPort()));
      case NONE:
      default:
        return Optional.empty();
    }
  }

  @Provides @Named("fallbackEnabled")
  public boolean fallbackEnabled() {
    return natOptions.getNatMethodFallbackEnabled();
  }

}
