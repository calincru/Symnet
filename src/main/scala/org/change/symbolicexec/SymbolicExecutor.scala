package org.change.symbolicexec

import parser.generic.{PathComponent, NetworkConfig}

/**
 * The execution process is governed by a SymbolicExecutor process.
 *
 * It's job is to:
 * - execute runnable processing blocks (those reached by a certain path)
 * - advance paths between connected modules (cross the links)
 * - enforce integrity and security constraints
 * - [eventually] filter unwanted paths
 *
 * @param parsedModel
 */
class SymbolicExecutor(parsedModel: NetworkConfig) {

  private val processingBlocks: Map[String, ProcessingBlock] = parsedModel.elements.map( e => (e._1, e._2.toProcessingBlock) )
  private val links: Map[PathLocation, PathLocation]=  (for {
    segm: List[PathComponent] <- parsedModel.paths
  } yield {
    (for {
      window <- segm.sliding(2)
      e1 = window.head
      e2 = window.last
    } yield (PathLocation(e1._1, e1._3, Output), PathLocation(e2._1, e2._2, Input))).toList
  }).flatten.toMap

  override def toString = "Processing elements:\n" +
    processingBlocks.map(_._2.toString()).mkString("\n") +
    "\nLinks:\n" +
    links.mkString("\n")

  /**
   * This run one step of symbolic execution.
   * @param paths Currently explored paths.
   * @return Next explored paths.
   */
  private def step(paths: List[Path]): (List[Path], List[Path]) = {
//    Propagate across links
    val (needPropagation, others) = paths.partition( _.location.accessPointType == Output)
    val (propagateable, stuck) = needPropagation.partition( p => links.contains(p.location))
    val afterPropagation = propagateable.map( p => p.move(links(p.location)))

    // Warning: We should assert no paths on an output port in existence

    val afterProcessing = (afterPropagation ++ others).map( p => {
      processingBlocks(p.location.processingBlockId).process(p)
    }).flatten.toList

    (afterProcessing, stuck)
  }

  def execute(input: List[Path]): List[Path] = if (! input.isEmpty) {

      println("=======================================================================================================")
      println(s"Exploring:\n${input.mkString("\n")}")
      val (next, stuck) = step(input)
      println(s"Stuck:\n${stuck.mkString("\n")}")
      println("=======================================================================================================")

      stuck ++ execute(next)
    } else {
      Nil
    }

  def execute(input: Path): List[Path] = execute(List(input))
}
