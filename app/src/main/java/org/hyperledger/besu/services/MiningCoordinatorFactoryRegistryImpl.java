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
package org.hyperledger.besu.services;

import org.hyperledger.besu.ethereum.blockcreation.MiningCoordinator;
import org.hyperledger.besu.ethereum.blockcreation.MiningCoordinatorContext;
import org.hyperledger.besu.ethereum.blockcreation.MiningCoordinatorFactoryRegistry;
import org.hyperledger.besu.ethereum.blockcreation.NoopMiningCoordinator;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiningCoordinatorFactoryRegistryImpl implements MiningCoordinatorFactoryRegistry {

  private static final Logger LOG =
      LoggerFactory.getLogger(MiningCoordinatorFactoryRegistryImpl.class);

  private Function<MiningCoordinatorContext, MiningCoordinator> creator =
      ctx -> new NoopMiningCoordinator(ctx.getMiningConfiguration());

  @Override
  public void registerCreator(final Function<MiningCoordinatorContext, MiningCoordinator> creator) {
    this.creator = creator;
    LOG.debug("Registered MiningCoordinator creator");
  }

  @Override
  public MiningCoordinator createCoordinator(final MiningCoordinatorContext context) {
    return creator.apply(context);
  }
}
