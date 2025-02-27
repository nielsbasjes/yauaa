/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2025 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.example

import nl.basjes.parse.useragent.{UserAgent, UserAgentAnalyzer}

class Demo {

  private val uaa = UserAgentAnalyzer
    .newBuilder()
    .withCache(1234)
    .withField("DeviceClass")
    .withAllFields()
    .build

  def parse(userAgent: String): UserAgent = {
    uaa.parse(userAgent)
  }

}
