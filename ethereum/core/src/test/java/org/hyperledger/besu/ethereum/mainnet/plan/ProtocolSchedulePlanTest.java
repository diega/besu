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
package org.hyperledger.besu.ethereum.mainnet.plan;

import static org.assertj.core.api.Assertions.assertThat;

import org.hyperledger.besu.config.GenesisConfig;
import org.hyperledger.besu.config.GenesisConfigOptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ProtocolSchedulePlanTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/mainnet.json",
        "/sepolia.json",
        "/hoodi.json",
        "/ephemery.json",
        "/dev.json",
        "/future.json",
        "/experimental.json",
        "/lukso.json",
        "/linea-mainnet.json",
        "/linea-sepolia.json"
      })
  public void forkIdActivationsMatchTheGenesisGettersForEveryBundledNetwork(
      final String genesisResource) {
    assertPlanMatchesConfig(GenesisConfig.fromResource(genesisResource).getConfigOptions());
  }

  @Test
  public void forkIdActivationsMatchTheGenesisGettersForADaoConfig() {
    assertPlanMatchesConfig(
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"daoForkBlock\": 3, \"eip150Block\": 14,"
                    + " \"byzantiumBlock\": 16, \"chainId\": 1234}}")
            .getConfigOptions());
  }

  @Test
  public void forkIdActivationsMatchTheGenesisGettersForAMergeNetSplitConfig() {
    assertPlanMatchesConfig(
        GenesisConfig.fromConfig(
                "{\"config\": {\"homesteadBlock\": 2, \"berlinBlock\": 10, \"londonBlock\": 10,"
                    + " \"mergeNetSplitBlock\": 20, \"shanghaiTime\": 1000,"
                    + " \"terminalTotalDifficulty\": 100, \"chainId\": 1234}}")
            .getConfigOptions());
  }

  private void assertPlanMatchesConfig(final GenesisConfigOptions config) {
    final ProtocolSchedulePlan plan = ProtocolSchedulePlan.fromConfig(config);
    assertThat(plan.forkIdBlockNumbers()).isEqualTo(config.getForkBlockNumbers());
    assertThat(plan.forkIdTimestamps()).isEqualTo(config.getForkBlockTimestamps());
  }
}
