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

import org.hyperledger.besu.ethereum.blockcreation.MiningCoordinatorFactoryRegistry;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.ServiceManager;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;

import com.google.auto.service.AutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoService(BesuPlugin.class)
public class ClassicMiningPlugin implements BesuPlugin {

  private static final Logger LOG = LoggerFactory.getLogger(ClassicMiningPlugin.class);
  private static final String PLUGIN_NAME = "classic";

  private final PoWMiningCLIOptions cliOptions;

  public ClassicMiningPlugin() {
    this.cliOptions = PoWMiningCLIOptions.create();
  }

  @Override
  public void register(final ServiceManager context) {
    LOG.debug("Registering Classic Mining Plugin");

    // Register CLI options
    context
        .getService(PicoCLIOptions.class)
        .ifPresentOrElse(
            picoCLIOptions -> {
              picoCLIOptions.addPicoCLIOptions(PLUGIN_NAME, cliOptions);
              LOG.debug("Classic plugin CLI options registered");
            },
            () -> LOG.warn("PicoCLIOptions service not available"));

    // Register mining coordinator creator
    context
        .getService(MiningCoordinatorFactoryRegistry.class)
        .ifPresentOrElse(
            registry -> {
              final PoWMiningCoordinatorCreator creator =
                  new PoWMiningCoordinatorCreator(cliOptions);
              registry.registerCreator(creator::create);
              LOG.debug("PoW mining coordinator creator registered");
            },
            () -> LOG.warn("MiningCoordinatorFactoryRegistry not available"));
  }

  @Override
  public void start() {
    LOG.debug("Starting Classic Mining Plugin");
    LOG.trace("Applied configuration: {}", cliOptions);
  }

  @Override
  public void stop() {
    LOG.debug("Stopping Classic Mining Plugin");
  }
}
