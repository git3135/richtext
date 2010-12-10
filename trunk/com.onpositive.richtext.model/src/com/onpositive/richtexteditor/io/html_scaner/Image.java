package com.onpositive.richtexteditor.io.html_scaner;

import java.net.URL;

import com.onpositive.richtext.model.meta.IImage;
import com.onpositive.richtext.model.meta.Rectangle;

public class Image implements IImage {

	String url;
	
	
	public Image(String curSrc) {
		this.url=curSrc;
	}

	public Image(URL resourceAsStream) {
		this.url=resourceAsStream.toExternalForm();
	}

	public Rectangle getBounds() {
		return null;
	}

	public String url() {		
		return url;
	}

}
