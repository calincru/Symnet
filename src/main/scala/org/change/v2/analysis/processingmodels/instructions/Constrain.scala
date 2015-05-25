package org.change.v2.analysis.processingmodels.instructions

import org.change.v2.analysis.constraint.Constraint
import org.change.v2.analysis.processingmodels.{State, Instruction}

/**
 * Author: Radu Stoenescu
 * Don't be a stranger,  symnetic.7.radustoe@spamgourmet.com
 */
case class Constrain(id: String, c: Constraint) extends Instruction {
  /**
   *
   * A state processing block produces a set of new states based on a previous one.
   *
   * @param s
   * @return
   */
  override def apply(s: State): (List[State], List[State]) = {
    optionToStatePair(s, "Error during 'constrain'") {
      _.memory.CONSTRAIN(id, c)
    }
  }
}
