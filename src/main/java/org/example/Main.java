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
import java.util.Locale;

public class Main {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");

    public static void main(String[] args) throws InterruptedException {
        Protocol protocol = findProtocol(args);
        String serviceName = "dummy service";

        System.out.println(String.format("Setting up service: \"%s\" to export via \"%s\"", serviceName, protocol.name));

        OpenTelemetry openTelemetry = setUpOpenTelemetry(serviceName, protocol);

        while (true) {
            createSpan(openTelemetry, protocol);
            Thread.sleep(5000);
        }
    }

    private static Protocol findProtocol(String[] args) {
        if(args == null || args.length == 0) {
            System.out.println("No protocol passed, defaulting to GRPC.");
            return Protocol.GRPC;
        }
        String arg = args[0].toUpperCase(Locale.US);
        return Protocol.valueOf(arg);
    }

    private static void createSpan(OpenTelemetry openTelemetry, Protocol protocol) {
        String spanName = "Some Span over " + protocol.name;
        Span span = openTelemetry.getTracer("SomeTracer").spanBuilder(spanName)
                .setAttribute("mytime", DATE_FORMAT.format(new Date()))
                .startSpan();
        span.end();
        System.out.println("Span sent named: \"" + spanName + "\"");
    }

    private static OpenTelemetry setUpOpenTelemetry(String serviceName, Protocol protocol) {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(
                        SdkTracerProvider.builder()
                                .addSpanProcessor(SimpleSpanProcessor.create(getSpanExporter(protocol)))
                                .addResource(Resource.create(Attributes.builder().put("service.name", serviceName).build()))
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