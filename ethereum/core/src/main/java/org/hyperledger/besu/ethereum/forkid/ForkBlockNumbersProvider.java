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
package org.hyperledger.besu.ethereum.forkid;

import org.hyperledger.besu.config.GenesisConfigOptions;

import java.util.List;

/**
 * Allows plugins to provide fork block numbers and timestamps for ForkId calculation. This is used
 * by ForkIdManager for P2P peer validation (EIP-2124).
 *
 * <p>The plugin decides whether to register a provider based on its own logic (e.g., checking chain
 * ID or config fields during registration).
 */
public interface ForkBlockNumbersProvider {

  /**
   * Returns the list of fork block numbers to provide. These will be combined with the built-in
   * fork numbers from GenesisConfigOptions.
   *
   * @param config the genesis config options
   * @return list of fork block numbers
   */
  List<Long> getForkBlockNumbers(GenesisConfigOptions config);

  /**
   * Returns the list of fork timestamps to provide. These will be combined with the built-in fork
   * timestamps from GenesisConfigOptions.
   *
   * @param config the genesis config options
   * @return list of fork timestamps
   */
  List<Long> getForkTimestamps(GenesisConfigOptions config);
}
