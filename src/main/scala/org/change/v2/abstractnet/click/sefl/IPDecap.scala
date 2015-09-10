package org.change.v2.abstractnet.click.sefl


import org.change.v2.abstractnet.generic.{ConfigParameter, ElementBuilder, GenericElement, Port}
import org.change.v2.analysis.expression.concrete._
import org.change.v2.analysis.expression.concrete.nonprimitive._
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.analysis.processingmodels.{Instruction, LocationId}
import org.change.v2.util.canonicalnames._
import org.change.v2.analysis.memory.TagExp._
import org.change.v2.analysis.memory.Tag

class IPDecap(name: String,
                   elementType: String,
                   inputPorts: List[Port],
                   outputPorts: List[Port],
                   configParams: List[ConfigParameter])
  extends GenericElement(name,
    elementType,
    inputPorts,
    outputPorts,
    configParams) {

  override def instructions: Map[LocationId, Instruction] = Map(
    inputPortName(0) -> InstructionBlock(
      Constrain(ProtoOffset, :==:(ConstantValue(configParams(0).value.toInt))),
      Deallocate(ProtoOffset, 8),
      Deallocate(IPDstOffset, 32),
      Deallocate(IPSrcOffset, 32),
      Deallocate(IPVersionOffset, 4),
      Assign("t",:@(IPLengthOffset)),
      Deallocate(IPLengthOffset, 16),
      Constrain("t",:==:(:+:(:@(IPLengthOffset),ConstantValue(20)))),
      Deallocate(IPHeaderLengthOffset, 4),
      Deallocate(TTLOffset, 8),
      Deallocate(IPIDOffset, 16),
      Forward(outputPortName(0))
    )
  )
}

class IPDecapElementBuilder(name: String, elementType: String)
  extends ElementBuilder(name, elementType) {

  addInputPort(Port())
  addOutputPort(Port())

  override def buildElement: GenericElement = {
    new IPDecap(name, elementType, getInputPorts, getOutputPorts, getConfigParameters)
  }
}

object IPDecap {
  private var unnamedCount = 0

  private val genericElementName = "ipdecap"

  private def increment {
    unnamedCount += 1
  }

  def getBuilder(name: String): IPDecapElementBuilder = {
    increment ; new IPDecapElementBuilder(name, "IPDecap")
  }

  def getBuilder: IPDecapElementBuilder =
    getBuilder(s"$genericElementName-$unnamedCount")
}
