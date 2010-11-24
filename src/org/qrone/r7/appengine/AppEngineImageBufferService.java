package org.qrone.r7.appengine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.qrone.img.ImageBuffer;
import org.qrone.img.ImageBufferService;
import org.qrone.util.QrONEUtils;

import com.google.appengine.api.images.ImagesServiceFactory;

@Deprecated
public class AppEngineImageBufferService implements ImageBufferService{

	@Override
	public ImageBuffer createImage(int width, int height) {
		return new AppEngineImageBuffer(width, height);
	}

	@Override
	public ImageBuffer createImage(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			QrONEUtils.copy(in, out);
		} catch (IOException e) {
			return null;
		}
		return new AppEngineImageBuffer(
				ImagesServiceFactory.makeImage(out.toByteArray())
				);
	}

	

}
