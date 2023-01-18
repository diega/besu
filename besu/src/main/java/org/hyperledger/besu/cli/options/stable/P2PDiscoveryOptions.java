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
package org.hyperledger.besu.cli.options.stable;

import org.hyperledger.besu.cli.DefaultCommandValues;
import org.hyperledger.besu.cli.converter.PercentageConverter;
import org.hyperledger.besu.ethereum.p2p.peers.EnodeURLImpl;
import org.hyperledger.besu.util.NetworkUtility;
import org.hyperledger.besu.util.number.Fraction;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import picocli.CommandLine;

public class P2PDiscoveryOptions {

  @CommandLine.ArgGroup(validate = false, heading = "@|bold P2P Discovery Options|@%n")
  P2PDiscoveryOptionGroup optionGroup = new P2PDiscoveryOptionGroup();

  public Boolean isP2pEnabled() {
    return optionGroup.p2pEnabled;
  }

  public Boolean getPeerDiscoveryEnabled() {
    return optionGroup.peerDiscoveryEnabled;
  }

  public List<String> getBootNodes() {
    return optionGroup.bootNodes;
  }

  public String getP2pHost() {
    return optionGroup.p2pHost;
  }

  public String getP2pInterface() {
    return optionGroup.p2pInterface;
  }

  public Integer getP2pPort() {
    return optionGroup.p2pPort;
  }

  public Integer getMaxPeers() {
    return optionGroup.maxPeers;
  }

  public int getMinPeers() {
    return optionGroup.minPeers;
  }

  public void setMinPeers(final int minPeers) {
    optionGroup.minPeers = minPeers;
  }

  public Boolean isLimitRemoteWireConnectionsEnabled() {
    return optionGroup.isLimitRemoteWireConnectionsEnabled;
  }

  public Integer getMaxRemoteConnectionsPercentage() {
    return optionGroup.maxRemoteConnectionsPercentage;
  }

  public String getDiscoveryDnsUrl() {
    return optionGroup.discoveryDnsUrl;
  }

  public Boolean getRandomPeerPriority() {
    return optionGroup.randomPeerPriority;
  }

  public Collection<Bytes> getBannedNodeIds() {
    return optionGroup.bannedNodeIds;
  }

  public InetAddress autoDiscoverDefaultIP() {
    return optionGroup.autoDiscoverDefaultIP();
  }

  public static class P2PDiscoveryOptionGroup {

    // Public IP stored to prevent having to research it each time we need it.
    private InetAddress autoDiscoveredDefaultIP = null;

    // Completely disables P2P within Besu.
    @CommandLine.Option(
        names = {"--p2p-enabled"},
        description = "Enable P2P functionality (default: ${DEFAULT-VALUE})",
        arity = "1")
    private final Boolean p2pEnabled = true;

    // Boolean option to indicate if peers should NOT be discovered, default to
    // false indicates that
    // the peers should be discovered by default.
    //
    // This negative option is required because of the nature of the option that is
    // true when
    // added on the command line. You can't do --option=false, so false is set as
    // default
    // and you have not to set the option at all if you want it false.
    // This seems to be the only way it works with Picocli.
    // Also many other software use the same negative option scheme for false
    // defaults
    // meaning that it's probably the right way to handle disabling options.
    @CommandLine.Option(
        names = {"--discovery-enabled"},
        description = "Enable P2P discovery (default: ${DEFAULT-VALUE})",
        arity = "1")
    private final Boolean peerDiscoveryEnabled = true;

    // A list of bootstrap nodes can be passed
    // and a hardcoded list will be used otherwise by the Runner.
    // NOTE: we have no control over default value here.
    @CommandLine.Option(
        names = {"--bootnodes"},
        paramLabel = "<enode://id@host:port>",
        description =
            "Comma separated enode URLs for P2P discovery bootstrap. "
                + "Default is a predefined list.",
        split = ",",
        arity = "0..*")
    private final List<String> bootNodes = null;

    @SuppressWarnings({"FieldCanBeFinal", "FieldMayBeFinal"}) // PicoCLI requires non-final Strings.
    @CommandLine.Option(
        names = {"--p2p-host"},
        paramLabel = DefaultCommandValues.MANDATORY_HOST_FORMAT_HELP,
        description = "IP address this node advertises to its peers (default: ${DEFAULT-VALUE})",
        arity = "1")
    private String p2pHost = autoDiscoverDefaultIP().getHostAddress();

