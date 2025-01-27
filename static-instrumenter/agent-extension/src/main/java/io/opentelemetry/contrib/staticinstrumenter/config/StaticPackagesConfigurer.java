/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.contrib.staticinstrumenter.config;

import com.google.auto.service.AutoService;
import io.opentelemetry.instrumentation.api.config.Config;
import io.opentelemetry.javaagent.tooling.bootstrap.BootstrapPackagesBuilder;
import io.opentelemetry.javaagent.tooling.bootstrap.BootstrapPackagesConfigurer;

/**
 * Makes classes from {@link io.opentelemetry.contrib.staticinstrumenter.agent.main} package
 * available both in agent's premain and static instrumenter's main.
 */
@AutoService(BootstrapPackagesConfigurer.class)
public class StaticPackagesConfigurer implements BootstrapPackagesConfigurer {

  @Override
  public void configure(Config config, BootstrapPackagesBuilder builder) {
    builder.add("io.opentelemetry.contrib.staticinstrumenter.agent.main");
  }
}
