/*
 * X-Ray configuration for Apache Derby >= 10.11.1.1.
 */

import static eu.coherentpaas.xray.config.AgentConfig.xray
import eu.coherentpaas.xray.graph.GraphAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import eu.coherentpaas.xray.logback.RawSocketAppender
import reactor.logback.AsyncAppender

statusListener(OnConsoleStatusListener)

println("Loading XRay instrumentation for CQE.")

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}

root(ERROR, ["STDOUT"])

logger("xray", ALL, ["STDOUT"], false)

