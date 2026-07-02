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
import org.osgi.service.component.annotations.Reference;

import com.adobe.aem.guides.wknd.core.services.ContentTitleService;

/**
 * Exposes {@link ContentTitleService} over HTTP, e.g. {@code GET /bin/wknd/content-title?path=/content/wknd}.
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/wknd/content-title",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class ContentTitleServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    private static final String PARAM_PATH = "path";

    @Reference
    private transient ContentTitleService contentTitleService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String path = request.getParameter(PARAM_PATH);
        if (path == null || path.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter: " + PARAM_PATH);
            return;
        }

        String title;
        try {
            title = contentTitleService.getTitle(path);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        if (title == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "No title found for path: " + path);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"title\":\"" + title.replace("\"", "\\\"") + "\"}");
    }
}
