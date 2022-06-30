package com.example.javaagent;

import com.example.service.Service;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;

import java.util.Collection;

/**
 * An example of using {@link io.opentelemetry.exporter.logging.LoggingSpanExporter} and {@link
 * io.opentelemetry.exporter.logging.LoggingMetricExporter}.
 */
public final class CustomHECLogExporter implements LogExporter {
    private static final String INSTRUMENTATION_NAME = CustomHECLogExporter.class.getName();

    public static String HEC_TOKEN = "";
    public static String SPLUNK_BASE_URL = "";

    private static Service service = new Service();

    public static void setHECToken(String token) {
        HEC_TOKEN = token;
    }
    public static void setSplunkBaseUrl(String baseUrl) {
        SPLUNK_BASE_URL = baseUrl;
    }

    public CustomHECLogExporter() {
    }

    @Override
    public CompletableResultCode export(Collection<LogData> logs) {

        if (logs != null && !logs.isEmpty()) {
            logs.stream().filter(log -> log.getSpanContext().isValid()).forEach(log -> {
                try {
                    service.sendEvents(log);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {

        System.out.println("Flush is executed.");
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {

        System.out.println("Shutdown is executed.");
        return CompletableResultCode.ofSuccess();
    }
}
