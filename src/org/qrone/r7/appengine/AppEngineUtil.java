package org.qrone.r7.appengine;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.arnx.jsonic.JSON;

import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.qrone.r7.script.browser.User;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.ShortBlob;
import com.google.appengine.api.datastore.Text;

public class AppEngineUtil {

	public static Map<String, Object> fromEntity(Entity e){
		return fromEntity(e, null);
	}
	
	public static Map<String, Object> fromEntity(Entity e, Scriptable filter){
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(e.getProperties());
		for (Iterator<Entry<String, Object>> i = map.entrySet().iterator(); i
				.hasNext();) {
			Entry<String, Object> item = i.next();
			if(filter != null && !filter.has(item.getKey(), filter)){
				i.remove();
			}else{
				item.setValue(from(item.getValue()));
			}
		}
		return map;
	}
	
	public static Object from(Object ds){
		if(ds instanceof Text){
			return ((Text)ds).toString();
		}else if(ds instanceof Blob){
			return ((Blob)ds).getBytes();
		}
		return ds;
	}
	
	public static Entity toEntity(String collection, Scriptable o){
		Entity e = new Entity(collection);
		Object[] l = o.getIds();
		for (int i = 0; i < o.getIds().length; i++) {
			Object obj = null;
			if(!(l[i] instanceof Number)){
				obj = o.get(((String)l[i]), o);
				e.setProperty((String)l[i], to(obj));
			}
			
		}
		return e;
	}

	public static Object to(Object obj){
		if(obj instanceof NativeJavaObject){
			obj = ((NativeJavaObject)obj).unwrap();
		}
		
		if(obj instanceof Boolean){
			return obj;
		}else if(obj instanceof Number){
			return new Double(((Number)obj).doubleValue());
		}else if(obj instanceof String){
			String ostr = (String)obj;
			if(ostr.length() > 500){
				return new Text(ostr);
			}else{
				return ostr;
			}
		}else if(obj instanceof byte[]){
			byte[] buf = (byte[])obj;
			if(buf.length > 500){
				return new Blob(buf);
			}else{
				return new ShortBlob(buf);
			}
		}else if(obj instanceof Date){
			return obj;
		}else if(obj instanceof User){
			return "openid:" + ((User)obj).getKey();
		}else if(obj instanceof Scriptable){
			return "json:" + JSON.encode(obj);
		}else{
			// null, undefined or not_found.
		}
		return null;
	}
}
