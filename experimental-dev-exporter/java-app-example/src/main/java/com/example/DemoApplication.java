package com.example;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        Tracer tracer = GlobalOpenTelemetry.get().getTracerProvider().get(DemoApplication.class.getName()); // getTracer(DemoApplication.class.getName());
        Span parentSpan = tracer.spanBuilder("parent").startSpan();

        try (Scope scope = parentSpan.makeCurrent()) {
            Logger logger = LogManager.getLogger(DemoApplication.class);
            logger.info("Logging started.");
            logger.info("Testing info logs.");
            logger.warn("Testing Warning logs.");
            logger.info("Main method is about to finish.");
            logger.error("Error log is also included here.");
            logger.debug("Is there anything to debug here?");
        } finally {
            parentSpan.end();
        }
    }

}
