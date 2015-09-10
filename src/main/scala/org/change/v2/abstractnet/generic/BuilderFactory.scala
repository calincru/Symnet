package org.change.v2.abstractnet.generic

import org.change.v2.Template
import org.change.v2.abstractnet.click.sefl._

object BuilderFactory {

  def getBuilder(nameValue: String, elementType: String) = elementType match {
    case "ToDevice" | "ToNetPort" | "ToNetFront" => ToDevice.getBuilder(nameValue)
    case "FromDevice" | "FromNetPort" | "FromNetFront" => FromDevice.getBuilder(nameValue)
    //    case "SEQChanger" => SEQChanger.getBuilder(nameValue)
    //    case "StartTunnel" => StartTunnel.getBuilder(nameValue)
    //    case "EndTunnel" => EndTunnel.getBuilder(nameValue)
    //    case "Firewall" => Firewall.getBuilder(nameValue)
    //    case "NAT" => NAT.getBuilder(nameValue)
    //    case "Client" => Client.getBuilder(nameValue)
    //    case "Server"  => Server.getBuilder(nameValue)
    case "IPRewriter" => IPRewriter.getBuilder(nameValue)
    case "IPEncap" => IPEncap.getBuilder(nameValue)
    case "EtherEncap" => EtherEncap.getBuilder(nameValue)
    case "IPDecap" => IPDecap.getBuilder(nameValue)
    case "StripIPHeader" => StripIPHeader.getBuilder(nameValue)
    case "CheckIPHeader" => CheckIPHeader.getBuilder(nameValue)
    case "Template" => Template.getBuilder(nameValue)
    //    case "IPFilter"  => IPFilter.getBuilder(nameValue)
    case "IPClassifier" => IPClassifier.getBuilder(nameValue)
    case "Paint" => Paint.getBuilder(nameValue)
    case _ => NoOpClickElm.getBuilder(nameValue, elementType)
  }
  def getBuilder(elementType: String) = elementType match {
    case "ToDevice" | "ToNetPort" | "ToNetFront" => ToDevice.getBuilder
    case "FromDevice" | "FromNetPort" | "FromNetFront" => FromDevice.getBuilder
//    case "SEQChanger" => SEQChanger.getBuilder
//    case "StartTunnel" => StartTunnel.getBuilder
//    case "EndTunnel" => EndTunnel.getBuilder
//    case "Firewall" => Firewall.getBuilder
//    case "NAT" => NAT.getBuilder
//    case "Client" => Client.getBuilder
//    case "Server" => Server.getBuilder
    case "IPRewriter"  => IPRewriter.getBuilder
    case "IPEncap" => IPEncap.getBuilder
    case "EtherEncap" => EtherEncap.getBuilder
    case "IPDecap" => IPDecap.getBuilder
    case "StripIPHeader" => StripIPHeader.getBuilder
    case "CheckIPHeader" => CheckIPHeader.getBuilder
    case "Template"  => Template.getBuilder
//    case "IPFilter"  => IPFilter.getBuilder
    case "IPClassifier"  => IPClassifier.getBuilder
    case "Paint"  => Paint.getBuilder
    case _ => NoOpClickElm.getBuilder(elementType)
  }
}
