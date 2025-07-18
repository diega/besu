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
package org.hyperledger.besu.evm.operation;

import static org.hyperledger.besu.evm.internal.Words.clampedToLong;

import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.evm.Code;
import org.hyperledger.besu.evm.EVM;
import org.hyperledger.besu.evm.account.MutableAccount;
import org.hyperledger.besu.evm.code.CodeInvalid;
import org.hyperledger.besu.evm.frame.ExceptionalHaltReason;
import org.hyperledger.besu.evm.frame.MessageFrame;
import org.hyperledger.besu.evm.gascalculator.GasCalculator;
import org.hyperledger.besu.evm.internal.Words;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import org.apache.tuweni.bytes.Bytes;

/** The Abstract create operation. */
public abstract class AbstractCreateOperation extends AbstractOperation {

  /** The constant UNDERFLOW_RESPONSE. */
  protected static final OperationResult UNDERFLOW_RESPONSE =
      new OperationResult(0L, ExceptionalHaltReason.INSUFFICIENT_STACK_ITEMS);

  /** The constant UNDERFLOW_RESPONSE. */
  protected static final OperationResult INVALID_OPERATION =
      new OperationResult(0L, ExceptionalHaltReason.INVALID_OPERATION);

  /** The EOF Version this create operation requires initcode to be in */
  protected final int eofVersion;

  /**
   * Instantiates a new Abstract create operation.
   *
   * @param opcode the opcode
   * @param name the name
   * @param stackItemsConsumed the stack items consumed
   * @param stackItemsProduced the stack items produced
   * @param gasCalculator the gas calculator
   * @param eofVersion the EOF version this create operation is valid in
   */
  protected AbstractCreateOperation(
      final int opcode,
      final String name,
      final int stackItemsConsumed,
      final int stackItemsProduced,
      final GasCalculator gasCalculator,
      final int eofVersion) {
    super(opcode, name, stackItemsConsumed, stackItemsProduced, gasCalculator);
    this.eofVersion = eofVersion;
  }

  @Override
  public OperationResult execute(final MessageFrame frame, final EVM evm) {
    if (frame.getCode().getEofVersion() != eofVersion) {
      return INVALID_OPERATION;
    }

    // manual check because some reads won't come until the "complete" step.
    if (frame.stackSize() < getStackItemsConsumed()) {
      return UNDERFLOW_RESPONSE;
    }

    Supplier<Code> codeSupplier = Suppliers.memoize(() -> getInitCode(frame, evm));

    if (frame.isStatic()) {
      return new OperationResult(0, ExceptionalHaltReason.ILLEGAL_STATE_CHANGE);
    }

    final long cost = cost(frame, codeSupplier);
    if (frame.getRemainingGas() < cost) {
      return new OperationResult(cost, ExceptionalHaltReason.INSUFFICIENT_GAS);
    }
    final Wei value = Wei.wrap(frame.getStackItem(0));

    final Address address = frame.getRecipientAddress();
    final MutableAccount account = frame.getWorldUpdater().getAccount(address);

    frame.clearReturnData();

    Code code = codeSupplier.get();

    if (code != null && code.getSize() > evm.getMaxInitcodeSize()) {
      frame.popStackItems(getStackItemsConsumed());
      return new OperationResult(cost, ExceptionalHaltReason.CODE_TOO_LARGE);
    }

    if (value.compareTo(account.getBalance()) > 0
        || frame.getDepth() >= 1024
        || account.getNonce() == -1
        || code == null
        || code.getEofVersion() != frame.getCode().getEofVersion()) {
      fail(frame);
    } else {
      account.incrementNonce();

      if (!code.isValid()) {
        fail(frame);
      } else {
        frame.decrementRemainingGas(cost);
        spawnChildMessage(frame, code, evm);
        frame.incrementRemainingGas(cost);
      }
    }
    return new OperationResult(cost, null, getPcIncrement());
  }

  /**
   * How many bytes does this operation occupy?
   *
   * @return The number of bytes the operation and immediate arguments occupy
   */
  protected int getPcIncrement() {
    return 1;
  }

  /**
   * Cost operation.
   *
   * @param frame the frame
   * @param codeSupplier a supplier for the initcode, if needed for costing
   * @return the long
   */
  protected abstract long cost(final MessageFrame frame, Supplier<Code> codeSupplier);

  /**
   * Target contract address.
   *
   * @param frame the frame
   * @param initcode the initcode for the new contract.
   * @return the address
   */
  protected abstract Address generateTargetContractAddress(MessageFrame frame, Code initcode);

  /**
   * Gets the initcode that will be run.
   *
   * @param frame The message frame the operation executed in
   * @param evm the EVM executing the message frame
   * @return the initcode, raw bytes, unparsed and unvalidated
   */
  protected abstract Code getInitCode(MessageFrame frame, EVM evm);

