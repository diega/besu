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
package org.hyperledger.besu.cli.config;

import org.hyperledger.besu.plugin.services.network.NetworkOption;

import java.util.Locale;

import picocli.CommandLine.ITypeConverter;

/** Converts a string network name to a NetworkOption (either Builtin or CustomNetwork). */
public class NetworkOptionConverter implements ITypeConverter<NetworkOption<NetworkName>> {

  @Override
  public NetworkOption<NetworkName> convert(final String value) {
    try {
      final NetworkName networkName = NetworkName.valueOf(value.toUpperCase(Locale.ROOT));
      return new NetworkOption.Builtin<>(networkName);
    } catch (final IllegalArgumentException e) {
      return new NetworkOption.CustomNetwork<>(value);
    }
  }
}
