package tagarithm

import org.scalatest.{FlatSpec, Matchers}
import org.change.v2.analysis.expression.concrete.nonprimitive.{:@, Symbol, :+:}
import org.change.v2.analysis.expression.concrete.{ConstantValue, SymbolicValue}
import org.change.v2.analysis.memory.{Tag, Value, MemorySpace}
import org.change.v2.analysis.processingmodels.{State}
import org.change.v2.analysis.processingmodels.instructions._
import org.scalatest.{Matchers, FlatSpec}
import org.change.v2.analysis.memory.TagExp._

/**
 * Author: Radu Stoenescu
 * Don't be a stranger,  symnetic.7.radustoe@spamgourmet.com
 */
class TagTests extends FlatSpec with Matchers{

  "Tag arithmetic" should "work" in {
    val s = State.bigBang

    val m = s.memory.Tag("L3", 10).get.Tag("L4", 100).get

    val sPrime = State(m)

    (Tag("L4") + 10 + Tag("L3") - 5)(sPrime) should be (Some(115))
  }

  "Tag instruction" should "properly set memory tags" in {
    val s = State.bigBang

    val finalState = InstructionBlock (
      CreateTag("L3", 10),
      CreateTag("L4", Tag("L3") + 15 - 2 + Tag("L3"))
    )(s)

    finalState._1.head.memory.memTags("L4") should be (33)
  }

  "Basic instructions involving tags" should "work" in {
    val s = State.bigBang

    val finalState = InstructionBlock (
      CreateTag("L3", 10),
      Allocate(Tag("L3")+20, 10),
      Assign(Tag("L3")+20, SymbolicValue()),
      CreateTag("L4", Tag("L3") + 20),
      Assign("VAL", :@(Tag("L4")))
    )(s)

    finalState._1.head.memory.eval("VAL").get.e.id should be (finalState._1.head.memory.eval(30).get.e.id)
  }

}
