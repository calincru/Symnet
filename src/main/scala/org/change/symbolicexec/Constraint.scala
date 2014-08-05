package org.change.symbolicexec

trait Constraint {
  /**
   * Materialize a constraint as the set of possible values.
   * @param valueType For bound checks.
   */
  def asSet(valueType: NumericType = NumericType()): List[Interval] = Nil
}

case class LT(v: Long) extends Constraint {
  override def asSet(valueType: NumericType): List[(Long, Long)] = {
    List((valueType.min, Math.min(v-1, valueType.max)))
  }
}
case class LTE(v: Long) extends Constraint {
  override def asSet(valueType: NumericType): List[(Long, Long)] = {
    List((valueType.min, Math.min(v, valueType.max)))
  }
}
case class GT(v: Long) extends Constraint {
  override def asSet(valueType: NumericType): List[(Long, Long)] = {
    List((Math.max(v+1, valueType.min), valueType.max))
  }
}
case class GTE(v: Long) extends Constraint{
  override def asSet(valueType: NumericType): List[(Long, Long)] = {
    List((Math.max(v, valueType.min), valueType.max))
  }
}
case class E(v: Long) extends Constraint{
  override def asSet(valueType: NumericType): List[(Long, Long)] =
    if (valueType.min <= v && v <= valueType.max) List((v,v))
    else Nil
}
case class Range(v1: Long, v2: Long) extends Constraint {
  override def asSet(valueType: NumericType): List[(Long, Long)] = {
    List((Math.max(v1, valueType.min), Math.min(v2, valueType.max)))
  }
}
case class RangeSeries(ranges: List[Range]) extends Constraint {
  override def asSet(valueType: NumericType): List[(Long, Long)] =
// Warning: The result should be normalized
    ranges.map(_.asSet(valueType)).flatten
}