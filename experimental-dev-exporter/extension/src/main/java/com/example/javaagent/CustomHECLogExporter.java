package com.example.javaagent;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

/**
 * An example of using {@link io.opentelemetry.exporter.logging.LoggingSpanExporter} and {@link
 * io.opentelemetry.exporter.logging.LoggingMetricExporter}.
 */
public final class CustomHECLogExporter implements LogExporter {
    private static final String INSTRUMENTATION_NAME = CustomHECLogExporter.class.getName();

    public CustomHECLogExporter() {
    }

    @Override
    public CompletableResultCode export(Collection<LogData> logs) {
        System.out.println("Export method executed.");
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost("http://127.0.0.1:8088/services/collector/raw");
        httpPost.addHeader("Authorization", "Splunk 2e4391ff-5370-489a-8545-ead7856ac345");

        if (logs != null && !logs.isEmpty()) {
            for (LogData log : logs) {
                try {
                    httpPost.setEntity(new StringEntity(log.toString()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        HttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpEntity entity = response.getEntity();
        System.out.println("response: " + entity);
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