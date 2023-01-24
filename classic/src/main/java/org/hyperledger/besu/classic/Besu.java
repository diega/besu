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
package org.hyperledger.besu.classic;

import org.hyperledger.besu.cli.DefaultCommandValues;
import org.hyperledger.besu.cli.options.stable.JsonRPCHttpOptions;
import org.hyperledger.besu.cli.options.stable.MetricsProtocolOptions;
import org.hyperledger.besu.cli.options.stable.NodePrivateKeyFileOption;
import org.hyperledger.besu.cli.options.stable.P2PDiscoveryOptions;
import org.hyperledger.besu.cli.options.unstable.NatOptions;
import org.hyperledger.besu.components.DaggerEthereumClassicComponents;
import org.hyperledger.besu.components.EthereumClassicComponents;
import org.hyperledger.besu.components.NatServiceModule;
import org.hyperledger.besu.components.NetworkRunnerModule;
import org.hyperledger.besu.ethereum.p2p.network.NetworkRunner;
import org.hyperledger.besu.nat.NatMethod;

import org.hyperledger.besu.nat.NatService;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;

import java.nio.file.Path;

import static org.hyperledger.besu.cli.DefaultCommandValues.getDefaultBesuDataPath;

@CommandLine.Command(
    name = "classic",
    mixinStandardHelpOptions = true,
    version = "23.1.0",
    description = "Runs a Hyperledger Besu Classic node.")
public class Besu implements Runnable {

  @Mixin NatOptions natOptions;
  @Mixin JsonRPCHttpOptions jsonRPCHttpOptions;
  @Mixin P2PDiscoveryOptions p2PDiscoveryOptions;
  @Mixin MetricsProtocolOptions metricsOptions;
  @Mixin NodePrivateKeyFileOption nodePrivateKeyFileOption;


  @CommandLine.Option(
      names = {"--nat-method"},
      description =
          "Specify the NAT circumvention method to be used, possible values are ${COMPLETION-CANDIDATES}."
              + " NONE disables NAT functionality. (default: ${DEFAULT-VALUE})")
  private final NatMethod natMethod = DefaultCommandValues.DEFAULT_NAT_METHOD;

  @CommandLine.Option(
      names = {"--data-path"},
      paramLabel = MANDATORY_PATH_FORMAT_HELP,
      description = "The path to Besu data directory (default: ${DEFAULT-VALUE})")
  final Path dataPath = getDefaultBesuDataPath(this);


  public static void main(final String[] args) {
    int exitCode = new CommandLine(new Besu()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    EthereumClassicComponents classicComponents = DaggerEthereumClassicComponents.builder()
        .natServiceModule(new NatServiceModule(natMethod, natOptions, p2PDiscoveryOptions, jsonRPCHttpOptions))
        .networkRunnerModule(new NetworkRunnerModule(p2PDiscoveryOptions, nodePrivateKeyFileOption))
        .build();

    NatService natService = classicComponents.natService();
    natService.start();

    try (NetworkRunner networkRunner = classicComponents.networkRunner()) {
      networkRunner.start();
      networkRunner.awaitStop();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    } finally {
      natService.stop();
    }
  }
}
