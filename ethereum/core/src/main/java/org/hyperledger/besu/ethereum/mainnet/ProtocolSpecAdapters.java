/*
 * Copyright 2020 ConsenSys AG.
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

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

public class ProtocolSpecAdapters {

  final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> modifiers;

  public ProtocolSpecAdapters(
      final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> modifiers) {
    this.modifiers = modifiers;
  }

  public static ProtocolSpecAdapters create(
      final long blockNumberOrTimestamp,
      final Function<ProtocolSpecBuilder, ProtocolSpecBuilder> modifier) {
    final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> entries = new HashMap<>();
    entries.put(blockNumberOrTimestamp, modifier);
    return new ProtocolSpecAdapters(entries);
  }

  /**
   * An empty set of adapters: every milestone keeps its unmodified definition.
   *
   * @return an empty set of adapters
   */
  public static ProtocolSpecAdapters empty() {
    return new ProtocolSpecAdapters(new HashMap<>());
  }

  /**
   * Returns a new set of adapters with the contributed modifiers composed on top of these. A
   * contributed modifier applies <em>after</em> the modifier in force at its activation — the floor
   * modifier — rather than replacing it, which is what a bare map key would do when {@code
   * ProtocolScheduleBuilder} resolves modifiers per milestone. Contributed activations are folded
   * in ascending order, so a contribution stays in force from its activation through every later
   * contributed activation.
   *
   * @param contributed the modifiers to compose on top of these adapters
   * @return a new set of adapters with the contributions composed in
   */
  public ProtocolSpecAdapters composedWith(final ProtocolSpecAdapters contributed) {
    final Map<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>> composed =
        new HashMap<>(modifiers);
    contributed.stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(
            entry -> {
              final Function<ProtocolSpecBuilder, ProtocolSpecBuilder> floorModifier =
                  new ProtocolSpecAdapters(composed).getModifierForBlock(entry.getKey());
              composed.put(entry.getKey(), floorModifier.andThen(entry.getValue()));
            });
    return new ProtocolSpecAdapters(composed);
  }

  public Function<ProtocolSpecBuilder, ProtocolSpecBuilder> getModifierForBlock(
      final long blockNumberOrTimestamp) {
    final NavigableSet<Long> epochs = new TreeSet<>(modifiers.keySet());
    final Long modifier = epochs.floor(blockNumberOrTimestamp);

    if (modifier == null) {
      return Function.identity();
    }

    return modifiers.get(modifier);
  }

  public Stream<Map.Entry<Long, Function<ProtocolSpecBuilder, ProtocolSpecBuilder>>> stream() {
    return modifiers.entrySet().stream();
  }
}
