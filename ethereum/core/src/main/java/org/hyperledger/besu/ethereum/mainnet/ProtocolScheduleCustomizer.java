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

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.plugin.services.BesuService;

import java.util.Map;
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
   * @param config the genesis configuration options
   * @return a map from block number to adapter function; each function transforms a {@link
   *     ProtocolSpecBuilder} to apply custom protocol rules at that block
   */
  Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> createAdapters(
      GenesisConfigOptions config);
}
