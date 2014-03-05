package org.netvisor.runtime.server.processing.start.processors

import org.netvisor.runtime.server.request.{StringField, FileField, Field}
import org.apache.commons.io.IOUtils
import java.io.{File, FileOutputStream}
import org.netvisor.parser.abstractnet.ClickToAbstractNetwork
import org.netvisor.runtime.server.processing.ParamPipelineElement

/**
 * radu
 * 3/5/14
 */
object ParseAndCheck extends ParamPipelineElement {

  def apply(v1: Map[String, Field]): Boolean = {
    v1.get("click_file") match {
      case Some(FileField(_, contents)) => {
        v1.get("id") match {
          case Some(FileField(_,identity)) => {
            val id = IOUtils.toString(identity)

            v1.get("vmName") match {
              case Some(StringField(_, name)) => {
                val abstractNet = ClickToAbstractNetwork.buildConfig(contents, id + name)

                println("Parsed: " + abstractNet)

                true
              }
              case None => false
            }

          }

          case None => false
        }
      }

      case None => false
    }
  }
}
