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

import java.util.List;

import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.aem.guides.wknd.core.services.ContentAuthorSearchService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentAuthorSearchServiceImplTest {

    @Mock
    private ResourceResolverFactory resourceResolverFactory;

    @Mock
    private ResourceResolver resourceResolver;

    @Mock
    private Session session;

    @Mock
    private Workspace workspace;

    @Mock
    private QueryManager queryManager;

    @Mock
    private Query query;

    @Mock
    private QueryResult queryResult;

    @Mock
    private ValueFactory valueFactory;

    private ContentAuthorSearchService contentAuthorSearchService;

    @BeforeEach
    void setUp() throws Exception {
        contentAuthorSearchService = new ContentAuthorSearchServiceImpl();
        setField("resourceResolverFactory", resourceResolverFactory);

        lenient().when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenReturn(resourceResolver);
        lenient().when(resourceResolver.adaptTo(Session.class)).thenReturn(session);
        lenient().when(session.getWorkspace()).thenReturn(workspace);
        lenient().when(workspace.getQueryManager()).thenReturn(queryManager);
        lenient().when(session.getValueFactory()).thenReturn(valueFactory);
        lenient().when(queryManager.createQuery(any(String.class), any(String.class))).thenReturn(query);
        lenient().when(query.execute()).thenReturn(queryResult);
    }

    private void setField(String name, Object value) throws Exception {
        java.lang.reflect.Field field = ContentAuthorSearchServiceImpl.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(contentAuthorSearchService, value);
    }

    @Test
    void testFindPagesByLastModifiedBy() throws Exception {
        Row row = mockRow("/content/wknd/jcr:content");
        when(queryResult.getRows()).thenReturn(toRowIterator(row));

        List<String> paths = contentAuthorSearchService.findPagesByLastModifiedBy("admin");

        assertEquals(List.of("/content/wknd/jcr:content"), paths);
        org.mockito.Mockito.verify(valueFactory).createValue("admin");
    }

    @Test
    void testFindPagesByLastModifiedBy_NoMatch() throws Exception {
        when(queryResult.getRows()).thenReturn(toRowIterator());

        assertTrue(contentAuthorSearchService.findPagesByLastModifiedBy("nobody").isEmpty());
    }

    @Test
    void testFindPagesByLastModifiedBy_LoginException() throws Exception {
        when(resourceResolverFactory.getServiceResourceResolver(anyMap())).thenThrow(new LoginException());

        assertTrue(contentAuthorSearchService.findPagesByLastModifiedBy("admin").isEmpty());
    }

    private Row mockRow(String path) throws Exception {
        Row row = org.mockito.Mockito.mock(Row.class);
        when(row.getPath()).thenReturn(path);
        return row;
    }

    private RowIterator toRowIterator(Row... rows) {
        return new RowIterator() {
            private int index = 0;

            @Override
            public Row nextRow() {
                return rows[index++];
            }

            @Override
            public void skip(long skipNum) {
                index += (int) skipNum;
            }

            @Override
            public long getSize() {
                return rows.length;
            }

            @Override
            public long getPosition() {
                return index;
            }

            @Override
            public boolean hasNext() {
                return index < rows.length;
            }

            @Override
            public Object next() {
                return nextRow();
            }
        };
    }
}
