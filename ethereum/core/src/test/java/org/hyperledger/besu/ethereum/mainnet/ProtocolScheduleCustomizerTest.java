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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.config.ForkIdActivations;
import org.hyperledger.besu.plugin.ServiceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

class ProtocolScheduleCustomizerTest {

  @Test
  void forkIdActivationsDefaultsToTheAdapterBlockNumbers() {
    final ProtocolScheduleCustomizer customizer =
        config -> Map.of(100L, Function.identity(), 200L, Function.identity());

    final ForkIdActivations activations = customizer.forkIdActivations(null);

    assertThat(activations.blockNumbers()).containsExactlyInAnyOrder(100L, 200L);
    assertThat(activations.timestamps()).isEmpty();
  }

  @Test
  void composeAdaptersWithoutAServiceManagerReturnsTheBaseModifiersUnchanged() {
    final List<String> trace = new ArrayList<>();

    final ProtocolSpecAdapters adapters =
        ProtocolScheduleCustomizer.composeAdapters(
            Map.of(0L, recordingModifier(trace, "base")), null, Optional.empty());

    final ProtocolSpecBuilder builder = new ProtocolSpecBuilder();
    assertThat(adapters.getModifierForBlock(0L).apply(builder)).isSameAs(builder);
    assertThat(trace).containsExactly("base");
  }

  @Test
  void composeAdaptersFoldsCustomizerAdaptersInAtTheirOwnBlocks() {
    final List<String> trace = new ArrayList<>();
    final ProtocolScheduleCustomizer customizer =
        config -> Map.of(5L, recordingModifier(trace, "custom"));

    final ProtocolSpecAdapters adapters =
        ProtocolScheduleCustomizer.composeAdapters(
            Map.of(0L, recordingModifier(trace, "base")), null, withCustomizer(customizer));

    final ProtocolSpecBuilder builder = new ProtocolSpecBuilder();

    // block 0 still resolves to the path's own base modifier...
    assertThat(adapters.getModifierForBlock(0L).apply(builder)).isSameAs(builder);
    assertThat(trace).containsExactly("base");

    // ...and the customizer's adapter is present at its own block.
    trace.clear();
    assertThat(adapters.getModifierForBlock(5L).apply(builder)).isSameAs(builder);
    assertThat(trace).containsExactly("custom");
  }

  @Test
  void composeAdaptersComposesRatherThanReplacesWhenAdapterSharesABlockWithABaseModifier() {
    final List<String> trace = new ArrayList<>();
    final ProtocolScheduleCustomizer customizer =
        config -> Map.of(0L, recordingModifier(trace, "custom"));

    final ProtocolSpecAdapters adapters =
        ProtocolScheduleCustomizer.composeAdapters(
            Map.of(0L, recordingModifier(trace, "base")), null, withCustomizer(customizer));

    final ProtocolSpecBuilder builder = new ProtocolSpecBuilder();
    assertThat(adapters.getModifierForBlock(0L).apply(builder)).isSameAs(builder);
    // both ran — the path's structural modifier first, then the customizer's; nothing is dropped
    assertThat(trace).containsExactly("base", "custom");
  }

  private static Optional<ServiceManager> withCustomizer(
      final ProtocolScheduleCustomizer customizer) {
    final ServiceManager serviceManager = mock(ServiceManager.class);
    when(serviceManager.getService(ProtocolScheduleCustomizer.class))
        .thenReturn(Optional.of(customizer));
    return Optional.of(serviceManager);
  }

  private static Function<ProtocolSpecBuilder, ProtocolSpecBuilder> recordingModifier(
      final List<String> trace, final String label) {
    return builder -> {
      trace.add(label);
      return builder;
    };
  }
}
