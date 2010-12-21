package org.qrone.r7.appengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ho.yaml.Yaml;
import org.mozilla.javascript.Scriptable;
import org.qrone.database.DatabaseService;
import org.qrone.r7.RepositoryService;
import org.qrone.r7.fetcher.HTTPFetcher;
import org.qrone.r7.github.GitHubResolver;
import org.qrone.r7.handler.URIHandler;
import org.qrone.r7.resolver.CascadeResolver;
import org.qrone.r7.resolver.SHAResolver;
import org.qrone.r7.resolver.URIResolver;
import org.qrone.r7.script.Scriptables;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class AppEngineRepositoryService implements URIHandler, RepositoryService{
	private Map<String, GitHubResolver> idToResolverMap = new Hashtable<String, GitHubResolver>();
	private DatastoreService service = DatastoreServiceFactory.getDatastoreService();
	private CascadeResolver cascade = new CascadeResolver();
	
	private static final String KIND = "qrone.repository";
	private static final String ID = "id";
	private static final String OWNER = "owner";
	private static final String NAME = "name";
	private static final String TREE_SHA = "tree_sha";
	
	private HTTPFetcher fetcher;
	private SHAResolver cacher;
	
	public AppEngineRepositoryService(HTTPFetcher fetcher, SHAResolver cacher){
		this.fetcher = fetcher;
		this.cacher = cacher;
		
		Query query = new Query(KIND);
		PreparedQuery pq = service.prepare(query);
		for( Entity e : pq.asIterable()){
			addGithub(e);
		}
	}
	
	private GitHubResolver addGithub(Entity e){
		GitHubResolver github = new GitHubResolver(fetcher, cacher, 
				(String)e.getProperty(OWNER), (String)e.getProperty(NAME), (String)e.getProperty(TREE_SHA));
		if(github.exist()){
			cascade.add(github);
			return github;
		}
		return null;
	}
	
	public URIResolver getResolver(){
		return cascade;
	}
	
	@Override
	public String add(Scriptable s) {
		Map repo = Scriptables.asMap(s);
		Entity e = new Entity(KIND);
		e.setProperty(OWNER, repo.get(OWNER).toString());
		e.setProperty(NAME, repo.get(NAME).toString());
		e.setProperty(TREE_SHA, repo.get(TREE_SHA).toString());
		
		GitHubResolver resolver = addGithub(e);
		if(resolver != null){
			service.put(e);
			String id = KeyFactory.keyToString(e.getKey());
			idToResolverMap.put(id, resolver);
			return id;
		}
		
		return null;
	}

	@Override
	public void remove(String id) {
		service.delete(KeyFactory.stringToKey(id));
		GitHubResolver resolver = idToResolverMap.get(id);
		if(resolver != null){
			idToResolverMap.remove(id);
			cascade.asList().remove(resolver);
		}
	}

	@Override
	public List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();

		Query query = new Query(KIND);
		PreparedQuery pq = service.prepare(query);
		for( Entity e : pq.asIterable()){
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(ID, KeyFactory.keyToString(e.getKey()));
			map.putAll(e.getProperties());
			list.add(map);
		}
		
		return list;
	}

	@Override
	public boolean handle(HttpServletRequest request,
			HttpServletResponse response, String uri, String requestPath,
			String requestPathArg) {
		List l = cascade.asList();
		for (Object o : l) {
			((URIHandler)o).handle(request, response, uri, requestPath, requestPathArg);
		}
		return false;
	}

	@Override
	public void reset(String id) {
		GitHubResolver resolver = idToResolverMap.get(id);
		if(resolver != null){
			resolver.reset();
		}
	}
	

}
