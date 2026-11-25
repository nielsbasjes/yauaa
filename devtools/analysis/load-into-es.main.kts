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

@file:DependsOn("nl.basjes.parse.useragent:yauaa:8.3.1-SNAPSHOT")
@file:DependsOn("nl.basjes.parse.httpdlog:httpdlog-parser:6.0.0")
@file:DependsOn("co.elastic.clients:elasticsearch-java:9.5.7")
@file:DependsOn("co.elastic.clients:elasticsearch-rest5-client:9.5.7")
@file:DependsOn("org.json:json:20260719")
@file:DependsOn("org.apache.logging.log4j:log4j-api:2.26.1")
@file:DependsOn("org.apache.logging.log4j:log4j-core:2.26.1")
@file:DependsOn("org.apache.logging.log4j:log4j-slf4j2-impl:2.26.1")

import nl.basjes.parse.core.Field
import nl.basjes.parse.useragent.UserAgentAnalyzer
import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.elasticsearch.core.BulkRequest
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest5_client.Rest5ClientTransport
import co.elastic.clients.transport.rest5_client.low_level.Rest5Client
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.http.message.BasicHeader
import org.json.JSONObject
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.LinkedHashMap
import java.util.Locale
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.isRegularFile
import kotlin.system.exitProcess

private val PARALLELISM = 2
private val QUEUE_CAPACITY = 1024
private val BULK_SIZE = 1000
private val INDEX_PREFIX = "useragents"

private data class LogRecord(
    var count: Long = 1,
    @set:Field("TIME.EPOCH:request.receive.time.epoch")                  var epoch                      : String? = null,
    @set:Field("TIME.YEAR:request.receive.time.year_utc")                var yearUtc                    : String? = null,
    @set:Field("TIME.MONTH:request.receive.time.month_utc")              var monthUtc                   : String? = null,
    @set:Field("TIME.DAY:request.receive.time.day_utc")                  var dayUtc                     : String? = null,
    @set:Field("TIME.HOUR:request.receive.time.hour_utc")                var hourUtc                    : String? = null,
    @set:Field("TIME.MINUTE:request.receive.time.minute_utc")            var minuteUtc                  : String? = null,
    @set:Field("TIME.SECOND:request.receive.time.second_utc")            var secondUtc                  : String? = null,
    @set:Field("TIME.MILLISECOND:request.receive.time.millisecond_utc")  var millisecondUtc             : String? = null,
    @set:Field("TIME.MICROSECOND:request.receive.time.microsecond_utc")  var microsecondUtc             : String? = null,
    @set:Field("HTTP.USERAGENT:request.user-agent")                      var userAgent                  : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua")                   var secChUa                    : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-arch")              var secChUaArch                : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-bitness")           var secChUaBitness             : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-full-version")      var secChUaFullVersion         : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-full-version-list") var secChUaFullVersionList     : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-mobile")            var secChUaMobile              : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-model")             var secChUaModel               : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-platform")          var secChUaPlatform            : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-platform-version")  var secChUaPlatformVersion     : String? = null,
    @set:Field("HTTP.HEADER:request.header.sec-ch-ua-wow64")             var secChUaWow64               : String? = null,
) : Comparable<LogRecord> {
    val date: LocalDate
        get() = LocalDate.of(yearUtc!!.toInt(), monthUtc!!.toInt(), dayUtc!!.toInt())

    private var cachedJson: String? = null
    private var cachedYaml: String? = null
    private var cachedUseragentCacheKey: Int? = null

    fun asHeadersMap(): Map<String, String> = LinkedHashMap<String, String>().apply {
        userAgent               ?.let { put("User-Agent",                       it) }
        secChUa                 ?.let { put("Sec-Ch-Ua",                        it) }
        secChUaArch             ?.let { put("Sec-Ch-Ua-Arch",                   it) }
        secChUaBitness          ?.let { put("Sec-Ch-Ua-Bitness",                it) }
        secChUaFullVersion      ?.let { put("Sec-Ch-Ua-Full-Version",           it) }
        secChUaFullVersionList  ?.let { put("Sec-Ch-Ua-Full-Version-List",      it) }
        secChUaMobile           ?.let { put("Sec-Ch-Ua-Mobile",                 it) }
        secChUaModel            ?.let { put("Sec-Ch-Ua-Model",                  it) }
        secChUaPlatform         ?.let { put("Sec-Ch-Ua-Platform",               it) }
        secChUaPlatformVersion  ?.let { put("Sec-Ch-Ua-Platform-Version",       it) }
        secChUaWow64            ?.let { put("Sec-Ch-Ua-Wow64",                  it) }
    }

    fun toJson(): String {
        if (cachedJson == null) {
            cachedJson = JSONObject(asHeadersMap()).toString()
        }
        return cachedJson!!
    }

    override fun toString(): String {
        if (cachedYaml == null) {
            cachedYaml = buildString {
                append("- test:\n")
                append("    input:\n")
                appendYamlField("User-Agent                   ", userAgent)
                appendYamlField("Sec-Ch-Ua                    ", secChUa)
                appendYamlField("Sec-Ch-Ua-Arch               ", secChUaArch)
                appendYamlField("Sec-Ch-Ua-Bitness            ", secChUaBitness)
                appendYamlField("Sec-Ch-Ua-Full-Version       ", secChUaFullVersion)
                appendYamlField("Sec-Ch-Ua-Full-Version-List  ", secChUaFullVersionList)
                appendYamlField("Sec-Ch-Ua-Mobile             ", secChUaMobile)
                appendYamlField("Sec-Ch-Ua-Model              ", secChUaModel)
                appendYamlField("Sec-Ch-Ua-Platform           ", secChUaPlatform)
                appendYamlField("Sec-Ch-Ua-Platform-Version   ", secChUaPlatformVersion)
                appendYamlField("Sec-Ch-Ua-Wow64              ", secChUaWow64)
            }
        }
        return cachedYaml!!
    }

    private fun StringBuilder.appendYamlField(header: String, value: String?) {
        value?.let {
            append("      ").append(header).append(": '").append(it.replace("'", "''")).append("'\n")
        }
    }

    val useragentCacheKey: Int
        get() {
            if (cachedUseragentCacheKey == null) {
                cachedUseragentCacheKey = toString().hashCode()
            }
            return cachedUseragentCacheKey!!
        }

    override fun compareTo(other: LogRecord): Int = compareValuesBy(
        this,
        other,
        LogRecord::userAgent,
        LogRecord::secChUa,
        LogRecord::secChUaArch,
        LogRecord::secChUaBitness,
        LogRecord::secChUaFullVersion,
        LogRecord::secChUaFullVersionList,
        LogRecord::secChUaMobile,
        LogRecord::secChUaModel,
        LogRecord::secChUaPlatform,
        LogRecord::secChUaPlatformVersion,
        LogRecord::secChUaWow64,
    )
}

