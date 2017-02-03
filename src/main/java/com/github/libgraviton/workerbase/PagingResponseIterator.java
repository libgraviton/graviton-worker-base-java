/**
 * iterator implementation that makes using Graviton pagination a breeze
 */
package com.github.libgraviton.workerbase;

import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.gdk.api.Response;
import com.github.libgraviton.gdk.api.header.HeaderBag;
import com.google.common.collect.AbstractIterator;

import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <pre>
 * This class allows to GET resources from Graviton, abstracting pagination.
 * You can just iterate over the set and the Iterator will take care to fetch
 * the next items if available.
 *
 * Important: You *really*should* pass a custom limit(n) RQL
 * expression with your desired pagesize. If you don't, the default Graviton
 * pagesize gets used that is potentially different from what you want.
 *
 * This class has two different constructor implementations:
 * * One where you only pass the request URL and the return will be a
 * {@literal List<DeferredMap>}. (generic JSON type by Jackson Jr.)
 * * One with URL and a Class type where the return will be {@literal List<YourType>}. This
 * allows
 * you to serialize to the POJO of your choosing.
 *
 * Example usage (using conversion to DeferredMap), pagesize = 2
 * </pre>
 *
 * <pre>
 * {@code
 * PagingResponseIterator<DeferredMap> pr = new PagingResponseIterator<>(
 *   "http://localhost:8000/person/customer/?limit(2)"
 * );
 *
 * DeferredMap singleElement;
 * while (pr.hasNext()) {
 *   singleElement = pr.next();
 *   System.out.println("customer id = " + singleElement.get("id"));
 * }
 * }
 * </pre>
 *
 * Example usage (using conversion to POJO GravitonFile), pagesize = 1:
 *
 * <pre>
 * {@code
 * PagingResponseIterator<GravitonFile> pr2 = new PagingResponseIterator<>(
 *   GravitonFile.class,
 *   "http://localhost:8000/file/?limit(1)"
 * );
 *
 * GravitonFile thefile;
 * while (pr2.hasNext()) {
 *   thefile = pr2.next();
 *   System.out.println("file id = " + thefile.getId());
 * }
 * }
 * </pre>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class PagingResponseIterator<T> extends AbstractIterator<T> {

    /**
     * internal iterator
     */
    private Iterator<T> resultSet;
    
    /**
     * last response object
     */
    private Response response;
    
    /**
     * the next page to fetch (if available)
     */
    private String nextUrl = null;

    /**
     * class type to serialize to (if available)
     */
    private Class<T> typeParameterClass;

    /**
     * wrapper to communicate with the graviton API
     */
    private GravitonApi gravitonApi;

    /**
     * Constructor to serialize to DeferredMap instances
     *
     * @param requestUrl url to request
     * @param gravitonApi GravitonApi instance
     * @throws java.lang.Exception if any.
     */
    public PagingResponseIterator(String requestUrl, GravitonApi gravitonApi) throws Exception {
        this.gravitonApi = gravitonApi;
        init(requestUrl);
    }

    /**
     * Constructor to serialize to the object of your choosing
     *
     * @param typeParameterClass class type to serialize to
     * @param requestUrl url to fetch
     * @param gravitonApi GravitonApi instance
     * @throws java.lang.Exception if any.
     */
    public PagingResponseIterator(Class<T> typeParameterClass, String requestUrl, GravitonApi gravitonApi) throws Exception {
        this.typeParameterClass = typeParameterClass;
        this.gravitonApi = gravitonApi;
        init(requestUrl);
    }

    /**
     * initializes the Iterator by fetching the first page
     * 
     * @param requestUrl url to fetch
     * @throws Exception
     */
    private void init(String requestUrl) throws Exception {
        fetch(requestUrl);
        parseLinkHeader();
    }

    /**
     * {@inheritDoc}
     *
     * iterator implementation
     * @see com.google.common.collect.AbstractIterator#computeNext()
     */
    @Override
    protected T computeNext() {

        // can we fetch the next?
        if (!resultSet.hasNext() && nextUrl != null) {
            try {
                fetch(nextUrl);

                // reset next url
                nextUrl = null;

                // check for next url
                parseLinkHeader();
            } catch (Exception e) {
                return endOfData();
            }
        }

        if (resultSet.hasNext()) {
            return resultSet.next();
        }

        return endOfData();
    }

    /**
     * Fetches a page, serializes it and puts it there for the Iterator
     * 
     * @param fetchUrl url to fetch
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void fetch(String fetchUrl) throws Exception {

        response = gravitonApi.get(fetchUrl).execute();

        if (typeParameterClass != null) {
            resultSet = response.getBodyItems(typeParameterClass).iterator();
        } else {
            resultSet = (Iterator<T>) response.getBodyItem(List.class).iterator();
        }
    }

    /**
     * Parses the Link header of the response and sets 'nextUrl' accordingly
     * @throws Exception
     */
    private void parseLinkHeader() throws Exception {
        Pattern pattern = Pattern.compile("\\<(.*)\\>; rel\\=\\\"next\\\"");
        HeaderBag headers = response.getHeaders();

        if (headers.get("link") != null) {
            String linkHeader = headers.get("link").get(0);
            String[] headerParts = linkHeader.split(",");
            Matcher matcher;
            for (String singleLinkHeader : headerParts) {
                matcher = pattern.matcher(singleLinkHeader);
                if (matcher.matches()) {
                    nextUrl = URLDecoder.decode(matcher.group(1), "UTF-8");
                }
            }
        }
    }
}