  /**
   * Handles stack items when operation fails for validation reasons (noe enough ether, bad eof
   * code)
   *
   * @param frame the current execution frame
   */
  protected void fail(final MessageFrame frame) {
    final long inputOffset = clampedToLong(frame.getStackItem(1));
    final long inputSize = clampedToLong(frame.getStackItem(2));
    frame.readMutableMemory(inputOffset, inputSize);
    frame.popStackItems(getStackItemsConsumed());
    frame.pushStackItem(Bytes.EMPTY);
  }

  private void spawnChildMessage(final MessageFrame parent, final Code code, final EVM evm) {
    final Wei value = Wei.wrap(parent.getStackItem(0));

    final Address contractAddress = generateTargetContractAddress(parent, code);
    final Bytes inputData = getInputData(parent);

    final long childGasStipend =
        gasCalculator().gasAvailableForChildCreate(parent.getRemainingGas());
    parent.decrementRemainingGas(childGasStipend);

    // frame addition is automatically handled by parent messageFrameStack
    MessageFrame.builder()
        .parentMessageFrame(parent)
        .type(MessageFrame.Type.CONTRACT_CREATION)
        .initialGas(childGasStipend)
        .address(contractAddress)
        .contract(contractAddress)
        .inputData(inputData)
        .sender(parent.getRecipientAddress())
        .value(value)
        .apparentValue(value)
        .code(code)
        .completer(child -> complete(parent, child, evm))
        .build();

    parent.setState(MessageFrame.State.CODE_SUSPENDED);
  }

  /**
   * Get the input data to be appended to the EOF factory contract. For CREATE and CREATE2 this is
   * always empty
   *
   * @param frame the message frame the operation was called in
   * @return the input data as raw bytes, or `Bytes.EMPTY` if there is no aux data
   */
  protected Bytes getInputData(final MessageFrame frame) {
    return Bytes.EMPTY;
  }

  private void complete(final MessageFrame frame, final MessageFrame childFrame, final EVM evm) {
    frame.setState(MessageFrame.State.CODE_EXECUTING);

    frame.incrementRemainingGas(childFrame.getRemainingGas());
    frame.addLogs(childFrame.getLogs());
    frame.addSelfDestructs(childFrame.getSelfDestructs());
    frame.addCreates(childFrame.getCreates());
    frame.popStackItems(getStackItemsConsumed());

    if (childFrame.getState() == MessageFrame.State.COMPLETED_SUCCESS) {
      Code outputCode =
          (childFrame.getCreatedCode() != null)
              ? childFrame.getCreatedCode()
              : evm.wrapCodeForCreation(childFrame.getOutputData());
      if (outputCode.isValid()) {
        Address createdAddress = childFrame.getContractAddress();
        frame.pushStackItem(Words.fromAddress(createdAddress));
        frame.setReturnData(Bytes.EMPTY);
        onSuccess(frame, createdAddress);
      } else {
        frame.getWorldUpdater().deleteAccount(childFrame.getRecipientAddress());
        frame.setReturnData(childFrame.getOutputData());
        frame.pushStackItem(Bytes.EMPTY);
        onInvalid(frame, (CodeInvalid) outputCode);
      }
    } else {
      frame.setReturnData(childFrame.getOutputData());
      frame.pushStackItem(Bytes.EMPTY);
      onFailure(frame, childFrame.getExceptionalHaltReason());
    }

    final int currentPC = frame.getPC();
    frame.setPC(currentPC + getPcIncrement());
  }

  /**
   * Called when the child {@code CONTRACT_CREATION} message has completed successfully, used to
   * give library users a chance to do implementation specific logic.
   *
   * @param frame the frame running the successful operation
   * @param createdAddress the address of the newly created contract
   */
  protected void onSuccess(final MessageFrame frame, final Address createdAddress) {
    // no-op by default
  }

  /**
   * Called when the child {@code CONTRACT_CREATION} message has failed to execute, used to give
   * library users a chance to do implementation specific logic.
   *
   * @param frame the frame running the successful operation
   * @param haltReason the exceptional halt reason of the child frame
   */
  protected void onFailure(
      final MessageFrame frame, final Optional<ExceptionalHaltReason> haltReason) {
    // no-op by default
  }

  /**
   * Called when the child {@code CONTRACT_CREATION} message has completed successfully but the
   * returned contract is invalid per chain rules, used to give library users a chance to do
   * implementation specific logic.
   *
   * @param frame the frame running the successful operation
   * @param invalidCode the code object containing the invalid code
   */
  protected void onInvalid(final MessageFrame frame, final CodeInvalid invalidCode) {
    // no-op by default
  }
}
