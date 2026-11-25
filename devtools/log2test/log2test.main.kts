#!/usr/bin/env kotlin
/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2026 Niels Basjes
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

@file:DependsOn("nl.basjes.parse.httpdlog:httpdlog-parser:6.0.0")
@file:DependsOn("nl.basjes.parse.useragent:yauaa:8.3.0")
@file:DependsOn("org.apache.logging.log4j:log4j-api:2.26.1")
@file:DependsOn("org.apache.logging.log4j:log4j-core:2.26.1")
@file:DependsOn("org.apache.logging.log4j:log4j-slf4j2-impl:2.26.1")

import nl.basjes.parse.core.Field
import nl.basjes.parse.core.exceptions.DissectionFailure
import nl.basjes.parse.httpdlog.HttpdLoglineParser
import nl.basjes.parse.useragent.UserAgentAnalyzer
import java.util.TreeMap
import java.util.TreeSet
import kotlin.io.path.Path
import kotlin.io.path.useLines
import kotlin.io.path.writeText

data class LogRecord(
    @set:Field("HTTP.USERAGENT:request.user-agent")                         var userAgent                   : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua")                      var secChUa                     : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-arch")                 var secChUaArch                 : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-bitness")              var secChUaBitness              : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-form-factors")         var secChUaFormFactors          : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-full-version")         var secChUaFullVersion          : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-full-version-list")    var secChUaFullVersionList      : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-mobile")               var secChUaMobile               : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-model")                var secChUaModel                : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-platform")             var secChUaPlatform             : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-platform-version")     var secChUaPlatformVersion      : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-wow64")                var secChUaWow64                : String? = null,
) : Comparable<LogRecord> {
    fun asHeadersMap(): Map<String, String> = TreeMap<String, String>().apply {
        userAgent               ?.let { put("User-Agent",                   it) }
        secChUa                 ?.let { put("Sec-Ch-Ua",                    it) }
        secChUaArch             ?.let { put("Sec-Ch-Ua-Arch",               it) }
        secChUaBitness          ?.let { put("Sec-Ch-Ua-Bitness",            it) }
        secChUaFormFactors      ?.let { put("Sec-Ch-Ua-Form-Factors",       it) }
        secChUaFullVersion      ?.let { put("Sec-Ch-Ua-Full-Version",       it) }
        secChUaFullVersionList  ?.let { put("Sec-Ch-Ua-Full-Version-List",  it) }
        secChUaMobile           ?.let { put("Sec-Ch-Ua-Mobile",             it) }
        secChUaModel            ?.let { put("Sec-Ch-Ua-Model",              it) }
        secChUaPlatform         ?.let { put("Sec-Ch-Ua-Platform",           it) }
        secChUaPlatformVersion  ?.let { put("Sec-Ch-Ua-Platform-Version",   it) }
        secChUaWow64            ?.let { put("Sec-Ch-Ua-Wow64",              it) }
    }

    override fun compareTo(other: LogRecord): Int = compareValuesBy(
        this,
        other,
        LogRecord::userAgent,
        LogRecord::secChUa,
        LogRecord::secChUaArch,
        LogRecord::secChUaBitness,
        LogRecord::secChUaFormFactors,
        LogRecord::secChUaFullVersion,
        LogRecord::secChUaFullVersionList,
        LogRecord::secChUaMobile,
        LogRecord::secChUaModel,
        LogRecord::secChUaPlatform,
        LogRecord::secChUaPlatformVersion,
        LogRecord::secChUaWow64,
    )
}

fun main(args:Array<String>) {

    if (args.isEmpty()) {
        System.err.println("Usage: kotlin log2test.main.kts <format> [filename ...]")
        return
    }

    if (args.size == 1) {
        // Print all possible values for the logformat
        val dummyParser = HttpdLoglineParser(Any::class.java, args[0])
        dummyParser.possiblePaths.forEach(::println)
        return
    }

    // 1: Parse all provided lines and put them in Records with ONLY the needed fields.
    //    Because they are put in a TreeSet we get deduplication on insert.

    val logFormat = args[0]
    val parser = HttpdLoglineParser(LogRecord::class.java, logFormat)
    val records = TreeSet<LogRecord>()


    args.drop(1)
        .forEach { fileName ->
            Path(fileName)
                .useLines { lines ->
                    lines
                        .filter { it.isNotBlank() }
                        .forEach { line ->
                            try {
                                records.add(parser.parse(line))
                            } catch (failure: DissectionFailure) {
                                System.err.println("DissectionFailure : ${failure.message}")
                            }
                        }
                }
        }

    // 2: Drop some unwanted ones
    // Some names (mostly Fediverse tools) cause an explosion in the number of useragents.
    val ignoredAgents =
        setOf("http.rb", "Akkoma", "Calckey", "Construct/", "Dendrite", "Friendica", "Misskey", "Pleroma")
    val unwantedRecords: Set<LogRecord> = records.filter {
        it.asHeadersMap().values.any { value ->
            ignoredAgents.any { ignored ->
                value.contains(
                    ignored,
                    ignoreCase = true
                )
            }
        }
    }.toSet()
    if (unwantedRecords.isNotEmpty()) {
        records.removeAll(unwantedRecords)
    }

    // 3: Put all unique records through the Yauaa analyzer
    val analyzer: UserAgentAnalyzer = UserAgentAnalyzer.newBuilder().immediateInitialization().withoutCache().build()
    var fileNumber = 1
    var recordCount = 0
    val content = StringBuilder(1_000_000)
    for (record in records) {
        content.append(analyzer.parse(record.asHeadersMap()).toYamlTestCase())
        println(++recordCount)

        // Limit the entries per file to keep them manageable
        if (recordCount % 1000 == 0) {
            Path("New_Agents_%04d.yaml".format(fileNumber)).writeText(content.toString())
            fileNumber++
            content.setLength(0)
        }
    }

// Write anything that remains
    if (content.isNotEmpty()) {
        Path("New_Agents_%04d.yaml".format(fileNumber)).writeText(content.toString())
    }

}


try {
    main(args)
} catch (e: Exception) {
    println("${e.stackTraceToString()} : ${e.message}")
}
