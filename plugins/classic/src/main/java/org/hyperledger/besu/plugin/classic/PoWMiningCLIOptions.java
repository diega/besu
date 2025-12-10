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

import java.time.Duration;

import picocli.CommandLine;

/** CLI options for PoW mining configuration in the Classic plugin. */
public class PoWMiningCLIOptions {

  /** Default limit for remote sealers. */
  public static final int DEFAULT_REMOTE_SEALERS_LIMIT = 1000;

  /** Default TTL for remote sealers hashrate in minutes. */
  public static final long DEFAULT_REMOTE_SEALERS_TTL = Duration.ofMinutes(10).toMinutes();

  /** Default TTL for PoW jobs in milliseconds. */
  public static final long DEFAULT_POW_JOB_TTL = Duration.ofMinutes(5).toMillis();

  /** Default maximum ommer depth. */
  public static final int DEFAULT_MAX_OMMERS_DEPTH = 8;

  private static final String REMOTE_SEALERS_LIMIT_FLAG = "--Xplugin-classic-remote-sealers-limit";
  private static final String REMOTE_SEALERS_TTL_FLAG =
      "--Xplugin-classic-remote-sealers-hashrate-ttl";
  private static final String POW_JOB_TTL_FLAG = "--Xplugin-classic-pow-job-ttl";
  private static final String MAX_OMMERS_DEPTH_FLAG = "--Xplugin-classic-max-ommers-depth";

  @CommandLine.Option(
      names = {REMOTE_SEALERS_LIMIT_FLAG},
      hidden = true,
      paramLabel = "<INTEGER>",
      description =
          "Limits the number of remote sealers that can submit their hashrates (default: ${DEFAULT-VALUE})")
  private int remoteSealersLimit = DEFAULT_REMOTE_SEALERS_LIMIT;

  @CommandLine.Option(
      names = {REMOTE_SEALERS_TTL_FLAG},
      hidden = true,
      paramLabel = "<LONG>",
      description =
          "Specifies the lifetime of each entry in the cache in minutes. An entry will be automatically "
              + "deleted if no update has been received before the deadline (default: ${DEFAULT-VALUE})")
  private long remoteSealersTimeToLive = DEFAULT_REMOTE_SEALERS_TTL;

  @CommandLine.Option(
      names = {POW_JOB_TTL_FLAG},
      hidden = true,
      paramLabel = "<LONG>",
      description =
          "Specifies the time in milliseconds PoW jobs are kept in cache and will accept a solution "
              + "from miners (default: ${DEFAULT-VALUE})")
  private long powJobTimeToLive = DEFAULT_POW_JOB_TTL;

  @CommandLine.Option(
      names = {MAX_OMMERS_DEPTH_FLAG},
      hidden = true,
      paramLabel = "<INTEGER>",
      description =
          "Specifies the depth of ommer blocks to accept when receiving solutions (default: ${DEFAULT-VALUE})")
  private int maxOmmersDepth = DEFAULT_MAX_OMMERS_DEPTH;

  private PoWMiningCLIOptions() {}

  /**
   * Creates a new instance with default values.
   *
   * @return new PoWMiningCLIOptions instance
   */
  public static PoWMiningCLIOptions create() {
    return new PoWMiningCLIOptions();
  }

  /**
   * Gets remote sealers limit.
   *
   * @return the remote sealers limit
   */
  public int getRemoteSealersLimit() {
    return remoteSealersLimit;
  }

  /**
   * Gets remote sealers time to live in minutes.
   *
   * @return the remote sealers time to live
   */
  public long getRemoteSealersTimeToLive() {
    return remoteSealersTimeToLive;
  }

  /**
   * Gets PoW job time to live in milliseconds.
   *
   * @return the PoW job time to live
   */
  public long getPowJobTimeToLive() {
    return powJobTimeToLive;
  }

  /**
   * Gets max ommers depth.
   *
   * @return the max ommers depth
   */
  public int getMaxOmmersDepth() {
    return maxOmmersDepth;
  }

  @Override
  public String toString() {
    return "PoWMiningCLIOptions{"
        + "remoteSealersLimit="
        + remoteSealersLimit
        + ", remoteSealersTimeToLive="
        + remoteSealersTimeToLive
        + ", powJobTimeToLive="
        + powJobTimeToLive
        + ", maxOmmersDepth="
        + maxOmmersDepth
        + '}';
  }
}
