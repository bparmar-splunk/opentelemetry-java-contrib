/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.example.javaagent;

import com.google.auto.service.AutoService;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.logs.SdkLogEmitterProviderBuilder;
import io.opentelemetry.sdk.logs.export.LogExporter;
import io.opentelemetry.sdk.logs.export.SimpleLogProcessor;

/**
 * This is one of the main entry points for Instrumentation Agent's customizations. It allows
 * configuring the {@link AutoConfigurationCustomizer}. See the {@link
 * #customize(AutoConfigurationCustomizer)} method below.
 *
 * <p>Also see https://github.com/open-telemetry/opentelemetry-java/issues/2022
 *
 * @see AutoConfigurationCustomizerProvider
 */
@AutoService(AutoConfigurationCustomizerProvider.class)
public class LogExporterAutoConfigurationCustomizerProvider
        implements AutoConfigurationCustomizerProvider {

    @Override
    public void customize(AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration.addLogEmitterProviderCustomizer(this::configureLogExporter);
    }

    private SdkLogEmitterProviderBuilder configureLogExporter(SdkLogEmitterProviderBuilder builder, ConfigProperties properties) {
        String hecToken = properties.getString("hec.token");
        String baseUrl = properties.getString("splunk.base.url");
        CustomHECLogExporter.setHECToken(hecToken == null || hecToken.isEmpty() ? "" : hecToken);
        CustomHECLogExporter.setSplunkBaseUrl(baseUrl == null || baseUrl.isEmpty() ? "https://127.0.0.1:8088" : baseUrl);
        return builder.addLogProcessor(SimpleLogProcessor.create(new CustomHECLogExporter()));
    }
}
