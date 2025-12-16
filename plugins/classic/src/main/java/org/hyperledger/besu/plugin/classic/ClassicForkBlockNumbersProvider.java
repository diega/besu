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
package org.hyperledger.besu.plugin.classic;

import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getAghartaBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getAtlantisBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getDefuseDifficultyBombBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getDieHardBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getEcip1015BlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getGothamBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getMagnetoBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getMystiqueBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getPhoenixBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getSpiralBlockNumber;
import static org.hyperledger.besu.plugin.classic.config.ClassicGenesisConfigHelper.getThanosBlockNumber;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.ethereum.forkid.ForkBlockNumbersProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalLong;

/** Provides Classic fork block numbers for ForkId calculation. */
public class ClassicForkBlockNumbersProvider implements ForkBlockNumbersProvider {

  @Override
  public List<Long> getForkBlockNumbers(final GenesisConfigOptions config) {
    final List<Long> forks = new ArrayList<>();

    // Note: classicForkBlock is not included as it marks when Ethereum forked away,
    // not an actual protocol change in the original chain
    addIfPresent(forks, getEcip1015BlockNumber(config));
    addIfPresent(forks, getDieHardBlockNumber(config));
    addIfPresent(forks, getGothamBlockNumber(config));
    addIfPresent(forks, getDefuseDifficultyBombBlockNumber(config));
    addIfPresent(forks, getAtlantisBlockNumber(config));
    addIfPresent(forks, getAghartaBlockNumber(config));
    addIfPresent(forks, getPhoenixBlockNumber(config));
    addIfPresent(forks, getThanosBlockNumber(config));
    addIfPresent(forks, getMagnetoBlockNumber(config));
    addIfPresent(forks, getMystiqueBlockNumber(config));
    addIfPresent(forks, getSpiralBlockNumber(config));

    return forks;
  }

  @Override
  public List<Long> getForkTimestamps(final GenesisConfigOptions config) {
    return Collections.emptyList();
  }

  private static void addIfPresent(final List<Long> list, final OptionalLong value) {
    value.ifPresent(list::add);
  }
}