private data class UARecord(
    val logRecord: LogRecord,
    val parseResults: Map<String, String>,
)

private data class WorkerInput(val record: LogRecord?)

private fun parseRecord(line: String): LogRecord? {
    if (line.isBlank() || line.startsWith("day")) {
        return null
    }

    val lineFormat = Regex("(20\\d{2})-(\\d{2})-(\\d{2})\\t([^\\t]*)\\t([^\\t]*)\\t([0-9]+)")

    val fields = lineFormat.find(line)?.destructured
    val yearUtc             = fields?.component1()
    val monthUtc            = fields?.component2()
    val dayUtc              = fields?.component3()
    val classification      = fields?.component4()
    val userAgent           = fields?.component5()
    val count               = fields?.component6()

    requireNotNull(fields)         { "Parse Error: $line" }
    requireNotNull(yearUtc)        { "Parse Error: $line" }
    requireNotNull(monthUtc)       { "Parse Error: $line" }
    requireNotNull(dayUtc)         { "Parse Error: $line" }
    requireNotNull(classification) { "Parse Error: $line" }
    requireNotNull(userAgent)      { "Parse Error: $line" }
    requireNotNull(count)          { "Parse Error: $line" }

    return LogRecord(
        count          = count.toLong(),
        yearUtc        = yearUtc,
        monthUtc       = monthUtc,
        dayUtc         = dayUtc,
        hourUtc        = "12",
        minuteUtc      = "00",
        secondUtc      = "00",
        millisecondUtc = "000",
        microsecondUtc = "000000",
        userAgent      = userAgent,
    ).apply {
        epoch = date.atTime(12, 0).toInstant(ZoneOffset.UTC).toEpochMilli().toString()
    }
}

