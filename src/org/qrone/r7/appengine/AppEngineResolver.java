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

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

public class AppEngineResolver implements URIResolver{
	private static final String KIND = "qrone.filesystem";
	private static final String DATA = "data";
	
	private DatastoreService service;
	private Map<String, Entity> weakmap = new WeakHashMap<String, Entity>();
	private Map<String, Object> noneset = new HashMap<String, Object>();
	
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
			noneset.put(path, Boolean.TRUE);
			return e;
		} catch (EntityNotFoundException ex) {
			noneset.put(path, Boolean.FALSE);
		}
		
		return null;
	}

	@Override
	public boolean exist(String path) {
		if(noneset.containsKey(path)){
			if(noneset.get(path).equals(Boolean.FALSE))
				return false;
			else
				return true;
		}
		return get(path) != null;
	}

	@Override
	public boolean updated(URI uri) {
		String path = uri.toString();
		return noneset.get(path) == null;
	}

	@Override
	public InputStream getInputStream(URI uri) throws IOException {
		Entity e = get(uri.toString());
		if(e != null){
			Blob b = (Blob)e.getProperty(DATA);
			return new ByteArrayInputStream(b.getBytes());
		}
		return null;
	}

	@Override
	public OutputStream getOutputStream(URI uri) throws IOException {
		String path = uri.toString();
		return new AppEngineOutputStream(path);
	}
	
	private class AppEngineOutputStream extends ByteArrayOutputStream{
		private String path;
		public AppEngineOutputStream(String path) {
			this.path = path;
		}
		
		@Override
		public void close() throws IOException {
			super.close();
			Entity e = new Entity(KeyFactory.createKey(KIND, path));
			e.setProperty(DATA, new Blob(toByteArray()));
			service.put(e);
		}
	}

	@Override
	public boolean remove(URI uri) {
		String path = uri.toString();
		weakmap.remove(path);
		noneset.remove(path);
		service.delete(KeyFactory.createKey(KIND, path));
		return true;
	}

}
