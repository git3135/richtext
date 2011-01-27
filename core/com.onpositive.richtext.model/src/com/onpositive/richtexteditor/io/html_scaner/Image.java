package com.onpositive.richtexteditor.io.html_scaner;

import java.net.URL;

import com.onpositive.richtext.model.meta.IImage;
import com.onpositive.richtext.model.meta.Rectangle;

public class Image implements IImage {

	String url;
	
	
	private Image(String curSrc) {
		this.url=curSrc;
	}

	private Image(URL resourceAsStream) {
		this.url=resourceAsStream.toExternalForm();
	}

	public Rectangle getBounds() {
		return new Rectangle(0,0,20,20);
	}

	public String url() {		
		return url;
	}

}
