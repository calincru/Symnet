package clickfiletoexecutor

import org.change.parser.clickfile.ClickToAbstractNetwork
import org.change.v2.executor.clickabstractnetwork.{ClickExecutionContext, ClickExecutionContextBuilder}
import org.scalatest.{Matchers, FlatSpec}

/**
 * Author: Radu Stoenescu
 * Don't be a stranger,  symnetic.7.radustoe@spamgourmet.com
 */
class MamaIeiDeReteauaFacultatii extends FlatSpec with  Matchers {

  val clickFilePath = "src/main/resources/click_test_files/ASA-click.click"

  "The ASA" should "be parsable" in {
    val absNet = ClickToAbstractNetwork.buildConfig(clickFilePath)
    val executor = ClickExecutionContextBuilder.buildExecutionContext(absNet)

    executor shouldBe a [ClickExecutionContext]
  }

}
