/**
 * 
 */
package com.onpositive.richtexteditor.model;

import java.util.List;

import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.StyleRange;


/**
 * @author 32kda	
 *
 */
public interface IRichtextDecorator {
	
	/**
	 * Decorates {@link StyleRange} list - returns list of StyleRanges, after decoration 
	 * @param ranges {@link StyleRange} list to decorate
	 * @param doc {@link Document} this StyleRange list belongs to
	 * @return decorated list of StyleRanges or list itself if it's nothing to decorate
 	 */
	List<StyleRange> decorateStyleRange(List<StyleRange> ranges, ITextDocument doc);

}