    @SuppressWarnings({"FieldCanBeFinal", "FieldMayBeFinal"}) // PicoCLI requires non-final Strings.
    @CommandLine.Option(
        names = {"--p2p-interface"},
        paramLabel = DefaultCommandValues.MANDATORY_HOST_FORMAT_HELP,
        description =
            "The network interface address on which this node listens for P2P communication (default: ${DEFAULT-VALUE})",
        arity = "1")
    private String p2pInterface = NetworkUtility.INADDR_ANY;

    @CommandLine.Option(
        names = {"--p2p-port"},
        paramLabel = DefaultCommandValues.MANDATORY_PORT_FORMAT_HELP,
        description = "Port on which to listen for P2P communication (default: ${DEFAULT-VALUE})",
        arity = "1")
    private final Integer p2pPort = EnodeURLImpl.DEFAULT_LISTENING_PORT;

    @CommandLine.Option(
        names = {"--max-peers", "--p2p-peer-upper-bound"},
        paramLabel = DefaultCommandValues.MANDATORY_INTEGER_FORMAT_HELP,
        description = "Maximum P2P connections that can be established (default: ${DEFAULT-VALUE})")
    private final Integer maxPeers = DefaultCommandValues.DEFAULT_MAX_PEERS;

    private int minPeers;

    @CommandLine.Option(
        names = {"--remote-connections-limit-enabled"},
        description =
            "Whether to limit the number of P2P connections initiated remotely. (default: ${DEFAULT-VALUE})")
    private final Boolean isLimitRemoteWireConnectionsEnabled = true;

    @CommandLine.Option(
        names = {"--remote-connections-max-percentage"},
        paramLabel = DefaultCommandValues.MANDATORY_DOUBLE_FORMAT_HELP,
        description =
            "The maximum percentage of P2P connections that can be initiated remotely. Must be between 0 and 100 inclusive. (default: ${DEFAULT-VALUE})",
        arity = "1",
        converter = PercentageConverter.class)
    private final Integer maxRemoteConnectionsPercentage =
        Fraction.fromFloat(DefaultCommandValues.DEFAULT_FRACTION_REMOTE_WIRE_CONNECTIONS_ALLOWED)
            .toPercentage()
            .getValue();

    @SuppressWarnings({"FieldCanBeFinal", "FieldMayBeFinal"}) // PicoCLI requires non-final Strings.
    @CommandLine.Option(
        names = {"--discovery-dns-url"},
        description = "Specifies the URL to use for DNS discovery")
    private String discoveryDnsUrl = null;

    @CommandLine.Option(
        names = {"--random-peer-priority-enabled"},
        description =
            "Allow for incoming connections to be prioritized randomly. This will prevent (typically small, stable) networks from forming impenetrable peer cliques. (default: ${DEFAULT-VALUE})")
    private final Boolean randomPeerPriority = false;

    @CommandLine.Option(
        names = {"--banned-node-ids", "--banned-node-id"},
        paramLabel = DefaultCommandValues.MANDATORY_NODE_ID_FORMAT_HELP,
        description = "A list of node IDs to ban from the P2P network.",
        split = ",",
        arity = "1..*")
    void setBannedNodeIds(final List<String> values) {
      try {
        bannedNodeIds =
            values.stream()
                .filter(value -> !value.isEmpty())
                .map(EnodeURLImpl::parseNodeId)
                .collect(Collectors.toList());
      } catch (final IllegalArgumentException e) {
        throw new CommandLine.ParameterException(
            new CommandLine(this),
            "Invalid ids supplied to '--banned-node-ids'. " + e.getMessage());
      }
    }

    private Collection<Bytes> bannedNodeIds = new ArrayList<>();

    // Used to discover the default IP of the client.
    // Loopback IP is used by default as this is how smokeTests require it to be
    // and it's probably a good security behaviour to default only on the localhost.
    private InetAddress autoDiscoverDefaultIP() {
      autoDiscoveredDefaultIP =
          Optional.ofNullable(autoDiscoveredDefaultIP).orElseGet(InetAddress::getLoopbackAddress);

      return autoDiscoveredDefaultIP;
    }
  }
}
