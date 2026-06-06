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

import org.hyperledger.besu.plugin.services.BesuService;

/**
 * The service plugins use to register {@link ProtocolScheduleContributor}s. It is registered as an
 * early service so plugins can contribute from {@code BesuPlugin#register(...)}; the registered
 * contributors are collected once when the {@link ProtocolSchedulePlan} is frozen (before the
 * controller is built), after which the service is read-only and further registration fails fast.
 *
 * <p>This service interface lives in {@code ethereum/core} (not {@code plugin-api}) because the
 * {@link ForkEntry} types it carries reference {@link ProtocolSpecBuilder}. Consuming plugins must
 * therefore depend on {@code ethereum/core} as a provided/compile-only dependency and must not shade
 * its classes, or the {@code Class}-identity lookup in {@code getService(...)} will miss.
 */
public interface ProtocolScheduleContributionService extends BesuService {

  /**
   * Registers a contributor. Must be called before the plan is frozen.
   *
   * @param contributor the contributor to register
   */
  void registerContributor(ProtocolScheduleContributor contributor);
}
