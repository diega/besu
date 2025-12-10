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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

public class PoWMiningCLIOptionsTest {

  @Test
  public void defaultValuesAreCorrect() {
    final PoWMiningCLIOptions options = PoWMiningCLIOptions.create();

    assertThat(options.getRemoteSealersLimit())
        .isEqualTo(PoWMiningCLIOptions.DEFAULT_REMOTE_SEALERS_LIMIT);
    assertThat(options.getRemoteSealersTimeToLive())
        .isEqualTo(PoWMiningCLIOptions.DEFAULT_REMOTE_SEALERS_TTL);
    assertThat(options.getPowJobTimeToLive()).isEqualTo(PoWMiningCLIOptions.DEFAULT_POW_JOB_TTL);
    assertThat(options.getMaxOmmersDepth()).isEqualTo(PoWMiningCLIOptions.DEFAULT_MAX_OMMERS_DEPTH);
  }

  @Test
  public void defaultConstantsHaveExpectedValues() {
    assertThat(PoWMiningCLIOptions.DEFAULT_REMOTE_SEALERS_LIMIT).isEqualTo(1000);
    assertThat(PoWMiningCLIOptions.DEFAULT_REMOTE_SEALERS_TTL)
        .isEqualTo(Duration.ofMinutes(10).toMinutes());
    assertThat(PoWMiningCLIOptions.DEFAULT_POW_JOB_TTL).isEqualTo(Duration.ofMinutes(5).toMillis());
    assertThat(PoWMiningCLIOptions.DEFAULT_MAX_OMMERS_DEPTH).isEqualTo(8);
  }

  @Test
  public void remoteSealersLimitCanBeConfigured() {
    final PoWMiningCLIOptions options = PoWMiningCLIOptions.create();
    parseOptions(options, "--Xplugin-classic-remote-sealers-limit=500");

    assertThat(options.getRemoteSealersLimit()).isEqualTo(500);
  }

  @Test
  public void remoteSealersHashrateTtlCanBeConfigured() {
    final PoWMiningCLIOptions options = PoWMiningCLIOptions.create();
    parseOptions(options, "--Xplugin-classic-remote-sealers-hashrate-ttl=20");

    assertThat(options.getRemoteSealersTimeToLive()).isEqualTo(20);
  }

  @Test
  public void powJobTtlCanBeConfigured() {
    final PoWMiningCLIOptions options = PoWMiningCLIOptions.create();
    parseOptions(options, "--Xplugin-classic-pow-job-ttl=600000");

    assertThat(options.getPowJobTimeToLive()).isEqualTo(600000);
  }

  @Test
  public void maxOmmersDepthCanBeConfigured() {
    final PoWMiningCLIOptions options = PoWMiningCLIOptions.create();
    parseOptions(options, "--Xplugin-classic-max-ommers-depth=6");

    assertThat(options.getMaxOmmersDepth()).isEqualTo(6);
  }

  @Test
  public void allOptionsCanBeConfiguredTogether() {
    final PoWMiningCLIOptions options = PoWMiningCLIOptions.create();
    parseOptions(
        options,
        "--Xplugin-classic-remote-sealers-limit=2000",
        "--Xplugin-classic-remote-sealers-hashrate-ttl=15",
        "--Xplugin-classic-pow-job-ttl=400000",
        "--Xplugin-classic-max-ommers-depth=4");

    assertThat(options.getRemoteSealersLimit()).isEqualTo(2000);
    assertThat(options.getRemoteSealersTimeToLive()).isEqualTo(15);
    assertThat(options.getPowJobTimeToLive()).isEqualTo(400000);
    assertThat(options.getMaxOmmersDepth()).isEqualTo(4);
  }

  private void parseOptions(final PoWMiningCLIOptions options, final String... args) {
    final CommandLine commandLine = new CommandLine(new TestCommand());
    commandLine.addMixin("pow-mining", options);
    commandLine.parseArgs(args);
  }

  @CommandLine.Command
  private static class TestCommand {}
}
