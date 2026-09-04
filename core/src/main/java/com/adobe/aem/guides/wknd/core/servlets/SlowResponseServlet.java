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

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces HTTP 504 Gateway Timeout by sleeping longer than the dispatcher render timeout (10s).
 * The dispatcher times out waiting for this servlet's response and returns 504 to the client.
 *
 * <ul>
 *   <li>{@code GET /bin/wknd/slow-response} — sleeps 15s (default, exceeds 10s render timeout)</li>
 *   <li>{@code GET /bin/wknd/slow-response?delay=30} — sleeps 30s</li>
 * </ul>
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/wknd/slow-response",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class SlowResponseServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SlowResponseServlet.class);
    private static final int DEFAULT_DELAY_SECONDS = 15;
    private static final int MAX_DELAY_SECONDS = 120;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        int delaySeconds = DEFAULT_DELAY_SECONDS;
        String delayParam = request.getParameter("delay");
        if (delayParam != null) {
            try {
                delaySeconds = Math.min(Integer.parseInt(delayParam), MAX_DELAY_SECONDS);
            } catch (NumberFormatException ignored) {
                // use default
            }
        }

        LOG.warn("Slow response servlet: sleeping {}s (render timeout is 10s) — request from {}",
                delaySeconds, request.getRemoteAddr());

        try {
            Thread.sleep(delaySeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Slow response servlet interrupted after partial sleep");
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":\"completed\",\"delaySeconds\":" + delaySeconds + "}");
    }
}
