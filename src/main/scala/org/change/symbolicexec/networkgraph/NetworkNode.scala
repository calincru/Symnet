package org.change.symbolicexec.networkgraph

import parser.generic._
import parser.specific.FromDevice

/**
 * Every node has a processing block, referred by elementId and
 * a set of transitions exiting it. These transitions are defined by
 *  1. The source port
 *  2. The destination node
 *  3, The destination port
 *
 * For every source port, there should be only one transition. (i.e no
 * source port should link two  different nodes - ambiguity risk)
 * @param elementId
 * @param eLinks
 */
case class NetworkNode(val elementId: String,
                       val vmId: String,
                       var eLinks: scala.collection.mutable.Map[Int, (NetworkNode, Int)] = scala.collection.mutable.Map()) {

  def findFirstThat(p: (NetworkNode) => Boolean): Option[NetworkNode] =
    if (p(this)) Some(this)
    else eLinks.values.map(_._1.findFirstThat(p)).find(_ match {case Some(n) => true}).getOrElse(None)

  /**
   * Return the first node referringg to the element baring the 'id'
   * @param id
   * @return
   */
  def findFirstOccurrence(id: String): Option[NetworkNode] = findFirstThat(_.elementId equals id)

  /**
   * Finds in the current tree the first node baring the same element and then
   * fuses the eLinks of both nodes.
   *
   * If the same port is linked in both nodes, results are unpredictible.
   * @param n Fused node
   * @return Fuse is possible or not (when the fuse node in the main tree is absent)
   */
  def linkNode(n : NetworkNode): Boolean = findFirstOccurrence(n.elementId) match {
    case Some(nn) => {
//      Merge links
      nn.eLinks = nn.eLinks ++ n.eLinks
      true
    }
    case None => false
  }

  def addExplicitLink(descendatNode: NetworkNode, source: Int, destination: Int): Unit = {
    eLinks += ((source, (descendatNode, destination)))
  }

  def removeExplicitLink(source: Int): Unit = {
    eLinks -= (source)
  }

  override def toString = elementId + eLinks.map( kv => s"[${kv._1}] -> [${kv._2._2}] ${kv._2._1.toString}").mkString("\n")
}

object NetworkNode {
  type Path = List[PathComponent]

  def buildFromParsedModel(parsedNetworkModel: NetworkConfig, vmId: String, whatRoot: (NetworkNode) => Boolean): NetworkNode = {
//    Build the tree forest corresponding to config
    val paths = parsedNetworkModel.paths.map(buildFromPath(_, vmId))
//    Select the root node
    import org.change.utils.collection.extractFirst
    val (root, others) = extractFirst(paths)(whatRoot)
//    Merge other trees to this root one
    others.map(root.linkNode(_))
    root
  }

  def treeRootedAtSource = { (model: NetworkConfig, vmId: String) =>
    buildFromParsedModel(model,vmId, ( e =>
        model.elements(e.elementId) match {
          case _:FromDevice => true
          case _ => false
        }
      )
    )
  }

  def treeRootedAt(rootElementId: String) = { (model: NetworkConfig, vmId: String) =>
    buildFromParsedModel(model, vmId, (e => e.elementId == rootElementId))
  }

  /**
   * Builds a tree out of a path.
   * @param path
   * @return
   */
  private def buildFromPath(path: List[PathComponent], vmId: String): NetworkNode = {
    val nodes = path.map(c => NetworkNode(c._1, vmId))
    for {
      (link, nodes) <- path.sliding(2).toIterable.zip(nodes.sliding(2).toIterable)
      sourceNode = nodes.head
      destNode = nodes.last
      sourcePort = link.head._3
      destinationPort = link.last._2
    } {
      sourceNode.eLinks += ((sourcePort, (destNode, destinationPort)))
    }
    nodes.head
  }
}


