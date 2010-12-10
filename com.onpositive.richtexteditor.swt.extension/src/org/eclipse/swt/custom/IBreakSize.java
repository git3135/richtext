package org.eclipse.swt.custom;

public interface IBreakSize {
	static int BREAK_SIZE_IN_LINES = 1;
	static int BREAK_SIZE_IN_PIXELS = 2;
	static int BREAK_SIZE_IN_CM = 3;
	int getValue();
	int getType();
}
