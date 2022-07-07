package com.example.javaagent;

import com.example.service.Service;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An example of using {@link io.opentelemetry.exporter.logging.LoggingSpanExporter} and {@link
 * io.opentelemetry.exporter.logging.LoggingMetricExporter}.
 */
public final class CustomHECLogExporter implements LogExporter {

    private static final Logger LOGGER = Logger.getLogger(CustomHECLogExporter.class.getName());

    public static String HEC_TOKEN = "";
    public static String SPLUNK_BASE_URL = "";

    private static Service service = new Service();

    public static void setHECToken(String token) {
        HEC_TOKEN = token;
    }

    public static void setSplunkBaseUrl(String baseUrl) {
        SPLUNK_BASE_URL = baseUrl;
    }

    @Override
    public CompletableResultCode export(Collection<LogData> logs) {

        if (logs != null && !logs.isEmpty()) {
            logs.stream().filter(log -> log.getSpanContext().isValid()).forEach(log -> {
                try {
                    service.sendEvents(log);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
            });
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {

        LOGGER.info("CustomHECLogExporter::flush() executed.");
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {

        LOGGER.info("CustomHECLogExporter::shutdown() executed.");
        return CompletableResultCode.ofSuccess();
    }
}
