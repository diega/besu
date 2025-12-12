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
package org.hyperledger.besu.plugin.services.network;

import java.util.Optional;

/**
 * Represents a network option that can be either a built-in network or a custom plugin-provided
 * network.
 *
 * @param <T> the type representing built-in networks (typically an enum)
 */
public sealed interface NetworkOption<T> {

  /**
   * Returns the built-in network if this is a Builtin option.
   *
   * @return Optional containing the built-in network, or empty if this is a CustomNetwork option
   */
  Optional<T> asBuiltin();

  /**
   * Returns the custom network name if this is a CustomNetwork option.
   *
   * @return Optional containing the custom name, or empty if this is a Builtin option
   */
  Optional<String> asCustom();

  /**
   * Returns true if this is a built-in network.
   *
   * @return true if Builtin, false if CustomNetwork
   */
  boolean isBuiltin();

  /**
   * Returns the network name as a string (works for both Builtin and CustomNetwork).
   *
   * @return the network name
   */
  String getName();

  /**
   * A built-in network.
   *
   * @param <T> the type representing built-in networks
   * @param network the built-in network value
   */
  record Builtin<T>(T network) implements NetworkOption<T> {
    @Override
    public Optional<T> asBuiltin() {
      return Optional.of(network);
    }

    @Override
    public Optional<String> asCustom() {
      return Optional.empty();
    }

    @Override
    public boolean isBuiltin() {
      return true;
    }

    @Override
    public String getName() {
      return network.toString();
    }
  }

  /**
   * A custom plugin-provided network.
   *
   * @param <T> the type representing built-in networks (unused but required for type consistency)
   * @param name the custom network name
   */
  record CustomNetwork<T>(String name) implements NetworkOption<T> {
    @Override
    public Optional<T> asBuiltin() {
      return Optional.empty();
    }

    @Override
    public Optional<String> asCustom() {
      return Optional.of(name);
    }

    @Override
    public boolean isBuiltin() {
      return false;
    }

    @Override
    public String getName() {
      return name;
    }
  }
}
