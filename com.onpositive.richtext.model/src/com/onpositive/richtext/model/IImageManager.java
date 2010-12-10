package com.onpositive.richtext.model;

import java.util.Observer;

import com.onpositive.richtext.model.meta.IImage;


public interface IImageManager {

	void registerImage(String imageFileName, IImage image);

	IImage checkImage(String imageFileName);

	IImage getImage(String imagePath, Observer observer);

}
