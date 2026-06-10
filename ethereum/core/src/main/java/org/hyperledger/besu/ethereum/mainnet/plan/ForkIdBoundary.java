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
package org.hyperledger.besu.ethereum.mainnet.plan;

/**
 * Whether a {@link ForkEntry} is an EIP-2124 fork-ID boundary. A schedule milestone is not always a
 * fork-ID boundary: Frontier at genesis is a milestone with no boundary, and a
 * terminal-total-difficulty Merge is a spec change that is deliberately not folded into the fork
 * ID.
 */
public enum ForkIdBoundary {
  /** Folded into the EIP-2124 fork ID. */
  INCLUDED,
  /** A schedule milestone only; not folded into the fork ID. */
  EXCLUDED
}
