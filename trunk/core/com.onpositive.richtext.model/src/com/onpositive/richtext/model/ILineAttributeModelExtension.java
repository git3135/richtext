package com.onpositive.richtext.model;

import com.onpositive.richtext.model.meta.BasicBullet;

public interface ILineAttributeModelExtension extends ILineAttributeModel{

	void setBulk(int[] aligns, BasicBullet[] bullets, int[] indents);

}
