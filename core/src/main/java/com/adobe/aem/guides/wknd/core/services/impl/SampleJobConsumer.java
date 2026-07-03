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

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Processes Sling jobs created by {@link SampleMinuteJob}. */
@Component(service = JobConsumer.class,
        property = {
                JobConsumer.PROPERTY_TOPICS + "=" + SampleMinuteJob.JOB_TOPIC
        })
@ServiceDescription("WKND Sample Job Consumer")
public class SampleJobConsumer implements JobConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(SampleJobConsumer.class);

    @Override
    public JobResult process(Job job) {
        LOG.info("Processing Sling job '{}' (topic: '{}', createdAt: {})",
                job.getId(), job.getTopic(), job.getProperty("createdAt"));
        return JobResult.OK;
    }
}
