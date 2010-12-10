/**
 * 
 */
package com.onpositive.richtexteditor.viewer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.DefaultAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension2;
import org.eclipse.jface.text.source.projection.AnnotationBag;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public final class WrapAwareAnnotationHover extends
		DefaultAnnotationHover {
	private final CompositeRuler verticalRuler;

	public WrapAwareAnnotationHover(CompositeRuler verticalRuler) {
		this.verticalRuler = verticalRuler;
	}

	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		Point c = Display.getDefault().getCursorLocation();
		Point cm = verticalRuler.getControl().toControl(c);
		int offsetAtLocation = sourceViewer.getTextWidget()
				.getOffsetAtLocation(cm)+sourceViewer.getTextWidget().getTopPixel();
		List javaAnnotations= getAnnotationsForLine(sourceViewer, lineNumber,offsetAtLocation);
		if (javaAnnotations != null) {

			if (javaAnnotations.size() == 1) {

				// optimization
				Annotation annotation= (Annotation) javaAnnotations.get(0);
				String message= annotation.getText();
				if (message != null && message.trim().length() > 0)
					return formatSingleMessage(message);

			} else {

				List messages= new ArrayList();

				Iterator e= javaAnnotations.iterator();
				while (e.hasNext()) {
					Annotation annotation= (Annotation) e.next();
					String message= annotation.getText();
					if (message != null && message.trim().length() > 0)
						messages.add(message.trim());
				}
				
				if (messages.size() == 1)
					return formatSingleMessage((String)messages.get(0));

				if (messages.size() > 1)
					return formatMultipleMessages(messages);
			}
		}

		

		return null;
	}

	private boolean isRulerLine(Position position, IDocument document, int line) {
		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				return line == document.getLineOfOffset(position.getOffset());
			} catch (BadLocationException x) {
			}
		}
		return false;
	}

	private boolean isDuplicateAnnotation(Map messagesAtPosition, Position position, String message) {
		if (messagesAtPosition.containsKey(position)) {
			Object value= messagesAtPosition.get(position);
			if (message.equals(value))
				return true;

			if (value instanceof List) {
				List messages= (List)value;
				if  (messages.contains(message))
					return true;

				messages.add(message);
			} else {
				ArrayList messages= new ArrayList();
				messages.add(value);
				messages.add(message);
				messagesAtPosition.put(position, messages);
			}
		} else
			messagesAtPosition.put(position, message);
		return false;
	}

	private boolean includeAnnotation(Annotation annotation, Position position, HashMap messagesAtPosition) {
		if (!isIncluded(annotation))
			return false;

		String text= annotation.getText();
		return (text != null && !isDuplicateAnnotation(messagesAtPosition, position, text));
	}

	private IAnnotationModel getAnnotationModel(ISourceViewer viewer) {
		if (viewer instanceof ISourceViewerExtension2) {
			ISourceViewerExtension2 extension= (ISourceViewerExtension2) viewer;
			return extension.getVisualAnnotationModel();
		}
		return viewer.getAnnotationModel();
	}

	List getAnnotationsForLine(ISourceViewer viewer, int line, int offsetAtLocation) {
		IAnnotationModel model = getAnnotationModel(viewer);
		if (model == null)
			return null;

		IDocument document = viewer.getDocument();
		List javaAnnotations = new ArrayList();
		HashMap messagesAtPosition = new HashMap();
		Iterator iterator = model.getAnnotationIterator();
		Annotation min=null;
		int minDelta=Integer.MAX_VALUE;
		while (iterator.hasNext()) {
			Annotation annotation = (Annotation) iterator
					.next();

			Position position = model.getPosition(annotation);
			if (position == null)
				continue;

			if (!isRulerLine(position, document, line))
				continue;

			if (annotation instanceof AnnotationBag) {
				AnnotationBag bag = (AnnotationBag) annotation;
				Iterator e = bag.iterator();
				while (e.hasNext()) {
					annotation = (Annotation) e.next();
					position = model.getPosition(annotation);
					if (position != null
							&& includeAnnotation(annotation,
									position,
									messagesAtPosition))
					{
						int abs = Math.abs(offsetAtLocation-position.offset);
						if (abs<minDelta){
							minDelta=abs;
							min=annotation;
						}
						
						//javaAnnotations.add(annotation);
					}
				}
				continue;
			}
			if (includeAnnotation(annotation, position,
					messagesAtPosition))
			{
				int abs = Math.abs(offsetAtLocation-position.offset);
				if (abs<minDelta){
					minDelta=abs;
					min=annotation;
				}
				//javaAnnotations.add(annotation);
			}
		}
		if (min!=null){
		javaAnnotations.add(min);
		}
		return javaAnnotations;
	}
}