package org.qrone.r7.appengine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.qrone.r7.fetcher.URLFetcher;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

public class AppEngineURLFetcher implements URLFetcher{
	private URLFetchService service = URLFetchServiceFactory.getURLFetchService();
	
	@Override
	public InputStream fetch(String url) {
		try {
			byte[] bytes = service.fetch(new URL(url)).getContent();
			System.err.println(new String(bytes));
			return new ByteArrayInputStream(bytes);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
