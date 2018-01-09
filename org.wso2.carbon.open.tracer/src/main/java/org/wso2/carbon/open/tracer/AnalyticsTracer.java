/*
 * Copyright 2016-2017 The OpenTracing Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.wso2.carbon.open.tracer;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpanSource;
import io.opentracing.BaseSpan;
import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import org.wso2.carbon.databridge.agent.DataPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsTracer is the analytics tracer implementation which uses the open tracing standard.
 */

public class AnalyticsTracer implements Tracer {
    private Propagator propagator;
    private ActiveSpanSource spanSource;
    private DataPublisher dataPublisher;
    private String componentName;

    AnalyticsTracer(DataPublisher dataPublisher, String componentName) {
        this(new ThreadLocalActiveSpanSource(), Propagator.TEXT_MAP, dataPublisher, componentName);
    }

    private AnalyticsTracer(ActiveSpanSource spanSource, Propagator propagator, DataPublisher dataPublisher,
                            String componentName) {
        this.propagator = propagator;
        this.spanSource = spanSource;
        this.dataPublisher = dataPublisher;
        this.componentName = componentName;
    }

    @Override
    public ActiveSpan activeSpan() {
        return spanSource.activeSpan();
    }

    @Override
    public ActiveSpan makeActive(Span span) {
        return spanSource.makeActive(span);
    }

    /**
     * Propagator allows the developer to intercept and verify any calls to inject() and/or extract().
     * <p>
     * By default, AnalyticsTracer uses Propagator.PRINTER which simply logs such calls to System.out.
     */
    public interface Propagator {
        <C> void inject(AnalyticsSpan.AnalyticsSpanContext ctx, Format<C> format, C carrier);

        <C> AnalyticsSpan.AnalyticsSpanContext extract(Format<C> format, C carrier, DataPublisher dataPublisher);

        Propagator TEXT_MAP = new Propagator() {
            public static final String SPAN_ID_KEY = "spanid";
            public static final String TRACE_ID_KEY = "traceid";
            public static final String BAGGAGE_KEY_PREFIX = "baggage-";

            @Override
            public <C> void inject(AnalyticsSpan.AnalyticsSpanContext ctx, Format<C> format, C carrier) {
                if (carrier instanceof TextMap) {
                    TextMap textMap = (TextMap) carrier;
                    for (Map.Entry<String, String> entry : ctx.baggageItems()) {
                        textMap.put(BAGGAGE_KEY_PREFIX + entry.getKey(), entry.getValue());
                    }
                    textMap.put(SPAN_ID_KEY, String.valueOf(ctx.spanId()));
                    textMap.put(TRACE_ID_KEY, String.valueOf(ctx.traceId()));
                } else {
                    throw new IllegalArgumentException("Unknown carrier");
                }
            }

            @Override
            public <C> AnalyticsSpan.AnalyticsSpanContext extract(Format<C> format, C carrier,
                                                                  DataPublisher dataPublisher) {
                Long traceId = null;
                Long spanId = null;
                Map<String, String> baggage = new HashMap<>();

                if (carrier instanceof TextMap) {
                    TextMap textMap = (TextMap) carrier;
                    for (Map.Entry<String, String> entry : textMap) {
                        if (TRACE_ID_KEY.equals(entry.getKey())) {
                            traceId = Long.valueOf(entry.getValue());
                        } else if (SPAN_ID_KEY.equals(entry.getKey())) {
                            spanId = Long.valueOf(entry.getValue());
                        } else if (entry.getKey().startsWith(BAGGAGE_KEY_PREFIX)) {
                            String key = entry.getKey().substring((BAGGAGE_KEY_PREFIX.length()));
                            baggage.put(key, entry.getValue());
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Unknown carrier");
                }

                if (traceId != null && spanId != null) {
                    return new AnalyticsSpan.AnalyticsSpanContext(traceId, spanId, baggage);
                }

                return null;
            }
        };
    }

    @Override
    public SpanBuilder buildSpan(String operationName) {
        return new SpanBuilder(operationName);
    }

    private SpanContext activeSpanContext() {
        ActiveSpan handle = this.spanSource.activeSpan();
        if (handle == null) {
            return null;
        }
        return handle.context();
    }

    @Override
    public <C> void inject(SpanContext spanContext, Format<C> format, C carrier) {
        this.propagator.inject((AnalyticsSpan.AnalyticsSpanContext) spanContext, format, carrier);
    }

    @Override
    public <C> SpanContext extract(Format<C> format, C carrier) {
        return this.propagator.extract(format, carrier, this.dataPublisher);
    }

    /**
     * This is the analytics extension of the {@link io.opentracing.Tracer.SpanBuilder}.
     */
    public final class SpanBuilder implements Tracer.SpanBuilder {
        private final String operationName;
        private long startMicros;
        private List<AnalyticsSpan.Reference> references = new ArrayList<>();
        private boolean ignoringActiveSpan;
        private Map<String, Object> initialTags = new HashMap<>();

        SpanBuilder(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public SpanBuilder asChildOf(SpanContext parent) {
            return addReference(References.CHILD_OF, parent);
        }

        @Override
        public SpanBuilder asChildOf(BaseSpan parent) {
            return addReference(References.CHILD_OF, parent.context());
        }

        @Override
        public SpanBuilder ignoreActiveSpan() {
            ignoringActiveSpan = true;
            return this;
        }

        @Override
        public SpanBuilder addReference(String referenceType, SpanContext referencedContext) {
            if (referencedContext != null) {
                this.references.add(new AnalyticsSpan.Reference((AnalyticsSpan.AnalyticsSpanContext) referencedContext,
                        referenceType));
            }
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, String value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, boolean value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withTag(String key, Number value) {
            this.initialTags.put(key, value);
            return this;
        }

        @Override
        public SpanBuilder withStartTimestamp(long microseconds) {
            this.startMicros = microseconds;
            return this;
        }

        @Override
        @Deprecated
        public AnalyticsSpan start() {
            return startManual();
        }

        @Override
        public ActiveSpan startActive() {
            AnalyticsSpan span = this.startManual();
            return spanSource.makeActive(span);
        }

        @Override
        public AnalyticsSpan startManual() {
            if (this.startMicros == 0) {
                this.startMicros = AnalyticsSpan.nowMicros();
            }
            SpanContext activeSpanContext = activeSpanContext();
            if (references.isEmpty() && !ignoringActiveSpan && activeSpanContext != null) {
                references.add(new AnalyticsSpan.Reference((AnalyticsSpan.AnalyticsSpanContext) activeSpanContext,
                        References.CHILD_OF));
            }
            return new AnalyticsSpan(operationName, startMicros, initialTags, references, dataPublisher, componentName);
        }
    }
}
