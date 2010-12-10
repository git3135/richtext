package com.onpositive.richtexteditor.model.resources;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

import com.onpositive.richtext.model.IRegionCompositeWrapper;
import com.onpositive.richtext.model.IRegionCompositeWrapperListener;
import com.onpositive.richtext.model.RegionCompositeEvent;
import com.onpositive.richtext.model.RegionPartition;

/**
 * @author kor
 * 
 */
public class CompositeEditorWrapper implements IRegionCompositeWrapper {
	HashSet<IRegionCompositeWrapperListener> listeners = new HashSet<IRegionCompositeWrapperListener>(
			2);
	protected Composite widget;
	protected StyledText innerStyledText;
	protected int oldLineCount;
	protected RegionPartition ownerPartition;
	protected boolean needsReinit = false;
	protected static int MAX_EDITOR_VERTICAL_SIZE = 600;

	/**
	 * @param widget
	 */
	public CompositeEditorWrapper(Composite widget) {
		this.widget = widget;
		if (widget instanceof StyledText)
			innerStyledText = (StyledText) widget;
		else
			innerStyledText = tryToSearchStyledText(widget);
		if (innerStyledText != null) {
			oldLineCount = innerStyledText.getLineCount();
			innerStyledText.setKeyBinding(SWT.DEL, ST.DELETE_NEXT); // This
																	// binding
																	// is being
																	// deleted
																	// due to a
																	// bug fixed
																	// long time
																	// ago. So
																	// we need
																	// to
																	// restore
																	// it.
			innerStyledText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					if (innerStyledText.getLineCount() != oldLineCount) {
						oldLineCount = innerStyledText.getLineCount();
						int y = 10;
						if (innerStyledText.getHorizontalBar() != null)
							y += innerStyledText.getHorizontalBar().getSize().y;
						int verticalSize = innerStyledText.getLineCount()
								* innerStyledText.getLineHeight() + y;
						if (verticalSize > MAX_EDITOR_VERTICAL_SIZE)
							verticalSize = MAX_EDITOR_VERTICAL_SIZE;
						setSize(getWidth(), verticalSize);
						notifyListeners(new RegionCompositeEvent(
								CompositeEditorWrapper.this,
								RegionCompositeEvent.LINE_COUNT_CHANGE));

						// innerStyledText.getParent().redraw(); //TODO Yet
						// commented; but not sure, we'll don't need this
					}
					notifyListeners(new RegionCompositeEvent(
							CompositeEditorWrapper.this,
							RegionCompositeEvent.SET_DIRTY));
				}

			});
			final Listener caretListener = new Listener() {
				int caretPos = 0;

				public void handleEvent(Event event) {
					if (innerStyledText.getCaretOffset() != caretPos) {
						notifyListeners(new RegionCompositeEvent(
								CompositeEditorWrapper.this,
								RegionCompositeEvent.CARET_MOVE,
								innerStyledText.getCaret()));
						caretPos = innerStyledText.getCaretOffset();
					}
				}
			};
			innerStyledText.addListener(SWT.MouseDown, caretListener);
			innerStyledText.addListener(SWT.KeyDown, caretListener);

		}
	}

	public void addRegionCompositeWrapperListener(
			IRegionCompositeWrapperListener listener) {
		listeners.add(listener);
	}

	protected void notifyListeners(RegionCompositeEvent event) {
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
			IRegionCompositeWrapperListener listener = (IRegionCompositeWrapperListener) iterator
					.next();
			listener.handleEvent(event);
		}
	}

	public int getHeight() {
		return widget.getBounds().height;
	}

	public int getInitialHeight() {
		final ScrollBar horizontalBar = innerStyledText.getHorizontalBar();
		int y = 0;
		if (horizontalBar != null)
			y = horizontalBar.getSize().y + y;
		return Math.min(MAX_EDITOR_VERTICAL_SIZE, innerStyledText
				.getLineHeight()
				* Math.max(1, innerStyledText.getLineCount()) + y);
	}

	public com.onpositive.richtext.model.meta.Point getSize() {
		Point size = widget.getSize();
		return new com.onpositive.richtext.model.meta.Point(size.x,size.y);
	}

	public int getWidth() {
		return widget.getBounds().width;
	}

	public void removeRegionCompositeWrapperListener(
			IRegionCompositeWrapperListener listener) {
		listeners.remove(listener);
	}

	public com.onpositive.richtext.model.meta.Point getLocation() {
		Point location = widget.getLocation();
		return new com.onpositive.richtext.model.meta.Point(location.x,location.y);
	}

	public int getX() {
		return widget.getBounds().x;
	}

	public int getY() {
		return widget.getBounds().y;
	}

	public com.onpositive.richtext.model.meta.Rectangle getBounds() {
		Rectangle bounds = widget.getBounds();
		return new com.onpositive.richtext.model.meta.Rectangle(bounds.x,
				bounds.y, bounds.width, bounds.height);
	}

	protected StyledText tryToSearchStyledText(Composite parent) {
		if (parent instanceof StyledText)
			return (StyledText) parent;
		Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Composite) {
				StyledText styledText = tryToSearchStyledText((Composite) children[i]);
				if (styledText != null)
					return styledText;
			}
		}
		return null;
	}

	public Composite getMainObject() {
		if (innerStyledText == null)
			return widget;
		return innerStyledText;
	}

	public void dispose() {
		widget.dispose();
	}

	public void setLocation(int x, int y) {
		if(widget.getLocation().equals(new Point(x, y))){
			return;
		}
		widget.setLocation(x, y);
	}

	public void setSize(int width, int height) {
		widget.setSize(width, height);
	}

	public Composite getTopLevelObject() {
		return widget;
	}

	public void setText(String initialText) {
		if (innerStyledText != null)
			innerStyledText.setText(initialText);
		/*else
			throw new IllegalStateException(
					"There's no suitable component to set text to!");*/
	}

	public String getContent() {
		if (innerStyledText != null)
			return innerStyledText.getText();
		else
			throw new IllegalStateException(
					"There's no suitable component to get text from!");
	}

}
