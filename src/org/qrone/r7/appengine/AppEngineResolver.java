package org.qrone.r7.appengine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.qrone.r7.resolver.URIResolver;
import org.qrone.r7.resolver.SHAResolver;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

public class AppEngineResolver implements SHAResolver{
	private static final String KIND = "qrone.filesystem";
	private static final String DATA = "data";
	private static final String SHA = "sha";
	
	private DatastoreService service;
	private Map<String, Entity> weakmap = new WeakHashMap<String, Entity>();
	private Map<String, Object> shaset = new HashMap<String, Object>();
	
	public AppEngineResolver() {
		service = DatastoreServiceFactory.getDatastoreService();
	}
	
	private Entity get(String path){
		Entity e = weakmap.get(path);
		if(e != null)
			return e;
		try {
			e = service.get(KeyFactory.createKey(KIND, path));
			weakmap.put(path, e);
			shaset.put(path, e.getProperty(SHA));
			return e;
		} catch (EntityNotFoundException ex) {
			shaset.put(path, Boolean.FALSE);
		}
		
		return null;
	}

	@Override
	public boolean exist(String path) {
		if(shaset.containsKey(path)){
			if(shaset.get(path).equals(Boolean.FALSE))
				return false;
			else
				return true;
		}
		return get(path) != null;
	}

	@Override
	public boolean updated(URI uri) {
		return updated(uri, null);
	}

	@Override
	public InputStream getInputStream(URI uri) throws IOException {
		return getInputStream(uri, null);
	}

	@Override
	public OutputStream getOutputStream(URI uri) throws IOException {
		return getOutputStream(uri, null);
	}
	
	private class AppEngineOutputStream extends ByteArrayOutputStream{
		private String path;
		private String sha;
		public AppEngineOutputStream(String path, String sha) {
			this.path = path;
			this.sha = sha;
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			Entity e = new Entity(KeyFactory.createKey(KIND, path));
			e.setProperty(DATA, new Blob(toByteArray()));
			e.setProperty(SHA, sha);
			service.put(e);
		}
	}

	@Override
	public boolean remove(URI uri) {
		String path = uri.toString();
		weakmap.remove(path);
		shaset.remove(path);
		service.delete(KeyFactory.createKey(KIND, path));
		return true;
	}

	@Override
	public boolean updated(URI uri, String sha) {
		String path = uri.toString();
		Object s = shaset.get(path);
		return s == null || !s.equals(sha);
	}

	@Override
	public InputStream getInputStream(URI uri, String sha) throws IOException {
		Entity e = get(uri.toString());
		if(e != null){
			if(sha == null || sha.equals(e.getProperty(SHA))){
				Blob b = (Blob)e.getProperty(DATA);
				return new ByteArrayInputStream(b.getBytes());
			}
		}
		return null;
	}

	@Override
	public OutputStream getOutputStream(URI uri, String sha) throws IOException {
		String path = uri.toString();
		return new AppEngineOutputStream(path, sha);
	}

}
