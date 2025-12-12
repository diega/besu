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

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class ClassicNetworkProviderTest {

  private final ClassicNetworkProvider provider = new ClassicNetworkProvider();

  @Test
  void networkNameIsCaseInsensitive() {
    assertThat(provider.getNetworkId("CLASSIC")).isEqualTo(BigInteger.ONE);
    assertThat(provider.getNetworkId("Classic")).isEqualTo(BigInteger.ONE);
    assertThat(provider.getNetworkId("MORDOR")).isEqualTo(BigInteger.valueOf(7));
    assertThat(provider.getNetworkId("Mordor")).isEqualTo(BigInteger.valueOf(7));
  }
}
