package com.onpositive.richtext.model.meta;


/**
 * A region describes a certain range in an indexed text store. Text stores are
 * for example documents or strings. A region is defined by its offset into the
 * text store and its length.
 * <p>
 * A region is considered a value object. Its offset and length do not change
 * over time.
 * <p>
 * Clients may implement this interface or use the standard implementation
 * {@link org.eclipse.jface.text.Region}.
 * </p>
 */
public interface IRegion {

	/**
	 * Returns the length of the region.
	 *
	 * @return the length of the region
	 */
	int getLength();

	/**
	 * Returns the offset of the region.
	 *
	 * @return the offset of the region
	 */
	int getOffset();
}
