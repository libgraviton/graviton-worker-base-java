package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.gdk.api.Response;
import com.github.libgraviton.workerbase.gdk.api.header.HeaderBag;
import com.github.libgraviton.workerbase.gdk.data.GravitonBase;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class})
public class PaginationResponseIteratorTest extends WorkerBaseTestCase {

    Response response;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {    
        baseMock();

        String fileResponseContent = FileUtils.readFileToString(
                new File("src/test/resources/json/fileResourceCollection.json"), Charset.forName("UTF-8"));

        HeaderBag headers = new HeaderBag.Builder()
                .set("link", "<http://localhost/file/?limit(1%2C1)>; rel=\"next\", " +
                "<http://localhost/file/?limit(1%2C1)>; rel=\"last\"," +
                "<http://localhost/file/?limit(1)>; rel=\"self\"")
                .build();

        HeaderBag headersNoMore = new HeaderBag.Builder()
                .set("link", "<http://localhost/file/?limit(2%2C1)>; rel=\"last\"," +
                        "<http://localhost/file/?limit(1)>; rel=\"self\"")
                .build();

        response = spy(Response.class);
        response.setObjectMapper(objectMapper);
        doReturn(fileResponseContent).when(response).getBody();
        doCallRealMethod().when(response).getBodyItem(eq(GravitonBase.class));
        doReturn(headers)
                .doReturn(headersNoMore)
                .doReturn(new HeaderBag.Builder().build())
                .when(response).getHeaders();

        when(gravitonApi.get(contains("/file/")).execute())
                .thenReturn(response);
    }

    @Test
    public void testMapHandling() throws Exception {
        PagingResponseIterator<Map> pr = new PagingResponseIterator<>("http://localhost/file/?limit(1)", gravitonApi);

        Map singleElement;
        
        // test all loop sequence
        assertTrue(pr.hasNext());

        int counter = 0;
        while (pr.hasNext()) {
            singleElement = pr.next();
            assertTrue((singleElement != null));
            counter++;
        }
        
        verify(response, times(2)).getHeaders();
        
        assertEquals(4, counter);
    }
    
    @Test
    public void testPojoHandling() throws Exception {
        PagingResponseIterator<com.github.libgraviton.gdk.gravitondyn.file.document.File> pr = new PagingResponseIterator<>(
                com.github.libgraviton.gdk.gravitondyn.file.document.File.class,
                "http://localhost/file/?limit(1)", gravitonApi);
        
        com.github.libgraviton.gdk.gravitondyn.file.document.File singleElement;
        
        // test all loop sequence
        assertTrue(pr.hasNext());

        int counter = 0;
        while (pr.hasNext()) {
            singleElement = pr.next();
            assertTrue((singleElement != null));
            assertFalse((singleElement.getId() == null));
            counter++;
        }
        
        verify(response, times(2)).getHeaders();
        
        assertEquals(4, counter);
    }    
 

}
