package org.hyperledger.besu.components;

import org.hyperledger.besu.ethereum.p2p.network.NetworkRunner;
import org.hyperledger.besu.nat.NatMethod;
import org.hyperledger.besu.nat.NatService;

import dagger.Component;

@Component(modules = {
    NatServiceModule.class,
    NetworkRunnerModule.class,
})
public interface EthereumClassicComponents {

  NatService natService();
  NetworkRunner networkRunner();
}
