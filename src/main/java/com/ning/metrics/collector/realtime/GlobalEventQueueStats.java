/*
 * Copyright 2010-2011 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.metrics.collector.realtime;

import com.ning.arecibo.jmx.Monitored;
import com.ning.arecibo.jmx.MonitoringType;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GlobalEventQueueStats
{
    private final Map<String, EventQueueStats> stats = new ConcurrentHashMap<String, EventQueueStats>();
    private final AtomicLong ignoredEvents = new AtomicLong(0);

    public EventQueueStats createLocalStats(final String eventType, final Collection<Object> queue, final long maxQueueLength)
    {
        final EventQueueStats localStats = new EventQueueStats(queue, maxQueueLength);
        // We are guaranteed to have at most one stats object per event type (see EventQueueProcessorImpl)
        stats.put(eventType, localStats);
        return localStats;
    }

    /**
     * Register an event that was ignored by the RealTime subsystem (event type not configured)
     */
    public void registerEventIgnored()
    {
        ignoredEvents.incrementAndGet();
    }

    @Monitored(description = "Number of ignored events (type not enabled)", monitoringType = {MonitoringType.VALUE, MonitoringType.RATE})
    public long getIgnoredEvents()
    {
        return ignoredEvents.get();
    }

    @Monitored(description = "Number of enqueued events", monitoringType = {MonitoringType.VALUE, MonitoringType.RATE})
    public long getEnqueuedEvents()
    {
        long enqueuedEvents = 0;
        for (final EventQueueStats localStats : stats.values()) {
            enqueuedEvents += localStats.getEnqueuedEvents();
        }
        return enqueuedEvents;
    }

    @Monitored(description = "Number of dropped events", monitoringType = {MonitoringType.VALUE, MonitoringType.RATE})
    public long getDroppedEvents()
    {
        long droppedEvents = 0;
        for (final EventQueueStats localStats : stats.values()) {
            droppedEvents += localStats.getDroppedEvents();
        }
        return droppedEvents;
    }

    @Monitored(description = "Number of successfully sent events", monitoringType = {MonitoringType.VALUE, MonitoringType.RATE})
    public long getSentEvents()
    {
        long sentEvents = 0;
        for (final EventQueueStats localStats : stats.values()) {
            sentEvents += localStats.getSentEvents();
        }
        return sentEvents;
    }

    @Monitored(description = "Number of events that could not be sent due to an error", monitoringType = {MonitoringType.VALUE, MonitoringType.RATE})
    public long getErroredEvents()
    {
        long erroredEvents = 0;
        for (final EventQueueStats localStats : stats.values()) {
            erroredEvents += localStats.getErroredEvents();
        }
        return erroredEvents;
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();

        int i = 1;
        final Set<String> eventTypes = stats.keySet();
        for (final String eventType : eventTypes) {
            final long queueSize = stats.get(eventType).getQueueSize();
            builder.append(String.format("%s: %d", eventType, queueSize));

            if (i < eventTypes.size()) {
                builder.append(", ");
            }
            i++;
        }

        return builder.toString();
    }

    /**
     * Unit test hook
     */
    public void clear()
    {
        ignoredEvents.set(0);
        for (final EventQueueStats localStats : stats.values()) {
            localStats.clear();
        }
    }
}
