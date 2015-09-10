package org.change.v2.abstractnet.click.sefl

import org.change.v2.abstractnet.generic.{ConfigParameter, ElementBuilder, GenericElement, Port}
import org.change.v2.analysis.expression.concrete._
import org.change.v2.analysis.expression.concrete.nonprimitive._
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.analysis.processingmodels.{Instruction, LocationId}
import org.change.v2.util.conversion.RepresentationConversion._
import org.change.v2.util.canonicalnames._
import org.change.v2.analysis.memory.TagExp._
import org.change.v2.analysis.memory.Tag

class VLANEncap(name: String,
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
      Allocate("s"),
      Assign("s",:@(Tag("L2")+EtherSrc)),
      Allocate("d"),
      Assign("d",:@(Tag("L2")+EtherDst)),
      Deallocate(Tag("L2")+EtherSrc),
      Deallocate(Tag("L2")+EtherDst),
      CreateTag("L2",Tag("L2")-16),
      Allocate(Tag("L2")+EtherSrc,48),
      Assign(Tag("L2")+EtherSrc,:@("s")),
      Allocate(Tag("L2")+EtherDst,48),
      Assign(Tag("L2")+EtherDst,:@("d")),
      Allocate(Tag("L2")+EtherType,16),
      Assign(Tag("L2")+EtherType,ConstantValue(EtherProtoVLAN)),
      Deallocate("s"),
      Deallocate("d"),
      Forward(outputPortName(0))
    )
  )
}

class VLANEncapElementBuilder(name: String, elementType: String)
  extends ElementBuilder(name, elementType) {

  addInputPort(Port())
  addOutputPort(Port())

  override def buildElement: GenericElement = {
    new VLANEncap(name, elementType, getInputPorts, getOutputPorts, getConfigParameters)
  }
}

object VLANEncap {
  private var unnamedCount = 0

  private val genericElementName = "vlanencap"

  private def increment {
    unnamedCount += 1
  }

  def getBuilder(name: String): VLANEncapElementBuilder = {
    increment ; new VLANEncapElementBuilder(name, "VLANEncap")
  }

  def getBuilder: VLANEncapElementBuilder =
    getBuilder(s"$genericElementName-$unnamedCount")
}
