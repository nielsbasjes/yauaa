package nl.basjes.parse.useragent.examples.scala

import nl.basjes.parse.useragent.{UserAgent, UserAgentAnalyzer}

class Demo {

  private val uaa = UserAgentAnalyzer
    .newBuilder()
    .withCache(1234)
    .withField("DeviceClass")
    .withAllFields()
    .build

  def parse(userAgent: String) : UserAgent = {
    uaa.parse(userAgent)
  }

}
