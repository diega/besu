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
package org.hyperledger.besu;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hyperledger.besu.config.GenesisConfigOptions;
import org.hyperledger.besu.plugin.services.ForkIdProvider;
import org.hyperledger.besu.plugin.services.ForkIdProvider.ForkSchedule;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ForkIdResolverTest {

  private static final BigInteger CHAIN_ID = BigInteger.valueOf(61);
  private static final List<Long> PROVIDER_BLOCKS = List.of(1L, 2L, 3L);
  private static final List<Long> PROVIDER_TIMESTAMPS = List.of(100L, 200L);
  private static final List<Long> GENESIS_BLOCKS = List.of(10L, 20L);
  private static final List<Long> GENESIS_TIMESTAMPS = List.of(1_000L);

  @Test
  void fallsBackToGenesisWhenNoProviderIsRegistered() {
    final ForkIdResolver resolver =
        new ForkIdResolver(Optional.empty(), genesisConfig(Optional.of(CHAIN_ID)));

    assertThat(resolver.blockNumberForks()).isEqualTo(GENESIS_BLOCKS);
    assertThat(resolver.timestampForks()).isEqualTo(GENESIS_TIMESTAMPS);
  }

  @Test
  void fallsBackToGenesisWhenProviderDoesNotSupportChain() {
    final ForkIdResolver resolver =
        new ForkIdResolver(
            Optional.of(new FakeForkIdProvider(CHAIN_ID)),
            genesisConfig(Optional.of(BigInteger.ONE)));

    assertThat(resolver.blockNumberForks()).isEqualTo(GENESIS_BLOCKS);
    assertThat(resolver.timestampForks()).isEqualTo(GENESIS_TIMESTAMPS);
  }

  @Test
  void usesProviderAndIgnoresGenesisWhenProviderSupportsChain() {
    final GenesisConfigOptions genesisConfig = genesisConfig(Optional.of(CHAIN_ID));
    final ForkIdResolver resolver =
        new ForkIdResolver(Optional.of(new FakeForkIdProvider(CHAIN_ID)), genesisConfig);

    assertThat(resolver.blockNumberForks()).isEqualTo(PROVIDER_BLOCKS);
    assertThat(resolver.timestampForks()).isEqualTo(PROVIDER_TIMESTAMPS);
    verify(genesisConfig, never()).getForkBlockNumbers();
    verify(genesisConfig, never()).getForkBlockTimestamps();
  }

  @Test
  void fallsBackToGenesisWhenNoChainIdIsConfiguredEvenIfAProviderIsRegistered() {
    // Without a chain ID there is no way to scope a provider, so the genesis lists are used.
    final ForkIdResolver resolver =
        new ForkIdResolver(
            Optional.of(new FakeForkIdProvider(CHAIN_ID)), genesisConfig(Optional.empty()));

    assertThat(resolver.blockNumberForks()).isEqualTo(GENESIS_BLOCKS);
    assertThat(resolver.timestampForks()).isEqualTo(GENESIS_TIMESTAMPS);
  }

  private static GenesisConfigOptions genesisConfig(final Optional<BigInteger> chainId) {
    final GenesisConfigOptions genesisConfig = mock(GenesisConfigOptions.class);
    when(genesisConfig.getChainId()).thenReturn(chainId);
    when(genesisConfig.getForkBlockNumbers()).thenReturn(GENESIS_BLOCKS);
    when(genesisConfig.getForkBlockTimestamps()).thenReturn(GENESIS_TIMESTAMPS);
    return genesisConfig;
  }

  /** A provider that applies only to a single chain ID. */
  private static final class FakeForkIdProvider implements ForkIdProvider {
    private final BigInteger supportedChainId;

    FakeForkIdProvider(final BigInteger supportedChainId) {
      this.supportedChainId = supportedChainId;
    }

    @Override
    public Optional<ForkSchedule> forkScheduleFor(final BigInteger chainId) {
      return supportedChainId.equals(chainId)
          ? Optional.of(new ForkSchedule(PROVIDER_BLOCKS, PROVIDER_TIMESTAMPS))
          : Optional.empty();
    }
  }
}
