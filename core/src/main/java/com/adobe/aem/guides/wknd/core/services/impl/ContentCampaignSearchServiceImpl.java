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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aem.guides.wknd.core.services.ContentCampaignSearchService;

@Component(service = ContentCampaignSearchService.class)
public class ContentCampaignSearchServiceImpl implements ContentCampaignSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(ContentCampaignSearchServiceImpl.class);

    /** Must match the subservice name declared in the serviceusermapping OSGi configuration. */
    static final String SUBSERVICE_NAME = "wknd-content-reader";

    private static final String QUERY = "SELECT [jcr:path] FROM [nt:unstructured] WHERE ISDESCENDANTNODE([/content])"
            + " AND [campaignId] = $campaignId";

    /** Caps the result set so a widely-used or unexpected campaign id can't return an unbounded page of results. */
    private static final long MAX_RESULTS = 500;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Override
    public List<String> findPagesByCampaignId(String campaignId) {
        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(
                Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME))) {
            Session session = resourceResolver.adaptTo(Session.class);
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            Query query = queryManager.createQuery(QUERY, Query.JCR_SQL2);
            query.bindValue("campaignId", session.getValueFactory().createValue(campaignId));
            query.setLimit(MAX_RESULTS);

            QueryResult result = query.execute();
            List<String> paths = new ArrayList<>();
            RowIterator rows = result.getRows();
            while (rows.hasNext()) {
                paths.add(rows.nextRow().getPath());
            }
            return paths;
        } catch (LoginException e) {
            LOG.error("Unable to obtain service resource resolver for subservice '{}'", SUBSERVICE_NAME, e);
            return Collections.emptyList();
        } catch (RepositoryException e) {
            LOG.error("Unable to search for content tagged with campaign id '{}'", campaignId, e);
            return Collections.emptyList();
        }
    }
}
