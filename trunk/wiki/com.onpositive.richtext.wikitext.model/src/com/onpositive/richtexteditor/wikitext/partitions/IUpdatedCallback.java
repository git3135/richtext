package com.onpositive.richtexteditor.wikitext.partitions;

import java.util.Collection;

import com.onpositive.richtext.model.IPartition;

public interface IUpdatedCallback
{
	/**
	 * Is called, when partition list update ends.
	 * @param updated list of updated partitions
	 */
	void partitionsUpdated(Collection<IPartition> updated);
}
