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
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aem.guides.wknd.core.services.ContentAuthorSearchService;

/**
 * Exposes {@link ContentAuthorSearchService} over HTTP for querying content by last-modified-by user, e.g.
 * {@code GET /bin/wknd/pages-by-author?author=admin}.
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/wknd/pages-by-author",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class PagesByAuthorServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    private static final String PARAM_AUTHOR = "author";

    @Reference
    private transient ContentAuthorSearchService contentAuthorSearchService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        String author = request.getParameter(PARAM_AUTHOR);
        if (author == null || author.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter: " + PARAM_AUTHOR);
            return;
        }

        List<String> paths = contentAuthorSearchService.findPagesByLastModifiedBy(author);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(toJsonArray(paths));
    }

    private static String toJsonArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(',');
            }
            json.append('"').append(values.get(i).replace("\"", "\\\"")).append('"');
        }
        return json.append(']').toString();
    }
}
