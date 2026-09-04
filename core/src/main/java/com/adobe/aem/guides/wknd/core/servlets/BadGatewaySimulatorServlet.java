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
import java.io.PrintWriter;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces an HTTP 502-like condition by writing a partial response then crashing.
 * The dispatcher receives an incomplete response from the AEM backend, resulting in a 502 Bad Gateway.
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

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(200);

        PrintWriter writer = response.getWriter();
        writer.write("{\"status\":\"partial response started\",\"data\":[");
        writer.flush();
        response.flushBuffer();

        throw new RuntimeException("Simulated backend crash after partial response — triggers 502 at dispatcher layer");
    }
}
