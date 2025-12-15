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
    addIfPresent(forks, config.getEcip1015BlockNumber());
    addIfPresent(forks, config.getDieHardBlockNumber());
    addIfPresent(forks, config.getGothamBlockNumber());
    addIfPresent(forks, config.getDefuseDifficultyBombBlockNumber());
    addIfPresent(forks, config.getAtlantisBlockNumber());
    addIfPresent(forks, config.getAghartaBlockNumber());
    addIfPresent(forks, config.getPhoenixBlockNumber());
    addIfPresent(forks, config.getThanosBlockNumber());
    addIfPresent(forks, config.getMagnetoBlockNumber());
    addIfPresent(forks, config.getMystiqueBlockNumber());
    addIfPresent(forks, config.getSpiralBlockNumber());

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
