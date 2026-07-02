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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.aem.guides.wknd.core.services.ContentTitleService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ContentTitleServiceImplTest {

    private final AemContext ctx = new AemContext();

    private ContentTitleService contentTitleService;

    @BeforeEach
    void setUp() {
        ctx.load().json("/com/adobe/aem/guides/wknd/core/services/impl/ContentTitleServiceImplTest.json", "/content");
        contentTitleService = ctx.registerInjectActivateService(new ContentTitleServiceImpl());
    }

    @Test
    void testGetTitle() {
        assertEquals("WKND Site", contentTitleService.getTitle("/content/wknd/jcr:content"));
    }

    @Test
    void testGetTitle_MissingProperty() {
        assertNull(contentTitleService.getTitle("/content/no-title/jcr:content"));
    }

    @Test
    void testGetTitle_MissingResource() {
        assertNull(contentTitleService.getTitle("/content/does-not-exist"));
    }

    @Test
    void testGetTitle_RejectsPathOutsideContent() {
        assertThrows(IllegalArgumentException.class, () -> contentTitleService.getTitle("/etc/foo"));
    }
}
