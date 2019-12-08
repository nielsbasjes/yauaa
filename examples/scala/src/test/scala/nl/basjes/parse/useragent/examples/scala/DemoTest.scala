package nl.basjes.parse.useragent.examples.scala

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.scalatest.FlatSpec

@RunWith(classOf[JUnitRunner])
class DemoTest extends FlatSpec {
  "The parser" must "extract the correct DeviceName" in {
    val demo = new Demo()
    val userAgent = "Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD90Z) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.124 Mobile Safari/537.36"
    val result = demo.parse(userAgent)
    assert(result.toXML.contains("<DeviceName>Google Nexus 6</DeviceName>"))
  }
}
