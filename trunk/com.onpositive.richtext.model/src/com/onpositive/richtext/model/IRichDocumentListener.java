/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.onpositive.richtext.model;

import com.onpositive.richtext.model.meta.DocumentEvent;

/**
 * @author kor
 * Interface for document listeners
 */
public interface IRichDocumentListener {

	/**
	 * The manipulation described by the document event will be performed.
	 *
	 * @param event the document event describing the document change
	 */
	void documentAboutToBeChanged(DocumentEvent event);
	
	/**
	 * The manipulation described by the document event has been performed.
	 *
	 * @param event the document event describing the document change
	 * @param change change 
	 */
	void documentChanged(DocumentEvent event,RichDocumentChange change);
}
