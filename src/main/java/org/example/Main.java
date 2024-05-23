package org.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");

    public static void main(String[] args) throws InterruptedException {
        OpenTelemetry openTelemetry = setUpOpenTelemetry();

        while (true) {
            createSpan(openTelemetry);
            Thread.sleep(5000);
        }
    }

    private static void createSpan(OpenTelemetry openTelemetry) {
        Span span = openTelemetry.getTracer("SomeTracer").spanBuilder("Some Span")
                .setAttribute("mytime", DATE_FORMAT.format(new Date()))
                .startSpan();
        span.end();
        System.out.println("Span created");
    }

    private static OpenTelemetry setUpOpenTelemetry() {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(
                        SdkTracerProvider.builder()
                                .addSpanProcessor(SimpleSpanProcessor.create(getSpanExporter()))
                                .addResource(Resource.create(Attributes.builder().put("service.name", "dummy service").build()))
                                .build())
                .build();
    }

    private static SpanExporter getSpanExporter() {
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://127.0.0.1:8200").build();
    }
}