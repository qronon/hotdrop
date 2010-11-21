package org.qrone.r7.appengine;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.qrone.img.ImageBuffer;
import org.qrone.img.ImageRect;

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

public class AppEngineImageBuffer implements ImageBuffer{
	private Image image;
	private int width;
	private int height;
	private List<Composite> composites = new ArrayList<Composite>();
	public AppEngineImageBuffer(Image image){
		this.image = image;
		width = image.getWidth();
		height = image.getHeight();
	}

	public AppEngineImageBuffer(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public void drawImage(ImageBuffer img, ImageRect to, ImageRect from) {
		AppEngineImageBuffer buf = (AppEngineImageBuffer)img;
		int w = img.getWidth();
		int h = img.getHeight();
		double x1 = ((double)from.x)/((double)w);
		double y1 = ((double)from.y)/((double)h);
		double x2 = ((double)(from.x+from.w))/((double)w);
		double y2 = ((double)(from.y+from.h))/((double)h);
		try{
			Transform t = ImagesServiceFactory.makeCrop(x1,y1,x2,y2);
			Image a = ImagesServiceFactory.makeImage(buf.image.getImageData());
			Image i = ImagesServiceFactory.getImagesService().applyTransform(t, a);
			Composite c = ImagesServiceFactory.makeComposite(i, to.x, to.y, 1, Composite.Anchor.TOP_LEFT);
			composites.add(c);
		}catch(Exception e){}
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void writeTo(OutputStream out) {
		Image i = ImagesServiceFactory.getImagesService()
			.composite(composites, width, height, 0x00000000);
		
		try {
			out.write(i.getImageData());
		} catch (IOException e) {
		}
	}
	
}
