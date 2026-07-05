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
package org.hyperledger.besu.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.hyperledger.besu.components.BesuComponent;

import org.junit.jupiter.api.Test;

class TransitionBesuControllerBuilderTest {

  @Test
  void besuComponentIsPropagatedToBothSubBuilders() {
    final BesuControllerBuilder preMergeBuilder = mock(BesuControllerBuilder.class);
    final MergeBesuControllerBuilder mergeBuilder = mock(MergeBesuControllerBuilder.class);
    final TransitionBesuControllerBuilder transition =
        new TransitionBesuControllerBuilder(preMergeBuilder, mergeBuilder);
    final BesuComponent besuComponent = mock(BesuComponent.class);

    transition.besuComponent(besuComponent);

    // Both sub-builders must receive the component: each one's createProtocolSchedule() resolves a
    // registered ProtocolScheduleCustomizer from it. Without this a TTD network would advertise the
    // customizer's fork-id activations while applying its rules on neither the pre- nor post-merge
    // schedule.
    verify(preMergeBuilder).besuComponent(besuComponent);
    verify(mergeBuilder).besuComponent(besuComponent);
  }
}
