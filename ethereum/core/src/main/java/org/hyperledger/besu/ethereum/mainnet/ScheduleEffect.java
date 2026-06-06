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
package org.hyperledger.besu.ethereum.mainnet;

import org.hyperledger.besu.datatypes.HardforkId;

import java.util.function.Function;

/**
 * What a {@link ForkEntry} does to the protocol-spec schedule at its activation. This is what lets
 * core-defined hard-forks and plugin-contributed customizations live in one list: core contributes
 * full built-in hard-fork definitions, a plugin contributes a modifier over the prior (floor) spec.
 */
public sealed interface ScheduleEffect
    permits ScheduleEffect.BuiltInHardfork, ScheduleEffect.Modifier {

  /**
   * A core, built-in hard-fork, referenced by its {@link HardforkId} and resolved against the
   * mainnet protocol-spec factory when the schedule is built.
   *
   * @param hardforkId the hard-fork identifier
   */
  record BuiltInHardfork(HardforkId hardforkId) implements ScheduleEffect {}

  /**
   * A transform applied on top of the resolved floor spec (the spec in force just before this
   * activation). This is the plugin case; the modifier is composed after — never replacing — the
   * floor spec's own modifications.
   *
   * @param modifier the spec-builder transform
   */
  record Modifier(Function<ProtocolSpecBuilder, ProtocolSpecBuilder> modifier)
      implements ScheduleEffect {}
}
