package org.change.v2.abstractnet.click.sefl

import org.change.v2.abstractnet.generic.{ConfigParameter, ElementBuilder, GenericElement, Port}
import org.change.v2.analysis.expression.concrete.ConstantValue
import org.change.v2.analysis.expression.concrete.nonprimitive.{:@, Symbol}
import org.change.v2.analysis.memory.TagExp
import org.change.v2.analysis.processingmodels.Instruction
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.util.regexes._
import org.change.v2.util.canonicalnames._
import org.change.v2.util.conversion.RepresentationConversion._

/**
 * radu
 * 3/5/14
 */
class IPRewriter(name: String,
         inputPorts: List[Port],
         outputPorts: List[Port],
         configParams: List[ConfigParameter])
  extends GenericElement(name,
    "IPRewriter",
    inputPorts,
    outputPorts,
    configParams) {

  def installRewritePatternForIPAddress(whichRule: Int, portA: Int, portB: Int,
                                        symbolNameSuffix: String, sAddr: TagExp,
                                        dAddr: TagExp, withwhat: String): Instruction = {
    val rwAddress = ipToNumber(withwhat)

    InstructionBlock(
      AssignNamedSymbol(s"$name-$whichRule-check-" + symbolNameSuffix, :@(sAddr)),
      AssignNamedSymbol(s"$name-$whichRule-apply-" + symbolNameSuffix, ConstantValue(rwAddress))
    )
  }

  private def installKeepPatterns(whichRule: Int, portA: Int, portB: Int) = InstructionBlock(
    // Install forward mappings
    AssignNamedSymbol(s"$name-$whichRule-check-sa", Symbol(IPSrcString)),
    AssignNamedSymbol(s"$name-$whichRule-check-da", Symbol(IPDstString)),
    AssignNamedSymbol(s"$name-$whichRule-check-sp", Symbol(PortSrcString)),
    AssignNamedSymbol(s"$name-$whichRule-check-dp", Symbol(PortDstString)),
    AssignNamedSymbol(s"$name-$whichRule-check-proto", Symbol(L4ProtoString)),

    AssignNamedSymbol(s"$name-$whichRule-apply-sa", Symbol(IPSrcString)),
    AssignNamedSymbol(s"$name-$whichRule-apply-da", Symbol(IPDstString)),
    AssignNamedSymbol(s"$name-$whichRule-apply-sp", Symbol(PortSrcString)),
    AssignNamedSymbol(s"$name-$whichRule-apply-dp", Symbol(PortDstString)),
    AssignNamedSymbol(s"$name-$whichRule-apply-fwport", ConstantValue(portA)),
    // Install reply mappings
    AssignNamedSymbol(s"$name-${whichRule+1}-check-sa", Symbol(IPDstString)),
    AssignNamedSymbol(s"$name-${whichRule+1}-check-da", Symbol(IPSrcString)),
    AssignNamedSymbol(s"$name-${whichRule+1}-check-sp", Symbol(PortDstString)),
    AssignNamedSymbol(s"$name-${whichRule+1}-check-dp", Symbol(PortSrcString)),
    AssignNamedSymbol(s"$name-${whichRule+1}-check-proto", Symbol(L4ProtoString)),

    AssignNamedSymbol(s"$name-${whichRule+1}-apply-sa", Symbol(IPDstString)),
    AssignNamedSymbol(s"$name-${whichRule+1}-apply-da", Symbol(IPSrcString)),
    AssignNamedSymbol(s"$name-${whichRule+1}-apply-sp", Symbol(PortDstString)),
    AssignNamedSymbol(s"$name-${whichRule+1}-apply-dp", Symbol(PortSrcString)),
    AssignNamedSymbol(s"$name-${whichRule+1}-apply-fwport", ConstantValue(portB)),
    // Forward the flow
    Forward(outputPortName(portA))
  )

  private val installInstructions: scala.collection.mutable.Map[String, Instruction] = scala.collection.mutable.Map()
  private var lastCheck = -1
  private val fwPorts: scala.collection.mutable.Map[Int, Int] = scala.collection.mutable.Map()

  def buildCheckAndApplyFor(inputPort: String): Instruction = {
    val buildCache: scala.collection.mutable.Map[Int, Instruction] = scala.collection.mutable.Map()

    def looper(which: Int): Instruction = buildCache.getOrElse(which, {
      val elseInstr = if (which == lastCheck)
        installInstructions(inputPort)
      else
        looper(which + 1)


      val i = If(ConstrainNamedSymbol(IPSrcString, :==:(Symbol(s"$name-$which-check-sa"))),
          If(ConstrainNamedSymbol(IPDstString, :==:(Symbol(s"$name-$which-check-da"))),
            If(ConstrainNamedSymbol(PortSrcString, :==:(Symbol(s"$name-$which-check-sp"))),
              If(ConstrainNamedSymbol(PortDstString, :==:(Symbol(s"$name-$which-check-dp"))),
                If(ConstrainNamedSymbol(L4ProtoString, :==:(Symbol(s"$name-$which-check-proto"))),
                  InstructionBlock(
                    AssignNamedSymbol(IPSrcString, Symbol(s"$name-$which-apply-sa")),
                    AssignNamedSymbol(IPDstString, Symbol(s"$name-$which-apply-da")),
                    AssignNamedSymbol(PortSrcString, Symbol(s"$name-$which-apply-sp")),
                    AssignNamedSymbol(PortDstString, Symbol(s"$name-$which-apply-dp")),
                    // Biggie here
                    Forward(outputPortName(fwPorts(which)))
                  ),
                  elseInstr
                ),
                elseInstr
              ),
              elseInstr
            ),
            elseInstr
          ),
          elseInstr
        )
      buildCache += (which -> i)
      i
    })

    looper(0)
  }

  def buildSpecIntructions(inputspec: String, port: Int) = inputspec match {
    case IPRewriter.keepPattern(fwPort, rpPort) => {
      installInstructions += (inputPortName(port) -> installKeepPatterns(port * 2, fwPort.toInt, rpPort.toInt))
      lastCheck += 2
      fwPorts += (2 * port -> fwPort.toInt)
      fwPorts += (2 * port + 1-> rpPort.toInt)
      buildCheckAndApplyFor(inputPortName(port))
    }
    case IPRewriter.passPattern(outputPort) => Forward(outputPortName(outputPort.toInt))
    case "drop" | "discard" => NoOp
  }

  private val iCache: scala.collection.mutable.Map[String, Instruction] = scala.collection.mutable.Map()

  for (
    (cp, i) <- configParams.zipWithIndex
  ) {
    iCache += (inputPortName(i) -> buildSpecIntructions(cp.value, i))
  }

  override val instructions = iCache.toMap

  override def outputPortName(which: Int): String = s"$name-out-$which"
  override def inputPortName(which: Int): String = s"$name-in-$which"
}

class IPRewriterElementBuilder(name: String)
  extends ElementBuilder(name, "IPRewriter") {

  addInputPort(Port())
  addOutputPort(Port())

  override def buildElement: GenericElement = {
    new IPRewriter(name, getInputPorts, getOutputPorts, getConfigParameters)
  }
}

object IPRewriter {
  val passPattern = ("pass (" + number+ ")").r
  val keepPattern = ("keep (" + number+ ") (" + number+ ")").r
  val addrPattern = ipv4+ "|-"
  val portPattern = number + "|" + number + "-" + number + "|-"
  val rewritePattern = ("pattern (" + addrPattern + ") (" + portPattern + ") (" + addrPattern + ") (" +
    portPattern + ") (" + number + ") (" + number + ")").r

  private var unnamedCount = 0

  private val genericElementName = "ipRewriter"

  private def increment {
    unnamedCount += 1
  }

  def getBuilder(name: String): IPRewriterElementBuilder = {
    increment ; new IPRewriterElementBuilder(name)
  }

  def getBuilder: IPRewriterElementBuilder =
    getBuilder(s"$genericElementName-$unnamedCount")
}
