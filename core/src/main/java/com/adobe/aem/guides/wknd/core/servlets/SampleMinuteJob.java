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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aem.guides.wknd.core.services.impl.SampleJobConsumer;

/**
 * Creates a Sling job on {@code GET}, e.g. {@code GET /bin/wknd/sample-job}. {@link SampleJobConsumer} processes it.
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/wknd/sample-job",
        "sling.servlet.methods=" + HttpConstants.METHOD_GET
})
public class SampleMinuteJob extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    /** Topic under which the Sling job is created; must match {@link SampleJobConsumer}'s registered topic. */
    public static final String JOB_TOPIC = "wknd/sample/job";

    @Reference
    private transient JobManager jobManager;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        Map<String, Object> jobProperties = new HashMap<>();
        jobProperties.put("createdAt", Instant.now().toString());

        Job job = jobManager.addJob(JOB_TOPIC, jobProperties);
        if (job == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to create Sling job with topic: " + JOB_TOPIC);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"jobId\":\"" + job.getId() + "\",\"topic\":\"" + JOB_TOPIC + "\"}");
    }
}
