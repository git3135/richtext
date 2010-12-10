package com.onpositive.richtexteditor.model.changes;

import com.onpositive.richtext.model.LinkPartition;
import com.onpositive.richtext.model.PartitionDelta;

/**
 * Class represents change for setting {@link LinkPartition}'s url string
 * @author 32kda
 */
public class SetUrlChange extends AbstractPartitionChange
{
	protected String newUrl;

	/**
	 * Creates change
	 * @param partition {@link LinkPartition} to set url to
	 * @param newUrl new url to set
	 */
	public SetUrlChange(LinkPartition partition, String newUrl)
	{
		super(partition);
		this.newUrl = newUrl;
	}

	/* (non-Javadoc)
	 * @see com.onpositive.richtexteditor.model.changes.Change#apply(com.onpositive.richtexteditor.model.partitions.PartitionDelta)
	 */
	public void apply(PartitionDelta delta)
	{
		String oldUrl = ((LinkPartition) partition).getUrl(); 
		((LinkPartition) partition).setUrl(newUrl);
		delta.getUndoChange().add(new SetUrlChange((LinkPartition) partition, oldUrl));
	}

}
