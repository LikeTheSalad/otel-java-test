package org.example;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
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
        Protocol protocol = Protocol.GRPC;
        OpenTelemetry openTelemetry = setUpOpenTelemetry(protocol);

        while (true) {
            createSpan(openTelemetry, protocol);
            Thread.sleep(5000);
        }
    }

    private static void createSpan(OpenTelemetry openTelemetry, Protocol protocol) {
        Span span = openTelemetry.getTracer("SomeTracer").spanBuilder("Some Span over " + protocol.name)
                .setAttribute("mytime", DATE_FORMAT.format(new Date()))
                .startSpan();
        span.end();
        System.out.println("Span sent over "+protocol.name);
    }

    private static OpenTelemetry setUpOpenTelemetry(Protocol protocol) {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(
                        SdkTracerProvider.builder()
                                .addSpanProcessor(SimpleSpanProcessor.create(getSpanExporter(protocol)))
                                .addResource(Resource.create(Attributes.builder().put("service.name", "dummy service").build()))
                                .build())
                .build();
    }

    private static SpanExporter getSpanExporter(Protocol protocol) {
        switch (protocol) {
            case HTTP:
                return getHTTPSpanExporter();
            case GRPC:
                return getGRPCSpanExporter();
            default:
                throw new IllegalArgumentException();
        }
    }

    private static SpanExporter getGRPCSpanExporter() {
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint("http://127.0.0.1:8200").build();
    }

    private static SpanExporter getHTTPSpanExporter() {
        return OtlpHttpSpanExporter.builder()
                .setEndpoint("http://127.0.0.1:8200").build();
    }

    enum Protocol {
        HTTP("HTTP"), GRPC("GRPC");
        public final String name;

        Protocol(String name) {
            this.name = name;
        }
    }
}