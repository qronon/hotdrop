package org.qrone.r7.appengine;

import javax.servlet.ServletContext;

import org.qrone.kvs.KeyValueStoreService;
import org.qrone.login.CookieHandler;
import org.qrone.png.PNGMemoryImageService;
import org.qrone.r7.Extendable;
import org.qrone.r7.ExtensionIndex;
import org.qrone.r7.PortingService;
import org.qrone.r7.PortingServiceBase;
import org.qrone.r7.fetcher.HTTPFetcher;
import org.qrone.r7.github.GitHubResolver;
import org.qrone.r7.handler.ExtendableURIHandler;
import org.qrone.r7.handler.DefaultHandler;
import org.qrone.r7.handler.PathFinderHandler;
import org.qrone.r7.handler.ResolverHandler;
import org.qrone.r7.resolver.FilteredResolver;
import org.qrone.r7.resolver.InternalResourceResolver;
import org.qrone.r7.resolver.SHAResolver;
import org.qrone.r7.resolver.URIResolver;
import org.qrone.r7.script.ext.ClassPrototype;
import org.qrone.r7.script.ext.ScriptableList;
import org.qrone.r7.script.ext.ScriptableMap;
import org.qrone.r7.tag.ImageHandler;
import org.qrone.r7.tag.Scale9Handler;
import org.qrone.r7.tag.SecurityTicketHandler;
 
public class AppEngineURIHandler extends ExtendableURIHandler{
	private KeyValueStoreService kvs = new AppEngineKVSService();
	private CookieHandler cookie = new CookieHandler(kvs);
	private HTTPFetcher fetcher = new AppEngineHTTPFetcher();
	private SHAResolver cache  = new AppEngineResolver();
	private GitHubResolver github = new GitHubResolver(fetcher, cache, 
			"qronon","qrone-admintool","master");
	private AppEngineRepositoryService repository = new AppEngineRepositoryService(fetcher, cache);
	
	public AppEngineURIHandler(ServletContext cx) {
		
		resolver.add(github);
		resolver.add(repository.getResolver());
		resolver.add(new FilteredResolver("/system/resource/", new InternalResourceResolver(cx)));
		resolver.add(cache);
		PortingService services = new PortingServiceBase(
				fetcher, 
				resolver, 
				new AppEngineDatastoreService(), 
				new AppEngineMemcachedService(), 
				new AppEngineLoginService(), 
				new PNGMemoryImageService(),
				repository,
				cookie
			);
		
		DefaultHandler defaulthandler = new DefaultHandler(services);
		
		rawextend(defaulthandler);
		rawextend(this);
		
		handler.add(cookie);
		handler.add(github);
		handler.add(repository);
		handler.add(defaulthandler);
	}
	
	private void rawextend(Extendable e){
		e.addExtension(ClassPrototype.class);
		e.addExtension(ScriptableMap.class);
		e.addExtension(ScriptableList.class);
		e.addExtension(ClassPrototype.class);
		e.addExtension(ImageHandler.class);
		e.addExtension(Scale9Handler.class);
		e.addExtension(SecurityTicketHandler.class);
	}
	
}
