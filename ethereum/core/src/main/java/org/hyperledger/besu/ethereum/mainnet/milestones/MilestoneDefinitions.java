/*
 * Copyright contributors to Besu.
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
package org.hyperledger.besu.ethereum.mainnet.milestones;

import static org.hyperledger.besu.ethereum.mainnet.milestones.MilestoneDefinition.createBlockNumberMilestone;
import static org.hyperledger.besu.ethereum.mainnet.milestones.MilestoneDefinition.createTimestampMilestone;

import org.hyperledger.besu.datatypes.HardforkId.MainnetHardforkId;
import org.hyperledger.besu.ethereum.mainnet.MainnetProtocolSpecFactory;
import org.hyperledger.besu.ethereum.mainnet.ProtocolSpecBuilder;
import org.hyperledger.besu.ethereum.mainnet.plan.Activation;
import org.hyperledger.besu.ethereum.mainnet.plan.ForkEntry;
import org.hyperledger.besu.ethereum.mainnet.plan.ProtocolSchedulePlan;
import org.hyperledger.besu.ethereum.mainnet.plan.ScheduleEffect;

import java.util.List;
import java.util.OptionalLong;
import java.util.function.Supplier;

/** Provides milestone definitions for the Ethereum Mainnet network. */
public class MilestoneDefinitions {

  /**
   * Resolves the plan's ordered milestone entries into milestone definitions, mapping each built-in
   * hardfork reference to its protocol-spec factory method.
   *
   * @param specFactory the protocol spec factory
   * @param plan the protocol schedule plan
   * @return the milestone definitions, in the plan's canonical order
   */
  public static List<MilestoneDefinition> fromPlan(
      final MainnetProtocolSpecFactory specFactory, final ProtocolSchedulePlan plan) {
    return plan.milestoneEntries().stream()
        .map(entry -> toMilestoneDefinition(specFactory, entry))
        .toList();
  }

  private static MilestoneDefinition toMilestoneDefinition(
      final MainnetProtocolSpecFactory specFactory, final ForkEntry entry) {
    if (!(entry.effect() instanceof ScheduleEffect.BuiltInHardfork builtInHardfork)
        || !(builtInHardfork.hardforkId() instanceof MainnetHardforkId hardforkId)) {
      throw new IllegalArgumentException(
          "Milestone entry '" + entry.id() + "' does not reference a built-in mainnet hardfork");
    }
    final Supplier<ProtocolSpecBuilder> specBuilder = resolveSpecBuilder(specFactory, hardforkId);
    return switch (entry.activation()) {
      case Activation.BlockNumber blockNumber ->
          createBlockNumberMilestone(hardforkId, OptionalLong.of(blockNumber.value()), specBuilder);
      case Activation.Timestamp timestamp ->
          createTimestampMilestone(hardforkId, OptionalLong.of(timestamp.value()), specBuilder);
    };
  }

  private static Supplier<ProtocolSpecBuilder> resolveSpecBuilder(
      final MainnetProtocolSpecFactory specFactory, final MainnetHardforkId hardforkId) {
    return switch (hardforkId) {
      case FRONTIER -> specFactory::frontierDefinition;
      case HOMESTEAD -> specFactory::homesteadDefinition;
      case TANGERINE_WHISTLE -> specFactory::tangerineWhistleDefinition;
      case SPURIOUS_DRAGON -> specFactory::spuriousDragonDefinition;
      case BYZANTIUM -> specFactory::byzantiumDefinition;
      case CONSTANTINOPLE -> specFactory::constantinopleDefinition;
      case PETERSBURG -> specFactory::petersburgDefinition;
      case ISTANBUL -> specFactory::istanbulDefinition;
      case MUIR_GLACIER -> specFactory::muirGlacierDefinition;
      case BERLIN -> specFactory::berlinDefinition;
      case LONDON -> specFactory::londonDefinition;
      case ARROW_GLACIER -> specFactory::arrowGlacierDefinition;
      case GRAY_GLACIER -> specFactory::grayGlacierDefinition;
      case PARIS -> specFactory::parisDefinition;
      case SHANGHAI -> specFactory::shanghaiDefinition;
      case CANCUN -> specFactory::cancunDefinition;
      case PRAGUE -> specFactory::pragueDefinition;
      case OSAKA -> specFactory::osakaDefinition;
      case BPO1 -> specFactory::bpo1Definition;
      case BPO2 -> specFactory::bpo2Definition;
      case BPO3 -> specFactory::bpo3Definition;
      case BPO4 -> specFactory::bpo4Definition;
      case BPO5 -> specFactory::bpo5Definition;
      case AMSTERDAM -> specFactory::amsterdamDefinition;
      case FUTURE_EIPS -> specFactory::futureEipsDefinition;
      case EXPERIMENTAL_EIPS -> specFactory::experimentalEipsDefinition;
      default ->
          throw new IllegalArgumentException(
              "No built-in protocol spec definition for hardfork " + hardforkId);
    };
  }
}
