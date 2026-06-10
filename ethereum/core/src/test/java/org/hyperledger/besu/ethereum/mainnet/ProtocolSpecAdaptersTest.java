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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProtocolSpecAdaptersTest {

  @Mock private Function<ProtocolSpecBuilder, ProtocolSpecBuilder> firstModifier;

  @Mock private Function<ProtocolSpecBuilder, ProtocolSpecBuilder> secondModifier;

  @Test
  public void specAdapterFindsTheModifierBelowRequestedBlock() {

    final ProtocolSpecAdapters adapters =
        new ProtocolSpecAdapters(Map.of(3L, firstModifier, 5L, secondModifier));

    assertThat(adapters.getModifierForBlock(3)).isEqualTo(firstModifier);
    assertThat(adapters.getModifierForBlock(4)).isEqualTo(firstModifier);
    assertThat(adapters.getModifierForBlock(5)).isEqualTo(secondModifier);
    assertThat(adapters.getModifierForBlock(6)).isEqualTo(secondModifier);
    assertThat(adapters.getModifierForBlock(0)).isEqualTo(Function.identity());
  }

  @Test
  public void composesAContributionOverTheFloorModifierAtANewKey() {
    final List<String> applied = new ArrayList<>();
    final ProtocolSpecAdapters composed =
        new ProtocolSpecAdapters(
                Map.of(0L, tracing(applied, "base@0"), 10L, tracing(applied, "base@10")))
            .composedWith(ProtocolSpecAdapters.create(5L, tracing(applied, "contributed@5")));

    applyModifierAt(composed, 5L);
    assertThat(applied).containsExactly("base@0", "contributed@5");

    applied.clear();
    applyModifierAt(composed, 10L);
    assertThat(applied).containsExactly("base@10");
  }

  @Test
  public void composesAContributionOverTheModifierAtTheSameKey() {
    final List<String> applied = new ArrayList<>();
    final ProtocolSpecAdapters composed =
        new ProtocolSpecAdapters(Map.of(10L, tracing(applied, "base@10")))
            .composedWith(ProtocolSpecAdapters.create(10L, tracing(applied, "contributed@10")));

    applyModifierAt(composed, 10L);
    assertThat(applied).containsExactly("base@10", "contributed@10");
  }

  @Test
  public void contributionBelowEveryBaseKeyAppliesAlone() {
    final List<String> applied = new ArrayList<>();
    final ProtocolSpecAdapters composed =
        new ProtocolSpecAdapters(Map.of(10L, tracing(applied, "base@10")))
            .composedWith(ProtocolSpecAdapters.create(5L, tracing(applied, "contributed@5")));

    applyModifierAt(composed, 5L);
    assertThat(applied).containsExactly("contributed@5");
  }

  @Test
  public void contributionsAccumulateAcrossTheirActivations() {
    final List<String> applied = new ArrayList<>();
    final ProtocolSpecAdapters composed =
        new ProtocolSpecAdapters(Map.of(0L, tracing(applied, "base@0")))
            .composedWith(
                new ProtocolSpecAdapters(
                    Map.of(
                        5L,
                        tracing(applied, "contributed@5"),
                        8L,
                        tracing(applied, "contributed@8"))));

    applyModifierAt(composed, 8L);
    assertThat(applied).containsExactly("base@0", "contributed@5", "contributed@8");

    applied.clear();
    applyModifierAt(composed, 5L);
    assertThat(applied).containsExactly("base@0", "contributed@5");
  }

  private void applyModifierAt(final ProtocolSpecAdapters adapters, final long key) {
    final ProtocolSpecBuilder unused = adapters.getModifierForBlock(key).apply(null);
  }

  private Function<ProtocolSpecBuilder, ProtocolSpecBuilder> tracing(
      final List<String> applied, final String name) {
    return builder -> {
      applied.add(name);
      return builder;
    };
  }
}
