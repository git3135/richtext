package org.eclipse.swt.custom;

/**
 * This converter does nothing actually - simply return a value itself
 * @author 32kda
 *
 */
public class OneToOneConverter implements IDimensionConverter {

	public double convert(double value) {
		return value;
	}

}
