package com.onpositive.richtexteditor.model.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.Document;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.meta.BasicBullet;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;

/**
 * @author kor Determines, that text is divided into partitions, and has
 *         lines, which can have aligns and bullets
 */
public final class SimpleRichTextModel implements
		ISimpleRichTextModel {

	private List<Integer> aligns;
	private List<BasicBullet> bullets;
	private List<BasePartition> styles;
	private String text;

	/**
	 * Basic constructor
	 * 
	 * @param aligns
	 *            line aligns list
	 * @param bullets
	 *            line bullets list
	 * @param styles
	 *            line styles list
	 * @param text
	 *            Text
	 */
	public SimpleRichTextModel(List<Integer> aligns,
			List<BasicBullet> bullets, List<BasePartition> styles,
			String text) {
		super();
		this.aligns = aligns;
		this.bullets = bullets;
		this.styles = styles;
		this.text = text;
	}

	public SimpleRichTextModel() {
		this.aligns=new ArrayList<Integer>();
		this.bullets=new ArrayList<BasicBullet>();
		this.text="";
		this.styles=new ArrayList<BasePartition>();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.ISimpleRichTextModel#getAlign(int)
	 */
	public int getAlign(int line) {
		return aligns.get(line);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.ISimpleRichTextModel#getBullet(int)
	 */
	public BasicBullet getBullet(int line) {
		return bullets.get(line);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.ISimpleRichTextModel#getLineCount()
	 */
	public int getLineCount() {
		return aligns.size();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.ISimpleRichTextModel#getPartitions()
	 */
	public List<BasePartition> getPartitions() {
		return styles;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see com.onpositive.richtexteditor.model.ISimpleRichTextModel#getText()
	 */
	public String getText() {
		return text;
	}

	public int getIndent(int lineIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param model
	 */
	public void append(ISimpleRichTextModel model){			
		int oldCount = new com.onpositive.richtext.model.Document(text).getNumberOfLines();
		int lineCount = model.getLineCount();
		if (oldCount > 0)
		{
			int newCount = new com.onpositive.richtext.model.Document(text + model.getText()).getNumberOfLines();
			int delta = oldCount + lineCount - newCount; //Checking line count match before and after addition
			if (delta == 1) //If it doesn't match, remove last bullet from original model
			{
				if (oldCount <= aligns.size()) 
					aligns.remove(oldCount - 1);
				if (oldCount <= bullets.size()) 
					bullets.remove(oldCount - 1);
			}
			else if (delta < 0 || delta > 1)
			{
				new IllegalStateException("Bad line attributes list match").printStackTrace();
			}
		}
		for (int a=0;a<lineCount;a++){
			aligns.add(model.getAlign(a));
			bullets.add(model.getBullet(a));
		}
		for (BasePartition p:model.getPartitions()){
			BasePartition copy=p.clone();
			copy.setOffset(copy.getOffset()+text.length());
			this.styles.add(copy);
		}
		
		text+=model.getText();
	}
	
	/**
	 * @param text
	 * @param align
	 * @param bullet
	 * @param partitions
	 */
	public void appendLine(String text,int align,BasicBullet bullet,BasePartition... partitions){			
		this.aligns.add(align);
		this.bullets.add(bullet);
		for (BasePartition p:partitions){
			BasePartition copy=p.clone();
			copy.setOffset(copy.getOffset()+this.text.length());
			
			this.styles.add(copy);
		}
		this.text+=text;
	}
	
	/**
	 * Aligns and bullets should have size corresponding to actual number of lines being added
	 * @param text Text to append
	 * @param aligns aligns for that text; Shouldn't be null
	 * @param bullets bullets for that text
	 * @param partitions partitions for that text. Should be a consistens consecutive partitions list
	 */
	public void appendText(String text, List<Integer> aligns, List<BasicBullet> bullets, List<BasePartition> partitions)
	{
		BufferedReader m=new BufferedReader(new StringReader(text));
		int lineCount=0;
		while (true){
			String s=null;
			try {
				s = m.readLine();
				
			} catch (IOException e) {
				
			}
			if (s==null){
				break;
			}
			lineCount++;
		}
		for (int a=0;a<lineCount;a++){
			if (aligns.size() > a)
				this.aligns.add(aligns.get(a));
			else
				this.aligns.add(0);
			if (bullets.size() > a)
				this.bullets.add(bullets.get(a));
			else
				this.bullets.add(null);

		}			
		for (BasePartition p:partitions){
			BasePartition copy=p.clone();
			copy.setOffset(copy.getOffset()+this.text.length());
			
			this.styles.add(copy);
		}
		this.text += text;
	}
}
