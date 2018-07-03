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

import io.opentracing.References;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.commons.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AnalyticsSpans are created via AnalyticsTracer.buildSpan(...), but they are also returned via calls to
 * AnalyticsTracer.finishedSpans(). They provide accessors to all Span state.
 **/
final class AnalyticsSpan implements Span {

    private static AtomicLong nextId = new AtomicLong();

    private AnalyticsSpanContext context;
    private final long parentId; // 0 if there's no parent.
    private final long startMicros;
    private boolean finished;
    private final Map<String, Object> tags;
    private String operationName;
    private final List<Reference> references;
    private DataPublisher dataPublisher;
    private String componentName;

    @Override
    public synchronized AnalyticsSpan setOperationName(String operationName) {
        finishedCheck("Setting operationName {%s} on already finished span", operationName);
        this.operationName = operationName;
        return this;
    }

    @Override
    public Span log(String s, Object o) {
        return null;
    }

    @Override
    public Span log(long l, String s, Object o) {
        return null;
    }

    @Override
    public synchronized AnalyticsSpanContext context() {
        return this.context;
    }

    @Override
    public void finish() {
        this.finish(nowMicros());
    }

    @Override
    public synchronized void finish(long finishMicros) {
        finishedCheck("Finishing already finished span");
        this.finished = true;
        this.dataPublisher.
                publish(new Event(Constants.ANALYTICS_SPAN_STREAM_ID,
                        System.currentTimeMillis(), null, null,
                        new Object[]{this.componentName, this.context.traceId, this.context.spanId,
                                Utils.getJSONString(this.context.baggage), parentId, operationName,
                                this.startMicros, finishMicros, Utils.getJSONString(tags),
                                Utils.getJSONString(references)}));
    }

    @Override
    public AnalyticsSpan setTag(String key, String value) {
        return setObjectTag(key, value);
    }

    @Override
    public AnalyticsSpan setTag(String key, boolean value) {
        return setObjectTag(key, value);
    }

    @Override
    public AnalyticsSpan setTag(String key, Number value) {
        return setObjectTag(key, value);
    }

    private synchronized AnalyticsSpan setObjectTag(String key, Object value) {
        finishedCheck("Adding tag {%s:%s} to already finished span", key, value);
        tags.put(key, value);
        return this;
    }

    @Override
    public final Span log(Map<String, ?> fields) {
        return log(nowMicros(), fields);
    }

    @Override
    public final synchronized AnalyticsSpan log(long timestampMicros, Map<String, ?> fields) {
        finishedCheck("Adding logs %s at %d to already finished span", fields, timestampMicros);
        return this;
    }

    @Override
    public AnalyticsSpan log(String event) {
        return this.log(nowMicros(), event);
    }

    @Override
    public AnalyticsSpan log(long timestampMicroseconds, String event) {
        return this.log(timestampMicroseconds, Collections.singletonMap("event", event));
    }

    @Override
    public synchronized Span setBaggageItem(String key, String value) {
        finishedCheck("Adding baggage {%s:%s} to already finished span", key, value);
        this.context = this.context.withBaggageItem(key, value);
        return this;
    }

    @Override
    public synchronized String getBaggageItem(String key) {
        return this.context.getBaggageItem(key);
    }

    /**
     * AnalyticsSpanContext implements a Dapper-like opentracing.SpanContext with a trace- and span-id.
     * <p>
     * Note that parent ids are part of the AnalyticsSpan, not the AnalyticsSpanContext (since they do not need
     * to propagate between processes).
     */
    static final class AnalyticsSpanContext implements SpanContext {
        private final String traceId;
        private final Map<String, String> baggage;
        private final long spanId;

        /**
         * A package-protected constructor to create a new AnalyticsSpanContext. This should only be called by
         * AnalyticsSpan and/or
         * AnalyticsTracer.
         *
         * @param baggage the AnalyticsSpanContext takes ownership of the baggage parameter
         * @see AnalyticsSpanContext#withBaggageItem(String, String)
         */
        AnalyticsSpanContext(String traceId, long spanId, Map<String, String> baggage) {
            this.baggage = baggage;
            this.traceId = traceId;
            this.spanId = spanId;
        }

        String getBaggageItem(String key) {
            return this.baggage.get(key);
        }

        String traceId() {
            return traceId;
        }

        long spanId() {
            return spanId;
        }

        /**
         * Create and return a new (immutable) AnalyticsSpanContext with the added baggage item.
         */
        AnalyticsSpanContext withBaggageItem(String key, String val) {
            Map<String, String> newBaggage = new HashMap<>(this.baggage);
            newBaggage.put(key, val);
            return new AnalyticsSpanContext(this.traceId, this.spanId, newBaggage);
        }

        @Override
        public Iterable<Map.Entry<String, String>> baggageItems() {
            return baggage.entrySet();
        }
    }

    public static final class Reference {
        private final AnalyticsSpanContext context;
        private final String referenceType;

        Reference(AnalyticsSpanContext context, String referenceType) {
            this.context = context;
            this.referenceType = referenceType;
        }

        AnalyticsSpanContext getContext() {
            return context;
        }

        String getReferenceType() {
            return referenceType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Reference reference = (Reference) o;
            return Objects.equals(context, reference.context) &&
                    Objects.equals(referenceType, reference.referenceType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(context, referenceType);
        }
    }

    AnalyticsSpan(String operationName, long startMicros, Map<String, Object> initialTags,
                  List<Reference> refs, DataPublisher dataPublisher, String componentName) {
        this.operationName = operationName;
        this.startMicros = startMicros;
        this.dataPublisher = dataPublisher;
        this.componentName = componentName;
        if (initialTags == null) {
            this.tags = new HashMap<>();
        } else {
            this.tags = new HashMap<>(initialTags);
        }
        if (refs == null) {
            this.references = Collections.emptyList();
        } else {
            this.references = new ArrayList<>(refs);
        }
        AnalyticsSpanContext parent = findPreferredParentRef(this.references);
        if (parent == null) {
            // We're a root Span.
            this.context = new AnalyticsSpanContext(generateTraceId(), nextId(), new HashMap<>());
            this.parentId = 0;
        } else {
            // We're a child Span.
            this.context = new AnalyticsSpanContext(parent.traceId, nextId(), mergeBaggages(this.references));
            this.parentId = parent.spanId;
        }
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }

    private static AnalyticsSpanContext findPreferredParentRef(List<Reference> references) {
        if (references.isEmpty()) {
            return null;
        }
        for (Reference reference : references) {
            if (References.CHILD_OF.equals(reference.getReferenceType())) {
                return reference.getContext();
            }
        }
        return references.get(0).getContext();
    }

    private static Map<String, String> mergeBaggages(List<Reference> references) {
        Map<String, String> baggage = new HashMap<>();
        for (Reference ref : references) {
            if (ref.getContext().baggage != null) {
                baggage.putAll(ref.getContext().baggage);
            }
        }
        return baggage;
    }

    private long nextId() {
        return nextId.incrementAndGet();
    }

    static long nowMicros() {
        return System.nanoTime();
    }

    private synchronized void finishedCheck(String format, Object... args) {
        if (finished) {
            throw new IllegalStateException(String.format(format, args));
        }
    }

    @Override
    public String toString() {
        return "{" +
                "traceId:" + context.traceId() +
                ", spanId:" + context.spanId() +
                ", parentId:" + parentId +
                ", operationName:\"" + operationName + "\"}";
    }
}
