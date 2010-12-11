package org.qrone.r7.appengine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.qrone.r7.fetcher.HTTPFetcher;
import org.qrone.r7.fetcher.HTTPResponse;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class AppEngineHTTPFetcher extends HTTPFetcher{
	private static final Logger log = Logger.getLogger(AppEngineHTTPFetcher.class.getName());
	private URLFetchService service = URLFetchServiceFactory.getURLFetchService();

	@Override
	public HTTPResponse request(org.qrone.r7.fetcher.HTTPRequest request)
			throws IOException {
		
		log.config(request.getMethod() + " " + request.getURL().toString());
		
		HTTPMethod method = HTTPMethod.valueOf(request.getMethod());
		FetchOptions options;

		if(request.isFollowRedirect()){
			options = FetchOptions.Builder.disallowTruncate().followRedirects();
		}else{
			options = FetchOptions.Builder.disallowTruncate();
		}
		
		HTTPRequest r = new HTTPRequest(request.getURL(), method, options);
		if(request.getHeaders() != null){
			for (Iterator<Entry<String, String>> iter = request.getHeaders().entrySet().iterator(); iter
					.hasNext();) {
				Entry<String, String> e = iter.next();
				HTTPHeader header = new HTTPHeader(e.getKey(), e.getValue());
				r.setHeader(header);
			}
		}
		com.google.appengine.api.urlfetch.HTTPResponse res = service.fetch(r);
		Map<String, String> hs = new HashMap<String, String>();
		for (Iterator<HTTPHeader> iter = res.getHeaders().iterator(); iter
				.hasNext();) {
			HTTPHeader t = iter.next();
			hs.put(t.getName(), t.getValue());
		}
		
		return new HTTPResponse(
				new ByteArrayInputStream(res.getContent()), 
				hs, res.getResponseCode());
	}
}
