package org.eclipse.swt.custom;

import org.eclipse.swt.widgets.Display;
/**
 * Can be used to convert some values from millimeters into pixels
 * @author 32kda
 *
 */
public class MetricToPixelConverter implements IDimensionConverter {
	protected final static double MMPI = 25.4;

	public double convert(double value) {
		int dpiX = Display.getDefault().getDPI().x;
		double inches = value / MMPI;
		return (int) Math.round(inches * dpiX);
	}
}
