package com.onpositive.richtexteditor.wikitext.partitions;

import java.util.Collection;

import com.onpositive.richtext.model.BasePartition;

public interface IPartitionsUpdater {

	boolean updatePartition(BasePartition partition);
	/**
	 * Performs a bulk update for several partitions. Should be used, if update operation can take a significant time
	 * and started in some background thread. After update ends< a list of partitions, that actually changed,
	 * is passed to a callback
	 * @param partitions Partitions to check for being updated
	 * @param callback callback to call when operation ends
	 * @return list of partitions, that actually updated
	 */
	void updatePartitions(Collection<BasePartition> partitions, IUpdatedCallback callback);
}
