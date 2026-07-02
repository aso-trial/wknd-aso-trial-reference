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

import java.util.Collections;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.guides.wknd.core.services.ContentTitleService;

@Component(service = ContentTitleService.class)
public class ContentTitleServiceImpl implements ContentTitleService {

    private static final Logger LOG = LoggerFactory.getLogger(ContentTitleServiceImpl.class);

    /** Must match the subservice name declared in the serviceusermapping OSGi configuration. */
    static final String SUBSERVICE_NAME = "wknd-content-reader";

    private static final String JCR_TITLE = "jcr:title";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;
    
    @Activate
    public void init(){
        String foo = getTitle("/content/wknd");
        LOG.info("Activated {}", foo);
    }

    @Override
    public String getTitle(String contentPath) {
        if (contentPath == null || !contentPath.startsWith("/content")) {
            throw new IllegalArgumentException("Path must be located under /content: " + contentPath);
        }

        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(
                Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME))) {
            Resource resource = resourceResolver.getResource(contentPath);
            if (resource == null) {
                LOG.warn("No resource found at path: {}", contentPath);
                return null;
            }
            return resource.getValueMap().get(JCR_TITLE, String.class);
        } catch (LoginException e) {
            LOG.error("Unable to obtain service resource resolver for subservice '{}'", SUBSERVICE_NAME, e);
            return null;
        }
    }
}
