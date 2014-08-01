package org.change.runtime.cli

import java.io.FileInputStream
import java.io.File
import org.change.parser.abstractnet.ClickToAbstractNetwork
import org.change.symbolicexec.SymbolicExecutor

/**
 * radu
 * 3/5/14
 */
object ParseSingleClick {

  def main(args: Array[String]) {
    if (args.length < 1) {
      println("Usage: <click file> [haskell|symb]")
    }

    val inputFile = new File(args(0))
    val option = if (args.length > 1 ) args(1) else "haskell"

    val networkAbstract = ClickToAbstractNetwork.buildConfig(inputFile)

    option match {
      case "haskell" => println(networkAbstract.asHaskellWithRuleNumber())
      case "symb" => println(new SymbolicExecutor(networkAbstract))
    }

  }

}