private fun createAnalyzer(): UserAgentAnalyzer = UserAgentAnalyzer
    .newBuilder()
    .showMatcherLoadStats()
    .immediateInitialization()
    .dropTests()
    .build()

private fun <T> putUnlessFailed(
    queue: BlockingQueue<T>,
    item: T,
    pipelineFailure: AtomicReference<Throwable?>,
) {
    while (!queue.offer(item, 100, TimeUnit.MILLISECONDS)) {
        pipelineFailure.get()?.let { throw IllegalStateException("A loader worker failed", it) }
    }
    pipelineFailure.get()?.let { throw IllegalStateException("A loader worker failed", it) }
}

private class ElasticsearchBulkWriter(
    endpoint: String,
    username: String?,
    password: String?,
) : AutoCloseable {
    private val client: ElasticsearchClient

    init {
        require(username.isNullOrBlank() == password.isNullOrBlank()) {
            "Set both ES_USERNAME and ES_PASSWORD, or neither"
        }

        val uri = URI.create(endpoint)
        require(uri.scheme in setOf("http", "https") && !uri.host.isNullOrBlank()) {
            "ES_URL must be an absolute HTTP or HTTPS URL: $endpoint"
        }

        val restClientBuilder = Rest5Client.builder(HttpHost(uri.scheme, uri.host, uri.port))
        uri.rawPath?.trimEnd('/')?.takeIf { it.isNotEmpty() }?.let(restClientBuilder::setPathPrefix)
        if (!username.isNullOrBlank()) {
            val credentials = "$username:$password".toByteArray(StandardCharsets.UTF_8)
            val authorization = "Basic ${Base64.getEncoder().encodeToString(credentials)}"
            restClientBuilder.setDefaultHeaders(arrayOf(BasicHeader("Authorization", authorization)))
        }

        val transport = Rest5ClientTransport(restClientBuilder.build(), JacksonJsonpMapper())
        client = ElasticsearchClient(transport)
    }

    fun write(records: List<UARecord>) {
        if (records.isEmpty()) {
            return
        }

        val request = BulkRequest.Builder()
        records.forEach { record ->
            val date = record.logRecord.date
            val index = "${INDEX_PREFIX}_${String.format(Locale.ROOT, "%04d-%02d", date.year, date.monthValue)}"
            request.operations { operation ->
                operation.create { create ->
                    create.index(index).document(toDocument(record))
                }
            }
        }

        val response = client.bulk(request.build())
        if (response.errors()) {
            val failures = response.items()
                .filter { it.error() != null }
                .take(5)
                .joinToString(separator = "; ") { "${it.status()} ${it.error()}" }
            error("Elasticsearch rejected bulk items: $failures")
        }
    }

    private fun toDocument(record: UARecord): Map<String, Any> {
        var inputJson = record.logRecord.toJson()
        if (inputJson.length > 255) {
            inputJson = inputJson.substring(0, 250) + "|..."
        }

        val timestamp = record.logRecord.date
            .atTime(12, 0)
            .toInstant(ZoneOffset.UTC)
            .atOffset(ZoneOffset.UTC)

        return mapOf(
            "@timestamp" to DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(timestamp),
            "count" to 1,
            "ua" to inputJson,
            "parsed" to record.parseResults,
        )
    }

    override fun close() {
        client.close()
    }
}

private fun inputFiles(args: Array<String>): List<Path> {
    val files = if (args.isNotEmpty()) {
        args.map { Path.of(it) }
    } else {
        Files.list(Path.of(".")).use { paths ->
            paths.filter { it.isRegularFile() && it.fileName.toString().endsWith(".txt") }
                .sorted()
                .toList()
        }
    }

    val missingFiles = files.filterNot { it.isRegularFile() }
    require(missingFiles.isEmpty()) { "Input file(s) not found: ${missingFiles.joinToString()}" }
    return files
}

