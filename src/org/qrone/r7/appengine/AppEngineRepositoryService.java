package org.qrone.r7.appengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.qrone.database.DatabaseService;
import org.qrone.r7.RepositoryService;
import org.qrone.r7.fetcher.URLFetcher;
import org.qrone.r7.github.GitHubResolver;
import org.qrone.r7.resolver.CascadeResolver;
import org.qrone.r7.resolver.URIResolver;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class AppEngineRepositoryService implements RepositoryService{
	private DatastoreService service = DatastoreServiceFactory.getDatastoreService();
	private CascadeResolver cascade = new CascadeResolver();
	
	private static final String KIND = "qrone.repository";
	private static final String ID = "id";
	private static final String NAME = "name";
	private static final String REPO = "repo";
	private static final String TAG = "tag";
	
	private URLFetcher fetcher;
	private URIResolver cacher;
	
	public AppEngineRepositoryService(URLFetcher fetcher, URIResolver cacher){
		Query query = new Query(KIND);
		PreparedQuery pq = service.prepare(query);
		for( Entity e : pq.asIterable()){
			addGithub(e);
		}
	}
	
	private void addGithub(Entity e){
		GitHubResolver github = new GitHubResolver(fetcher, cacher, 
				(String)e.getProperty(NAME), (String)e.getProperty(REPO), (String)e.getProperty(TAG));
		cascade.add(github);
	}
	
	public URIResolver getResolver(){
		return cascade;
	}
	
	@Override
	public String add(String path, Map<String, String> repo) {
		Entity e = new Entity(KIND);
		for (Iterator<String> i = repo.keySet().iterator(); i
				.hasNext();) {
			String key = i.next();
			e.setProperty(key, repo.get(key));
		}
		
		String id = KeyFactory.keyToString(service.put(e));
		
		e.setProperty(ID, id);
		addGithub(e);
		return id;
	}

	@Override
	public void remove(String id) {
		service.delete(KeyFactory.stringToKey(id));
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
	

}
