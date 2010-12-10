package org.eclipse.swt.custom;

public class InPixelsBreakSize implements IBreakSize {

	public int getType() {
		return BREAK_SIZE_IN_PIXELS;
	}

	public int getValue() {		
		return 100;
	}

}
