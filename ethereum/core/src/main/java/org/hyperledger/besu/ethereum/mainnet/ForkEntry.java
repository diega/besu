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

/**
 * A single fork declared once, with everything that distinguishes it: where it activates, what it
 * does to the schedule, and whether it counts towards the EIP-2124 fork ID. Declaring activation,
 * spec effect and fork-ID relevance together makes it impossible for the advertised fork ID to
 * drift from the rules a node enforces.
 *
 * @param id a stable identifier for the fork (its own identity, not its contributor's)
 * @param activation where the fork activates
 * @param effect what the fork does to the protocol-spec schedule
 * @param forkIdBoundary whether the activation is folded into the fork ID
 */
public record ForkEntry(
    String id, Activation activation, ScheduleEffect effect, ForkIdBoundary forkIdBoundary) {}
