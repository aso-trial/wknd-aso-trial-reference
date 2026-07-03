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
package com.adobe.aem.guides.wknd.core.services;

import java.util.List;

/**
 * Looks up content tagged with a given campaign id.
 */
public interface ContentCampaignSearchService {

    /**
     * @param campaignId the value of the {@code campaignId} property to search for
     * @return the paths of matching resources, or an empty list if none are found
     */
    List<String> findPagesByCampaignId(String campaignId);
}
