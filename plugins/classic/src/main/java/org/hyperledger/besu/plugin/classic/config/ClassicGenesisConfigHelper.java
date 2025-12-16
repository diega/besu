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
package org.hyperledger.besu.plugin.classic.config;

import org.hyperledger.besu.config.GenesisConfigOptions;

import java.util.Map;
import java.util.OptionalLong;

/**
 * Helper to read Classic-specific genesis config values from the raw config map. This allows the
 * plugin to access Classic config fields without requiring methods in GenesisConfigOptions.
 */
public final class ClassicGenesisConfigHelper {

  private ClassicGenesisConfigHelper() {}

  public static OptionalLong getClassicForkBlock(final GenesisConfigOptions config) {
    return getOptionalLong(config, "classicforkblock");
  }

  public static OptionalLong getEcip1015BlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "ecip1015block");
  }

  public static OptionalLong getDieHardBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "diehardblock");
  }

  public static OptionalLong getGothamBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "gothamblock");
  }

  public static OptionalLong getDefuseDifficultyBombBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "ecip1041block");
  }

  public static OptionalLong getAtlantisBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "atlantisblock");
  }

  public static OptionalLong getAghartaBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "aghartablock");
  }

  public static OptionalLong getPhoenixBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "phoenixblock");
  }

  public static OptionalLong getThanosBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "thanosblock");
  }

  public static OptionalLong getMagnetoBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "magnetoblock");
  }

  public static OptionalLong getMystiqueBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "mystiqueblock");
  }

  public static OptionalLong getSpiralBlockNumber(final GenesisConfigOptions config) {
    return getOptionalLong(config, "spiralblock");
  }

  public static OptionalLong getEcip1017EraRounds(final GenesisConfigOptions config) {
    return getOptionalLong(config, "ecip1017erarounds");
  }

  private static OptionalLong getOptionalLong(final GenesisConfigOptions config, final String key) {
    final Map<String, Object> rawConfig = config.getRawConfigMap();
    final Object value = rawConfig.get(key);
    if (value == null) {
      return OptionalLong.empty();
    }
    if (value instanceof Number) {
      return OptionalLong.of(((Number) value).longValue());
    }
    if (value instanceof String) {
      try {
        return OptionalLong.of(Long.parseLong((String) value));
      } catch (final NumberFormatException e) {
        return OptionalLong.empty();
      }
    }
    return OptionalLong.empty();
  }
}
