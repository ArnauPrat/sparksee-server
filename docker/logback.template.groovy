import static eu.coherentpaas.xray.config.AgentConfig.xray
import eu.coherentpaas.xray.graph.GraphAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

statusListener(OnConsoleStatusListener)

println("Loading XRay instrumentation for CQE.")

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}

appender("SOCKET", SocketAppender) {
	    port = 12345
	    remoteHost = "REPLACE_XRAY_HOST"   // or wherever is xray-central...
}


root(WARN, ["STDOUT"])
xray("com.tinkerpop.gremlin.sparksee.structure") {
	instrument("SparkseeGraph") {

		log("compute(Ljava/lang/Long;Ljava/lang/String;Ljava/util/Map;)Ljava/lang/String;")
		receive("compute(Ljava/lang/Long;Ljava/lang/String;Ljava/util/Map;)Ljava/lang/String;", "#0")

		/*log("commit(Ljava/lang/Long;Ljava/lang/Long;)Ljava/lang/String;")
		receive("commit(Ljava/lang/Long;Ljava/lang/Long;)Ljava/lang/String;", "#0")

		log("rollback(Ljava/lang/Long;)Ljava/lang/String;")
		receive("rollback(Ljava/lang/Long;)Ljava/lang/String;", "#0")

		log("getWS(Ljava/lang/Long;)Ljava/lang/String;")
		receive("getWS(Ljava/lang/Long;)Ljava/lang/String;", "#0")

		// TODO: El next no te transactionId! El primer parametre es queryId
		log("next(Ljava/lang/Long;Ljava/lang/Long;)Ljava/lang/String;")
		receive("next(Ljava/lang/Long;Ljava/lang/Long;)Ljava/lang/String;", "#0")*/

		// El redo no te transactionId i segurament no n'ha de tenir. Potser no cal ni 
		// instrumentar-lo o com a minim no cal relacionar-ho amb cap transaccio. 
		//log("redoWS(Ljava/lang/Long;Ljava/lang/Long;)Ljava/lang/String;")
		//receive("redoWS(Ljava/lang/Long;Ljava/lang/Long;)Ljava/lang/String;", "#0")
	}
}