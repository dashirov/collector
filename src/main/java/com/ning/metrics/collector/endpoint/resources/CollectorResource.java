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

package com.ning.metrics.collector.endpoint.resources;

import com.google.inject.Inject;
import com.ning.metrics.collector.binder.annotations.ExternalEventRequestHandler;
import com.ning.metrics.collector.endpoint.EventStats;
import com.ning.metrics.collector.endpoint.extractors.ParsedRequest;
import com.ning.metrics.serialization.event.Granularity;
import org.joda.time.DateTime;

import javax.servlet.ServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Version 1 of the collector's external API.
 */
@Path("/1")
public class CollectorResource
{
    private final EventRequestHandler requestHandler;

    @Inject
    public CollectorResource(@ExternalEventRequestHandler final EventRequestHandler requestHandler)
    {
        this.requestHandler = requestHandler;
    }

    @GET
    public Response get(
        @QueryParam("v") final String event,
        @QueryParam("date") final String eventDateTimeString,
        @QueryParam(Granularity.GRANULARITY_QUERY_PARAM) final String eventGranularity,
        @Context final HttpHeaders httpHeaders,
        @Context final ServletRequest request
    )
    {
        final EventStats eventStats = new EventStats();
        final DateTime eventDateTime = new DateTime(eventDateTimeString);
        return requestHandler.handleEventRequest(event, new ParsedRequest(httpHeaders, eventDateTime, eventGranularity, request.getRemoteAddr()), eventStats);
    }
}
