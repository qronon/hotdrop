package org.qrone.r7.github;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

import org.ho.yaml.Yaml;
import org.qrone.r7.appengine.AppEngineURLFetcher;
import org.qrone.r7.fetcher.URLFetcher;
import org.qrone.r7.handler.URIHandler;
import org.qrone.r7.resolver.URIResolver;
import org.qrone.util.QrONEUtils;

public class GitHubResolver implements URIResolver, URIHandler {
	private static final Logger log = Logger.getLogger(GitHubResolver.class.getName());
	private URLFetcher fetcher;
	private URIResolver cacheresolver;
	private String user;
	private String repo;
	private String treesha;
	
	private Map<String,String> blobs;
	private Set<String> updatedSet = new HashSet<String>();
	
	public GitHubResolver(URLFetcher fetcher, URIResolver cacheresolver, String user, String repo, String treesha){
		this.fetcher = fetcher;
		this.cacheresolver = cacheresolver;
		this.user = QrONEUtils.escape(user);
		this.repo = QrONEUtils.escape(repo);
		this.treesha = QrONEUtils.escape(treesha);
	}
	
	public void clear(){
		blobs = null;
	}
	
	private Map<String,String> getFiles(){
		if(blobs == null){
			try {
				Map map = (Map)Yaml.load(fetcher.fetch("http://github.com/api/v2/yaml/blob/all/" 
						+ user + "/" + repo + "/" + treesha));
				blobs = (Map<String,String>)map.get("blobs");
			} catch (IOException e) {}
		}
		return blobs;
	}
	
	@Override
	public boolean exist(String path) {
		getFiles();
		if(cacheresolver.exist(path)) return true;
		return blobs.containsKey(path.substring(1));
	}

	@Override
	public boolean updated(URI uri) {
		return updatedSet.contains(uri.toString());
	}

	@Override
	public InputStream getInputStream(URI uri) throws IOException {
		InputStream in = cacheresolver.getInputStream(uri);
		if(in != null) return in;
		
		updatedSet.remove(uri.toString());
		String sha = getFiles().get(uri.toString().substring(1));
		if(sha != null){
			InputStream fin =  fetcher.fetch("http://github.com/api/v2/yaml/blob/show/" 
					+ user + "/" + repo + "/" + sha);
			byte[] bytes = QrONEUtils.read(fin);
			
			OutputStream out = cacheresolver.getOutputStream(uri);
			out.write(bytes);
			out.flush();
			out.close();
			
			return new ByteArrayInputStream(bytes);
		}
		return null;
	}

	@Override
	public OutputStream getOutputStream(URI uri) throws IOException {
		OutputStream out = cacheresolver.getOutputStream(uri);
		return out;
	}

	@Override
	public boolean handle(HttpServletRequest request,
			HttpServletResponse response, String path, String pathArg) {
		if(path.equals("/qrone-server/github-post-receive")){
			try {
				log.config("Github post-receive hooks.");
				blobs = null;
				Map map = (Map)JSON.decode(request.getParameter("payload"));
				List<Map> list = (List)map.get("commits");
				
				for (Map m : list) {
					List<String> removed = (List)m.get("removed");
					for (String p : removed) {
						try {
							log.config("removed: /" + p);
							cacheresolver.remove(new URI("/" + p));
							updatedSet.add("/" + p);
						} catch (URISyntaxException e) {}
					}
					List<String> modified = (List)m.get("modified");
					for (String p : modified) {
						try {
							log.config("modified: /" + p);
							cacheresolver.remove(new URI("/" + p));
							updatedSet.add("/" + p);
						} catch (URISyntaxException e) {}
					}
				}
				
				/*
				Map<String,String> oldblobs = blobs;
				getFiles();
				
				if(oldblobs != null){
					for (Entry<String,String> e : oldblobs.entrySet()) {
						String sha = blobs.get(e.getKey());
						if(sha == null || !sha.equals(e.getValue())){
							try {
								cacheresolver.remove(new URI("/" + e.getKey()));
								updatedSet.add("/" + e.getKey());
							} catch (URISyntaxException e1) {}
						}
					}
				}
				*/
				
				/*
				
				Map body = (Map)JSON.decode(request.getInputStream());
				List<Map> commits = (List)body.get("commits");
				
				for (Map map : commits) {
					String id = (String)map.get("id");
					InputStream in = fetcher.fetch("http://github.com/api/v2/json/commits/show/" + user + "/" + repo + "/" + id);
					Map commit = (Map)((Map)JSON.decode(in)).get("commit");
					
					List<String> added = (List)commit.get("added");
					for (String file : added) {
						clearCache(file);
					}
					
					List<String> removed = (List)commit.get("removed");
					for (String file : removed) {
						clearCache(file);
					}
					
					List<Map> modified = (List)commit.get("modified");
					for (Map m : modified) {
						String file = (String)m.get("filename");
						clearCache(file);
					}
				}
				*/
				
				return true;
			} catch (ClassCastException e) {
			}
		}
		return false;
	}
	
	/*
	private void clearCache(String path){
		updatedSet.add(path);
		blobs.remove(path);
		try {
			cacheresolver.remove(new URI(path));
		} catch (URISyntaxException e) {
		}
	}
	*/
	
	@Override
	public boolean remove(URI uri) {
		return false;
	}
}
