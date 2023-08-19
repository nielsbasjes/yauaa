/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

File buildLog = new File( basedir, 'build.log' )
assert buildLog.exists()

assert 1 <= buildLog.getText().count("YauaaVersion")
assert 1 == buildLog.getText().count("For more information: https://yauaa.basjes.nl")
assert 1 == buildLog.getText().count("using expression: classpath*:UserAgents/")
assert 1 == buildLog.getText().count("Building all matchers for all possible fields.")
assert 1 == buildLog.getText().count("Preheating JVM by running ")
assert 1 == buildLog.getText().count("Initializing Analyzer data structures")
