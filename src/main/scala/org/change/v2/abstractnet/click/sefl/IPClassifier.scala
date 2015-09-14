package org.change.v2.abstractnet.click.sefl

import org.change.v2.abstractnet.generic.{ConfigParameter, ElementBuilder, GenericElement, Port}
import org.change.v2.analysis.expression.concrete._
import org.change.v2.analysis.processingmodels._
import org.change.v2.analysis.processingmodels.instructions._
import org.change.v2.util.canonicalnames._
import org.change.v2.util.conversion.NumberFor
import org.change.v2.util.conversion.RepresentationConversion._
import org.change.v2.util.regexes._

class IPClassifier(name: String,
                    inputPorts: List[Port],
                    outputPorts: List[Port],
                    configParams: List[ConfigParameter])
  extends GenericElement(name,
    "IPClassifier",
    inputPorts,
    outputPorts,
    configParams) {

  val lastIndex = configParams.length - 1

  // Supported condition formats.
  val conditionSeparator = """\s+(and|&&)\s+"""

  val ipProto = ("ip proto (" + number + ")").r

  val srcHostAddr = ("src host (" + ipv4 + ")").r
  val srcNetAddr = ("src net (" + ipv4 + ")/(" + number + ")").r
  val srcNetExplicitAddr = ("src net (" + ipv4 + ") mask (" + ipv4 + ")").r

  val dstHostAddr = ("dst host (" + ipv4 + ")").r
  val dstNetAddr = ("dst net (" + ipv4 + ")/(" + number + ")").r
  val dstNetExplicitAddr = ("dst net (" + ipv4 + ") mask (" + ipv4 + ")").r

  val srcPort = ("src (tcp|udp) port (" + number + ")").r
  val dstPort = ("dst (tcp|udp) port (" + number + ")").r

  val etherSrc = ("ether src (" + macCisco +")").r
  val etherDst = ("ether dst (" + macCisco +")").r

  val tcp = "tcp".r
  val udp = "udp".r

  private def conditionToConstraint(condition: String): Instruction = condition match {
    case ipProto(v) => ConstrainRaw(IPVersion, :==:(ConstantValue(v.toInt)))

    case srcHostAddr(ip) => ConstrainRaw(IPSrc, :==:(ConstantValue(ipToNumber(ip))))
    case dstHostAddr(ip) => ConstrainRaw(IPDst, :==:(ConstantValue(ipToNumber(ip))))

    case etherSrc(macSrc) => ConstrainRaw(EtherSrc, :==:(ConstantValue(macToNumberCiscoFormat(macSrc))))
    case etherDst(macDst) => ConstrainRaw(EtherDst, :==:(ConstantValue(macToNumberCiscoFormat(macDst))))

    case dstNetAddr(ip, mask) => {
      val (lower, upper) = ipAndMaskToInterval(ip, mask)
      ConstrainRaw(IPDst, :&:(:>=:(ConstantValue(lower)), :<=:(ConstantValue(upper))))
    }
    case dstNetExplicitAddr(ip, mask) => {
      val (lower, upper) = ipAndExplicitMaskToInterval(ip, mask)
      ConstrainRaw(IPDst, :&:(:>=:(ConstantValue(lower)), :<=:(ConstantValue(upper))))
    }
    case srcNetAddr(ip, mask) => {
      val (lower, upper) = ipAndMaskToInterval(ip, mask)
      ConstrainRaw(IPSrc, :&:(:>=:(ConstantValue(lower)), :<=:(ConstantValue(upper))))
    }
    case srcNetExplicitAddr(ip, mask) => {
      val (lower, upper) = ipAndExplicitMaskToInterval(ip, mask)
      ConstrainRaw(IPSrc, :&:(:>=:(ConstantValue(lower)), :<=:(ConstantValue(upper))))
    }

    case srcPort(port) => ConstrainRaw(TcpSrc, :==:(ConstantValue(port.toInt)))
    case dstPort(port) => ConstrainRaw(TcpDst, :==:(ConstantValue(port.toInt)))

    case tcp() => ConstrainRaw(Proto, :==:(ConstantValue(TCPProto)))
    case udp() => ConstrainRaw(Proto, :==:(ConstantValue(UDPProto)))

  }

  val any = """\s*(true|\-)\s*""".r
  val none = """\s*false\s*""".r

  val portToInstr = scala.collection.mutable.Map[Int, Instruction]()

  for {
    (p,i) <- configParams.zipWithIndex.reverse
  } {
    portToInstr += ((i, paramsToInstructionBlock(p.value,i)))
  }

  override def instructions: Map[LocationId, Instruction] = Map( inputPortName(0) -> portToInstr(0) )

  def paramsToInstructionBlock(param: String, whichOne: Int): Instruction = param match {
    case any(_) => Forward(outputPortName(whichOne))
    case none() => if (whichOne < lastIndex)
        portToInstr(whichOne + 1)
      else
        NoOp
    case _ => {
      val conditions = param.split(conditionSeparator).toList

      def conditionsToInstruction(conds: List[String]): Instruction = {
        val cond = conds.head
        If(conditionToConstraint(cond),
          if (conds.length == 1)
            Forward(outputPortName(whichOne))
          else
            conditionsToInstruction(conds.tail),
          if (whichOne < lastIndex)
            portToInstr(whichOne + 1)
          else
            NoOp
        )
      }

      conditionsToInstruction(conditions)
    }
  }

  override def outputPortName(which: Int): String = s"$name-out-$which"

}

class IPClassifierElementBuilder(name: String)
  extends ElementBuilder(name, "IPClassifier") {

  addInputPort(Port())

  override def buildElement: GenericElement = {
    new IPClassifier(name, getInputPorts, getOutputPorts, getConfigParameters)
  }

  override def handleConfigParameter(paramString: String): ElementBuilder = {
    super.handleConfigParameter(paramString)
    addOutputPort(Port(Some(paramString)))
  }
}

object IPClassifier {

  private var unnamedCount = 0

  private val genericElementName = "IPClassifier"

  private def increment {
    unnamedCount += 1
  }

  def getBuilder(name: String): IPClassifierElementBuilder = {
    increment ; new IPClassifierElementBuilder(name)
  }

  def getBuilder: IPClassifierElementBuilder =
    getBuilder(s"$genericElementName-$unnamedCount")
}