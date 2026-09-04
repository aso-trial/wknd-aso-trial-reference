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
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Produces HTTP 500 responses by throwing unhandled exceptions.
 * Used for testing dispatcher error handling and incident triage tooling.
 *
 * <ul>
 *   <li>{@code GET /bin/wknd/error-simulator?type=npe} — NullPointerException</li>
 *   <li>{@code GET /bin/wknd/error-simulator?type=runtime} — RuntimeException</li>
 *   <li>{@code GET /bin/wknd/error-simulator?type=repo} — ServletException wrapping a repository failure</li>
 *   <li>{@code GET /bin/wknd/error-simulator?type=stackoverflow} — StackOverflowError</li>
 *   <li>{@code GET /bin/wknd/error-simulator} — generic RuntimeException (default)</li>
 * </ul>
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/wknd/error-simulator",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class ErrorSimulatorServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ErrorSimulatorServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        String type = request.getParameter("type");
        if (type == null) {
            type = "runtime";
        }

        LOG.warn("Error simulator triggered with type='{}' from {}", type, request.getRemoteAddr());

        switch (type) {
            case "npe":
                String nullRef = null;
                nullRef.length();
                break;
            case "repo":
                throw new ServletException("Simulated repository failure",
                        new javax.jcr.RepositoryException("Cannot access session"));
            case "stackoverflow":
                triggerStackOverflow(0);
                break;
            case "runtime":
            default:
                throw new RuntimeException("Simulated internal server error");
        }
    }

    private void triggerStackOverflow(int depth) {
        triggerStackOverflow(depth + 1);
    }
}
