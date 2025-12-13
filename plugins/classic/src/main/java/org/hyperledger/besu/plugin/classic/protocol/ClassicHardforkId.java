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
package org.hyperledger.besu.plugin.classic.protocol;

import org.hyperledger.besu.datatypes.HardforkId;

/** List of all Ethereum Classic hard forks. */
public enum ClassicHardforkId implements HardforkId {
  /** Frontier fork. */
  FRONTIER(true, "Frontier"),
  /** Homestead fork. */
  HOMESTEAD(true, "Homestead"),
  /** Classic Recovery Init fork. */
  CLASSIC_RECOVERY_INIT(true, "Classic Recovery Init"),
  /** Classic Tangerine Whistle fork. */
  CLASSIC_TANGERINE_WHISTLE(true, "Classic Tangerine Whistle"),
  /** Die Hard fork. */
  DIE_HARD(true, "Die Hard"),
  /** Gotham fork. */
  GOTHAM(true, "Gotham"),
  /** Defuse Difficulty Bomb fork. */
  DEFUSE_DIFFICULTY_BOMB(true, "Defuse Difficulty Bomb"),
  /** Atlantis fork. */
  ATLANTIS(true, "Atlantis"),
  /** Agharta fork. */
  AGHARTA(true, "Agharta"),
  /** Phoenix fork. */
  PHOENIX(true, "Phoenix"),
  /** Thanos fork. */
  THANOS(true, "Thanos"),
  /** Magneto fork. */
  MAGNETO(true, "Magneto"),
  /** Mystique fork. */
  MYSTIQUE(true, "Mystique"),
  /** Spiral fork. */
  SPIRAL(true, "Spiral");

  final boolean finalized;
  final String description;

  ClassicHardforkId(final boolean finalized, final String description) {
    this.finalized = finalized;
    this.description = description;
  }

  @Override
  public boolean finalized() {
    return finalized;
  }

  @Override
  public String description() {
    return description;
  }
}
