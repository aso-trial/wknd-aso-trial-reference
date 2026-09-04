/*
 *  Copyright 2026 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.adobe.aem.guides.wknd.core.servlets;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces HTTP 503 Service Unavailable responses, optionally in an intermittent pattern.
 * Primary scenario for demonstrating dispatcher incident triage tooling.
 *
 * <ul>
 *   <li>{@code GET /bin/wknd/error-503-simulator} — always returns 503</li>
 *   <li>{@code GET /bin/wknd/error-503-simulator?intermittent=true} — returns 503 ~50% of the time, 200 otherwise</li>
 *   <li>{@code GET /bin/wknd/error-503-simulator?retryAfter=120} — sets Retry-After header (default: 30s)</li>
 * </ul>
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/wknd/error-503-simulator",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class ServiceUnavailableServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ServiceUnavailableServlet.class);
    private static final int DEFAULT_RETRY_AFTER = 30;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        boolean intermittent = "true".equalsIgnoreCase(request.getParameter("intermittent"));

        if (intermittent && ThreadLocalRandom.current().nextBoolean()) {
            LOG.info("503 simulator (intermittent mode): returning 200 this time");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":\"ok\",\"mode\":\"intermittent\",\"result\":\"healthy\"}");
            return;
        }

        int retryAfter = DEFAULT_RETRY_AFTER;
        String retryParam = request.getParameter("retryAfter");
        if (retryParam != null) {
            try {
                retryAfter = Integer.parseInt(retryParam);
            } catch (NumberFormatException ignored) {
                // use default
            }
        }

        LOG.warn("503 Service Unavailable simulator triggered (intermittent={}) from {}",
                intermittent, request.getRemoteAddr());

        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "Service temporarily unavailable — simulated overload condition");
    }
}
