package org.change.v2.analysis.processingmodels

import org.change.v2.analysis.memory.MemorySpace

import org.change.v2.util.canonicalnames._
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.analysis.expression.concrete.SymbolicValue


/**
 * Author: Radu Stoenescu
 * Don't be a stranger,  symnetic.7.radustoe@spamgourmet.com
 */
case class State(memory: MemorySpace = MemorySpace.clean,
                 history: List[LocationId] = Nil,
                 errorCause: Option[ErrorCause] = None,
                 instructionHistory: List[Instruction] = Nil) {
  def location: LocationId = history.head
  def forwardTo(locationId: LocationId): State = State(memory, locationId :: history, errorCause, instructionHistory)
  def status = errorCause.getOrElse("OK")
  override def toString = s"Path ($status) {\n$memory\n} End Of Path Desc"
  def addInstructionToHistory(i: Instruction) = State(memory, history, errorCause, i :: instructionHistory)
}

object State {
 def bigBang: State = {
   val init = State(MemorySpace.clean)
   InstructionBlock (
//     AssignNamedSymbol(IPSrc, SymbolicValue()),
//     AssignNamedSymbol(IPDst, SymbolicValue()),
//     AssignNamedSymbol(PortSrc, SymbolicValue()),
//     AssignNamedSymbol(PortDst, SymbolicValue()),
//     Assign(Proto, SymbolicValue()),
//     AssignNamedSymbol(TTL, SymbolicValue()),
//     Assign(IPLength, SymbolicValue()),
//     Assign(IPHeaderLength, SymbolicValue()),
//     Assign(IPID, SymbolicValue()),
     //     Assign(Payload, SymbolicValue())
//     AssignNamedSymbol(IPVersion, SymbolicValue())
   )(init)._1.head
 }
}
