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
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces HTTP 502 Bad Gateway responses.
 * Simulates the condition where a downstream service or backend returns an invalid response.
 *
 * {@code GET /bin/wknd/error-502-simulator}
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/wknd/error-502-simulator",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class BadGatewaySimulatorServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(BadGatewaySimulatorServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        LOG.warn("502 Bad Gateway simulator triggered from {}", request.getRemoteAddr());

        response.sendError(HttpServletResponse.SC_BAD_GATEWAY,
                "Simulated bad gateway — downstream service returned an invalid response");
    }
}
