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
package com.adobe.aem.guides.wknd.core.services.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Sample scheduled task that creates a Sling job every minute. {@link SampleJobConsumer} processes the job. */
@Component(service = Runnable.class, immediate = true)
@ServiceDescription("WKND Sample Minute Job")
@Designate(ocd = SampleMinuteJob.Config.class)
public class SampleMinuteJob implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(SampleMinuteJob.class);

    /** Topic under which the Sling job is created; must match {@link SampleJobConsumer}'s registered topic. */
    public static final String JOB_TOPIC = "wknd/sample/job";

    @ObjectClassDefinition(name = "WKND Sample Minute Job",
            description = "Sample task that creates a Sling job every minute.")
    public @interface Config {

        @AttributeDefinition(name = "Cron Expression",
                description = "Runs every minute by default.")
        String scheduler_expression() default "0 * * * * ?";

        @AttributeDefinition(name = "Concurrent Task",
                description = "Whether or not to schedule this task concurrently.")
        boolean scheduler_concurrent() default false;
    }

    @Reference
    private JobManager jobManager;

    @Activate
    @Modified
    protected void activate(Config config) {
        LOG.debug("Activated SampleMinuteJob with cron expression: {}", config.scheduler_expression());
    }

    @Override
    public void run() {
        Map<String, Object> jobProperties = new HashMap<>();
        jobProperties.put("createdAt", Instant.now().toString());

        Job job = jobManager.addJob(JOB_TOPIC, jobProperties);
        if (job == null) {
            LOG.warn("Failed to create Sling job with topic '{}'", JOB_TOPIC);
        } else {
            LOG.debug("Created Sling job '{}' with topic '{}'", job.getId(), JOB_TOPIC);
        }
    }
}
