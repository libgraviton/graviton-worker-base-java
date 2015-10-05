package javaworker;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.jr.ob.impl.DeferredMap;
import com.github.libgraviton.workerbase.PagingResponseIterator;
import com.github.libgraviton.workerbase.model.GravitonFile;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({com.rabbitmq.client.ConnectionFactory.class,Unirest.class})
public class PaginationResponseIteratorTest extends WorkerBaseTestCase {

    HttpResponse<String> fileStatusResponse;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {    
        this.baseMock();
        
        URL fileResponseUrl = this.getClass().getClassLoader().getResource("json/fileResourceCollection.json");
        String fileResponseContent = FileUtils.readFileToString(new File(fileResponseUrl.getFile()));
        GetRequest getRequestStatus = mock(GetRequest.class);        
        
        List<String> headerList = new ArrayList<String>();
        headerList.add("<http://localhost/file/?limit(1%2C1)>; rel=\"next\", " +
                "<http://localhost/file/?limit(1%2C1)>; rel=\"last\"," +
                "<http://localhost/file/?limit(1)>; rel=\"self\"");
        Headers headers = new Headers();
        headers.put("link", headerList);
        
        List<String> headerListNoMore = new ArrayList<String>();
        headerListNoMore.add("<http://localhost/file/?limit(2%2C1)>; rel=\"last\"," +
                "<http://localhost/file/?limit(1)>; rel=\"self\"");
        Headers headersNoMore = new Headers();
        headersNoMore.put("link", headerListNoMore);
        
        fileStatusResponse = (HttpResponse<String>) mock(HttpResponse.class);
        when(fileStatusResponse.getHeaders())
            .thenReturn(headers)
            .thenReturn(headersNoMore)
            .thenReturn(new Headers());
        when(fileStatusResponse.getBody())
            .thenReturn(fileResponseContent);        
        when(getRequestStatus.header(anyString(), anyString()))
            .thenReturn(getRequestStatus);
        when(getRequestStatus.asString())
            .thenReturn(fileStatusResponse);        
        when(Unirest.get(contains("/file/")))
            .thenReturn(getRequestStatus);        
    }

    @Test
    public void testDeferredMapHandling() throws Exception {
        PagingResponseIterator<DeferredMap> pr = new PagingResponseIterator<>("http://localhost/file/?limit(1)");
        
        DeferredMap singleElement;
        
        // test all loop sequence
        assertTrue(pr.hasNext());

        int counter = 0;
        while (pr.hasNext()) {
            singleElement = pr.next();
            assertTrue((singleElement instanceof DeferredMap));
            counter++;
        }
        
        verify(fileStatusResponse, times(2)).getHeaders();
        
        assertEquals(4, counter);
    }
    
    @Test
    public void testPojoHandling() throws Exception {
        PagingResponseIterator<GravitonFile> pr = new PagingResponseIterator<>(GravitonFile.class, "http://localhost/file/?limit(1)");
        
        GravitonFile singleElement;
        
        // test all loop sequence
        assertTrue(pr.hasNext());

        int counter = 0;
        while (pr.hasNext()) {
            singleElement = pr.next();
            assertTrue((singleElement instanceof GravitonFile));
            assertFalse((singleElement.getId() == null));
            counter++;
        }
        
        verify(fileStatusResponse, times(2)).getHeaders();
        
        assertEquals(4, counter);
    }    
 

}
