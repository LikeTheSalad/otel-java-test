package org.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.SimpleLogRecordProcessor;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

public class Main {
    public static void main(String[] args) {
        OpenTelemetry openTelemetry = setUpOpenTelemetry();

        Logger logger = openTelemetry.getLogsBridge().loggerBuilder("LoggerScope").build();

        logger.logRecordBuilder().setBody("Some body").emit();
    }

    private static OpenTelemetry setUpOpenTelemetry() {
        return OpenTelemetrySdk.builder()
                .setLoggerProvider(
                        SdkLoggerProvider.builder()
                                .addLogRecordProcessor(SimpleLogRecordProcessor.create(OtlpJsonLoggingLogRecordExporter.create()))
                                .build()
                ).setTracerProvider(
                        SdkTracerProvider.builder()
                                .addSpanProcessor(SimpleSpanProcessor.create(OtlpJsonLoggingSpanExporter.create()))
                                .build())
                .build();
    }
}