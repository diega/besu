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
package org.hyperledger.besu.plugin.services;

import org.hyperledger.besu.datatypes.NetworkSpec;
import org.hyperledger.besu.plugin.Unstable;

import java.util.Optional;

/**
 * A service that allows plugins to provide network definitions for custom networks.
 *
 * <p>When the core network resolution mechanism (e.g., {@code --network=<name>}) does not recognize
 * a network name, it consults registered {@code NetworkProvider}s as a fallback. This lets plugins
 * add support for additional networks without modifying the core network enum.
 */
@Unstable
public interface NetworkProvider extends BesuService {

  /**
   * Returns the network definition for the given name, or empty if this provider does not handle
   * it. Names are matched case-insensitively.
   *
   * @param networkName the requested network name (e.g. the value of {@code --network=<name>})
   * @return the network definition, or {@link Optional#empty()} if this provider does not recognize
   *     the name
   */
  Optional<NetworkSpec> findNetwork(String networkName);
}
