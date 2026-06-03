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
package org.hyperledger.besu.datatypes;

import java.math.BigInteger;
import java.net.URL;

/**
 * The static definition of a network: enough to seed network configuration — its network id,
 * genesis config URL and whether it supports snap sync. Implemented both by Besu's built-in
 * networks and by plugin-provided networks (via {@code NetworkProvider}), so the two can be handled
 * uniformly.
 */
public interface NetworkSpec {

  /**
   * The network ID, used for peer-to-peer protocol negotiation.
   *
   * @return the network ID
   */
  BigInteger getNetworkId();

  /**
   * The URL of the genesis JSON configuration; the chain ID and fork schedule are read from it.
   *
   * @return the genesis config URL
   */
  URL getGenesisConfigUrl();

  /**
   * Whether the network supports snap sync, which selects the default sync mode.
   *
   * @return {@code true} if snap sync is supported
   */
  boolean canSnapSync();
}
