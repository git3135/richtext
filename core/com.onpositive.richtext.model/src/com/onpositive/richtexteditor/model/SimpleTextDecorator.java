package com.onpositive.richtexteditor.model;

import java.util.LinkedList;
import java.util.List;


import com.onpositive.richtext.model.meta.ITextDocument;
import com.onpositive.richtext.model.meta.StyleRange;

public abstract class SimpleTextDecorator implements IRichtextDecorator {

	protected int lastDecorationOffset = 0;

	/** (non-Javadoc)
	 * @see com.onpositive.richtexteditor.model.IRichtextDecorator#decorateStyleRange(java.util.List, org.eclipse.jface.text.IDocument)
	 */
	public List<StyleRange> decorateStyleRange(List<StyleRange> ranges, ITextDocument doc) {
		if (ranges==null){
			return ranges;
		}
		StyleRange last = ranges.get(ranges.size() - 1);
		int base = ranges.get(0).start;
		String text = null;
		try {
			text = doc.get(base, last.start + last.length - base);
		} catch (Exception e) {
			e.printStackTrace();
			return ranges;
		}
		lastDecorationOffset = 0;
		int currIdx = getNextDecorationOffset(text); 
		
		if (currIdx == -1)
			return ranges;
		currIdx += base;
		List<StyleRange> resultList = new LinkedList<StyleRange>(ranges);
		int addedCount = 0;
		for (int i = 0; i < ranges.size() && currIdx > -1; i++)
		{
			StyleRange styleRange = ranges.get(i);
			if (styleRange.start <= currIdx && styleRange.start + styleRange.length > currIdx)
			{
				while (currIdx - base > -1 && currIdx < styleRange.start + styleRange.length)
				{
					if (styleRange.length < getMinimumDecorationLength())
					{
						currIdx = getNextDecorationOffset(text);
						if (currIdx > -1)
							currIdx += base;
					}
					else
					{
						int k = currIdx + getMinimumDecorationLength();
						k = getDecoratedAreaTailLength(base, text, styleRange,k);
						StyleRange decoratedRange = styleRange; //We will decorate this range, after cutting all parts from it, which we don't need to decorate
						if (styleRange.start == currIdx)
						{
							if (k != styleRange.start + styleRange.length)
							{
								int oldLength = styleRange.length;
								styleRange.length = k - styleRange.start;
								StyleRange range2 = (StyleRange) styleRange.clone();
								range2.start = k;
								range2.length = styleRange.start + oldLength - k;
								resultList.add(i + addedCount + 1, range2);
								addedCount++;
								styleRange = range2;
							}							
						}
						else
						{
							StyleRange prevRange = (StyleRange) styleRange.clone();
							prevRange.length = currIdx - styleRange.start;
							styleRange.start = currIdx;
							styleRange.length -= prevRange.length;
							resultList.add(i + addedCount, prevRange);
							addedCount++;
							if (k != styleRange.start + styleRange.length)
							{
								int oldLength = styleRange.length;
								styleRange.length = k - styleRange.start;
								StyleRange range2 = (StyleRange) styleRange.clone();
								range2.start = k;
								range2.length = styleRange.start + oldLength - k;
								resultList.add(i + addedCount + 1, range2);
								addedCount++;		
								styleRange = range2; //Because we should analyze this tail
							}
						}
						decorateStyleRange(decoratedRange);
					}
					if (currIdx > -1)
						currIdx = getNextDecorationOffset(text);
					if (currIdx > -1)
						currIdx += base;
				}
			}
		}
		return resultList;
	}

	protected abstract int getDecoratedAreaTailLength(int base, String text,
			StyleRange styleRange, int basePartEndOffset);

	protected abstract int getMinimumDecorationLength();

	protected abstract int getNextDecorationOffset(String text);

	protected abstract void decorateStyleRange(StyleRange decoratedRange);

}
