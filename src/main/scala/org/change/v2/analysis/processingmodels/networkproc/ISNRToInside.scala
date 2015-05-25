package org.change.v2.analysis.processingmodels.networkproc

import org.change.v2.analysis.processingmodels.instructions.{Dup, Same}
import org.change.v2.analysis.processingmodels.{InstructionBlock, State, Instruction}
/**
 * Author: Radu Stoenescu
 * Don't be a stranger,  symnetic.7.radustoe@spamgourmet.com
 */
object ISNRToInside extends Instruction {
  /**
   *
   * A state processing block produces a set of new states based on a previous one.
   *
   * @param s
   * @return
   */
  override def apply(s: State): (List[State], List[State]) =
    InstructionBlock(
      Same("SEQ", "New-SEQ"),
      Dup("SEQ", "Old-SEQ")
    )(s)
}