fun main(args: Array<String>) {
    require(PARALLELISM > 0) { "PARALLELISM must be greater than zero" }

    val files = inputFiles(args)
    if (files.isEmpty()) {
        println("No input files found (expected *.txt in the current directory)")
        return
    }

    val endpoint = System.getenv("ES_URL")?.takeIf { it.isNotBlank() } ?: "http://127.0.0.1:9200"
    val writer = ElasticsearchBulkWriter(
        endpoint = endpoint,
        username = System.getenv("ES_USERNAME"),
        password = System.getenv("ES_PASSWORD"),
    )
    val workerQueues = List(PARALLELISM) { ArrayBlockingQueue<WorkerInput>(QUEUE_CAPACITY) }
    val resultQueue = ArrayBlockingQueue<UARecord>(PARALLELISM * QUEUE_CAPACITY)
    val pipelineFailure = AtomicReference<Throwable?>()
    val producerFinished = AtomicBoolean(false)
    val workersFinished = CountDownLatch(PARALLELISM)
    val inputOk = AtomicLong()
    val inputBad = AtomicLong()

    val workerThreads = workerQueues.mapIndexed { workerId, queue ->
        Thread({
            try {
                val analyzer = createAnalyzer()
                while (true) {
                    val record = queue.take().record ?: break
                    val userAgent = analyzer.parse(record.asHeadersMap())
                    val parseResults = userAgent.toMap(userAgent.availableFieldNamesSorted)
                    putUnlessFailed(resultQueue, UARecord(record, parseResults), pipelineFailure)
                }
            } catch (failure: InterruptedException) {
                Thread.currentThread().interrupt()
                pipelineFailure.compareAndSet(null, failure)
            } catch (failure: Throwable) {
                pipelineFailure.compareAndSet(null, failure)
            } finally {
                workersFinished.countDown()
            }
        }, "uaa-worker-$workerId")
    }

    val producerThread = Thread({
        try {
            for (file in files) {
                Files.newBufferedReader(file).use { reader ->
                    var lineNumber = 0
                    while (true) {
                        if (Thread.currentThread().isInterrupted) {
                            throw InterruptedException("Input producer interrupted")
                        }
                        val line = reader.readLine() ?: break
                        lineNumber++

                        val record = try {
                            parseRecord(line)
                        } catch (failure: Exception) {
                            val badCount = inputBad.incrementAndGet()
                            System.err.println("Skipping invalid record at $file:$lineNumber: ${failure.message}")
                            if ((inputOk.get() + badCount) % 10_000 == 0L) {
                                println("Input : Ok: ${inputOk.get()} | Bad: $badCount")
                            }
                            null
                        } ?: continue

                        val worker = Math.floorMod(record.useragentCacheKey, PARALLELISM)
                        putUnlessFailed(workerQueues[worker], WorkerInput(record), pipelineFailure)
                        val okCount = inputOk.incrementAndGet()
                        if ((okCount + inputBad.get()) % 10_000 == 0L) {
                            println("Input : Ok: $okCount | Bad: ${inputBad.get()}")
                        }
                    }
                }
            }

            workerQueues.forEach { putUnlessFailed(it, WorkerInput(null), pipelineFailure) }
        } catch (failure: Throwable) {
            pipelineFailure.compareAndSet(null, failure)
        } finally {
            producerFinished.set(true)
        }
    }, "loader-input")

    workerThreads.forEach(Thread::start)
    producerThread.start()

    val batch = ArrayList<UARecord>(BULK_SIZE)
    var indexedRecords = 0L
    try {
        while (!producerFinished.get() || workersFinished.count > 0L || resultQueue.isNotEmpty()) {
            pipelineFailure.get()?.let { throw IllegalStateException("Loading failed", it) }
            val record = resultQueue.poll(100, TimeUnit.MILLISECONDS) ?: continue
            batch.add(record)
            if (batch.size == BULK_SIZE) {
                writer.write(batch)
                indexedRecords += batch.size
                batch.clear()
            }
        }

        pipelineFailure.get()?.let { throw IllegalStateException("Loading failed", it) }
        if (batch.isNotEmpty()) {
            writer.write(batch)
            indexedRecords += batch.size
        }
        println("Loaded $indexedRecords records; input ok=${inputOk.get()}, bad=${inputBad.get()}, parallelism=$PARALLELISM")
    } finally {
        producerThread.interrupt()
        workerThreads.forEach(Thread::interrupt)
        producerThread.join()
        workerThreads.forEach(Thread::join)
        writer.close()
    }
}

try {
    main(args)
} catch (failure: Exception) {
    System.err.println("${failure.message}\n${failure.stackTraceToString()}")
    exitProcess(1)
}
