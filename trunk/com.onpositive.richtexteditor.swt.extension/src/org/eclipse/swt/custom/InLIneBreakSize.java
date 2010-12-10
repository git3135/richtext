package org.eclipse.swt.custom;

public class InLIneBreakSize implements IBreakSize {

	public int getType() {		
		return BREAK_SIZE_IN_LINES;
	}

	public int getValue() {		
		return 7;
	}

}
