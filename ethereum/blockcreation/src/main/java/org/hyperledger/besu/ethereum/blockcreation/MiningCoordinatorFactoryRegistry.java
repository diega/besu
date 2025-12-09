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
package org.hyperledger.besu.ethereum.blockcreation;

import org.hyperledger.besu.plugin.services.BesuService;

import java.util.function.Function;

/**
 * Registry service for custom MiningCoordinator creation.
 *
 * <p>Plugins use this service to register a function that creates MiningCoordinator instances. The
 * core uses this registry to create the mining coordinator when building the controller.
 */
public interface MiningCoordinatorFactoryRegistry extends BesuService {

  /**
   * Registers a mining coordinator creator function.
   *
   * @param creator function that creates a MiningCoordinator from a context
   */
  void registerCreator(Function<MiningCoordinatorContext, MiningCoordinator> creator);

  /**
   * Creates a mining coordinator using the registered creator.
   *
   * @param context the context with dependencies for creating the coordinator
   * @return the created MiningCoordinator
   * @throws IllegalStateException if no creator has been registered
   */
  MiningCoordinator createCoordinator(MiningCoordinatorContext context);
}
