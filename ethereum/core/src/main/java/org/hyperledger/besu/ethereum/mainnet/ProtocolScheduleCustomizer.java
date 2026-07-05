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

import org.hyperledger.besu.config.ForkIdActivations;
import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.plugin.ServiceManager;
import org.hyperledger.besu.plugin.services.BesuService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A service that allows plugins to customize the protocol schedule by providing adapter functions
 * that modify {@link ProtocolSpecBuilder} instances at specific block numbers.
 *
 * <p>Adapters use floor semantics: an adapter registered at block N applies to all milestones at
 * block numbers &gt;= N until the next adapter entry. Each adapter receives the raw builder from
 * the nearest prior milestone and can modify it (e.g., swap gas calculator, block processor, EVM).
 *
 * <p>This is the primary mechanism for plugins to inject custom protocol rules (such as
 * network-specific hardfork changes) into the protocol schedule without modifying core code.
 */
public interface ProtocolScheduleCustomizer extends BesuService {

  /**
   * Creates a map of block-number-keyed adapter functions for the given genesis configuration.
   *
   * <p>Implementations must be deterministic and depend only on the chain's own configuration (its
   * chain id and specific fork settings). In particular they must not branch on {@link
   * GenesisConfigOptions#getForkBlockNumbers()} or {@link
   * GenesisConfigOptions#getForkBlockTimestamps()}: the platform augments those aggregates with the
   * very activations this customizer contributes (see {@link #forkIdActivations}), so reading them
   * here would make the result depend on whether the activations have already been folded in. This
   * method may be evaluated more than once during start-up (once to derive the fork ID and once per
   * protocol-schedule path), and a non-deterministic implementation would let the advertised fork
   * ID drift from the rules actually enforced.
   *
   * @param config the genesis configuration options
   * @return a non-null map from block number to adapter function (return an empty map to contribute
   *     nothing); each function transforms a {@link ProtocolSpecBuilder} to apply custom protocol
   *     rules at that block
   */
  Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> createAdapters(
      GenesisConfigOptions config);

  /**
   * The EIP-2124 fork activations this customizer contributes — the block numbers (and timestamps)
   * at which it introduces consensus changes that peers must agree on, so the node's advertised
   * fork ID stays consistent with the rules it enforces.
   *
   * <p>Defaults to the {@link #createAdapters} block numbers, since a customizer's adapter blocks
   * are its fork boundaries. Override to declare timestamp activations, or to decouple the
   * advertised fork ID from the adapter blocks.
   *
   * @param config the genesis configuration options of the active chain
   * @return the fork activations folded into the EIP-2124 fork ID
   */
  default ForkIdActivations forkIdActivations(final GenesisConfigOptions config) {
    return ForkIdActivations.ofBlockNumbers(createAdapters(config).keySet());
  }

  /**
   * Folds the registered customizer's adapters (if any) over a protocol-schedule path's own base
   * modifiers, producing the {@link ProtocolSpecAdapters} that path should build with. The mainnet
   * schedule construction composes its adapters here, so the rules a node enforces stay consistent
   * with the fork ID derived from {@link #forkIdActivations}.
   *
   * <p>Where a customizer adapter shares a block with one of the path's structural modifiers, the
   * two are composed (the structural modifier first, then the customizer's) rather than one
   * replacing the other, so neither contribution is silently dropped.
   *
   * @param baseModifiers the path's own block-keyed modifiers (for example the fixed-difficulty
   *     calculator swap, or the Merge's Paris modifications)
   * @param config the genesis configuration options of the active chain
   * @param serviceManager the service manager to query for a registered customizer, if present
   * @return the combined adapters to hand to the {@link ProtocolScheduleBuilder}
   */
  static ProtocolSpecAdapters composeAdapters(
      final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> baseModifiers,
      final GenesisConfigOptions config,
      final Optional<ServiceManager> serviceManager) {
    final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> modifiers =
        new HashMap<>(baseModifiers);
    serviceManager
        .flatMap(manager -> manager.getService(ProtocolScheduleCustomizer.class))
        .ifPresent(
            customizer ->
                customizer
                    .createAdapters(config)
                    .forEach(
                        (block, adapter) ->
                            modifiers.merge(
                                block, adapter, (base, custom) -> base.andThen(custom))));
    return new ProtocolSpecAdapters(modifiers);
  }
}
