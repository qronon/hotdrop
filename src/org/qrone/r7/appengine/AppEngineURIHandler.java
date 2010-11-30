package org.qrone.r7.appengine;

import javax.servlet.ServletContext;

import org.qrone.kvs.KeyValueStoreService;
import org.qrone.login.CookieHandler;
import org.qrone.png.PNGMemoryImageService;
import org.qrone.r7.Extendable;
import org.qrone.r7.ExtensionIndex;
import org.qrone.r7.PortingServiceBase;
import org.qrone.r7.fetcher.URLFetcher;
import org.qrone.r7.github.GitHubResolver;
import org.qrone.r7.handler.ExtendableURIHandler;
import org.qrone.r7.handler.HTML5Handler;
import org.qrone.r7.handler.PathFinderHandler;
import org.qrone.r7.handler.ResolverHandler;
import org.qrone.r7.resolver.URIResolver;
import org.qrone.r7.script.ext.ClassPrototype;
import org.qrone.r7.script.ext.ListWrapper;
import org.qrone.r7.script.ext.MapWrapper;
import org.qrone.r7.tag.ImageHandler;
import org.qrone.r7.tag.Scale9Handler;
 
public class AppEngineURIHandler extends ExtendableURIHandler{
	private KeyValueStoreService kvs = new AppEngineKVSService();
	private CookieHandler cookie = new CookieHandler(kvs);
	private URLFetcher fetcher = new AppEngineURLFetcher();
	private URIResolver cache  = new AppEngineResolver();
	private GitHubResolver github = new GitHubResolver(fetcher, cache, 
			"qronon","qrone-admintool","master");
	private AppEngineRepositoryService repository = new AppEngineRepositoryService(fetcher, cache);
	
	public AppEngineURIHandler(ServletContext cx) {
		//resolver.add(new FilteredResolver("/qrone-server/", new InternalResourceResolver()));
		//resolver.add(new ServletResolver(cx));
		
		resolver.add(github);
		resolver.add(repository.getResolver());

		HTML5Handler html5handler = new HTML5Handler(
				new PortingServiceBase(
						fetcher, 
						resolver, 
						new AppEngineDatastoreService(), 
						new AppEngineMemcachedService(), 
						new AppEngineLoginService(), 
						new PNGMemoryImageService(),
						repository,
						cookie
				));
		
		rawextend(html5handler);
		rawextend(this);
		
		handler.add(cookie);
		handler.add(github);
		handler.add(new PathFinderHandler(html5handler));
		handler.add(new ResolverHandler(resolver));
	}
	
	private void rawextend(Extendable e){
		e.addExtension(ClassPrototype.class);
		e.addExtension(ListWrapper.class);
		e.addExtension(MapWrapper.class);
		e.addExtension(ImageHandler.class);
		e.addExtension(Scale9Handler.class);
	}
	
}
