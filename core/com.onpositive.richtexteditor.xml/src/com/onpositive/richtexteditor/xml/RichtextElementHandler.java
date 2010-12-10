package com.onpositive.richtexteditor.xml;

import java.util.ArrayList;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onpositive.commons.xml.language.Context;
import com.onpositive.commons.xml.language.IElementHandler;
import com.onpositive.richtexteditor.actions.ActionFactory;
import com.onpositive.richtexteditor.io.IFormatFactory;
import com.onpositive.richtexteditor.model.resources.LayerManager;
import com.onpositive.semantic.model.ui.property.editors.IViewerConfigurator;
import com.onpositive.semantic.ui.xml.UIElementHandler;

public class RichtextElementHandler extends UIElementHandler implements
		IElementHandler
{
	

	public RichtextElementHandler()
	{

	}

	
	protected Object createElement(Element element, Object parentContext,
			String localName, final Context ctx)
	{
		if (localName.equals("richtext"))
		{
			RichTextEditorWrapper wrapper = new RichTextEditorWrapper();
			for (int i = 0; i < element.getAttributes().getLength(); i++)
			{
				Attr n = (Attr) element.getAttributes().item(i);
				String nodeName = n.getNodeName();
				if (nodeName.equals("richTextConfigurator"))
				{
					RichtextConfigurator configurator = (RichtextConfigurator) getClassInstanceForNodeValue(n,IViewerConfigurator.class,ctx.getClassLoader());
					wrapper.setConfigurator(configurator);
				}
				else if (nodeName.equals("formatFactoryClass")) 
				{
					IFormatFactory factory = (IFormatFactory) getClassInstanceForNodeValue(n,IFormatFactory.class,ctx.getClassLoader());
					wrapper.setFormatFactory(factory);					
				}
				else if (nodeName.equals("useExternalToolbar")) 
				{
					boolean useExternalToolbar = Boolean.parseBoolean(n.getNodeValue().trim().toLowerCase());
					wrapper.setCreateToolbar(!useExternalToolbar);					
				}
			}
			final NodeList nodes = element.getElementsByTagName("actions");
			if (nodes.getLength() > 0)
			{
				wrapper.setActionFactorycreator(new RichTextEditorWrapper.ActionFactoryCreator()
						{

							
							public ActionFactory getActionFactory(
									LayerManager manager, ISourceViewer viewer)
							{
								ActionFactory fs = new ActionFactory(manager,
										viewer)
								{
									
									
									public ArrayList<IContributionItem> createActionsList()
									{
										ArrayList<IContributionItem> resList = new ArrayList<IContributionItem>();
										Element actions = (Element) nodes
										.item(0);
										NodeList childNodes = actions
												.getChildNodes();
										int length = childNodes.getLength();
										for (int i = 0; i < length; i++)
										{
											String nodeName = childNodes
													.item(i).getNodeName();
											if (nodeName.equals("boldAction"))
												resList.add(new ActionContributionItem(getBoldAction()));
											else if (nodeName.equals("italicAction"))
												resList.add(new ActionContributionItem(getItalicAction()));
											else if (nodeName.equals("underlineAction"))
												resList.add(new ActionContributionItem(getUnderlineAction()));
											else if (nodeName.equals("strikethroughAction"))
												resList.add(new ActionContributionItem(getStrikeThroughAction()));
											else if (nodeName.equals("addHRAction"))
												resList.add(new ActionContributionItem(getAddHRAction()));
											else if (nodeName.equals("addImageAction"))
												resList.add(new ActionContributionItem(getAddImageAction()));
											else if (nodeName.equals("openFileAction"))
												resList.add(new ActionContributionItem(getOpenFileAction()));
											else if (nodeName.equals("bulletedListAction"))
												resList.add(new ActionContributionItem(getBulletedListAction()));
											else if (nodeName.equals("numberedListAction"))
												resList.add(new ActionContributionItem(getNumberedListAction()));
											else if (nodeName.equals("leftAlignAction"))
												resList.add(new ActionContributionItem(getAlignLeftAction()));
											else if (nodeName.equals("rightAlignAction"))
												resList.add(new ActionContributionItem(getAlignRightAction()));
											else if (nodeName.equals("centerAlignAction"))
												resList.add(new ActionContributionItem(getAlignCenterAction()));
											else if (nodeName.equals("fitAlignAction"))
												resList.add(new ActionContributionItem(getAlignJustifyAction()));
											else if (nodeName.equals("listAction"))
												resList.add(new ActionContributionItem(getBulletedListAction()));
											else if (nodeName.equals("numberedListAction"))
												resList.add(new ActionContributionItem(getNumberedListAction()));
											else if (nodeName.equals("separator"))
												resList.add(new Separator());
											else if (nodeName.equals("newLinkAction"))
												resList.add(new ActionContributionItem(getNewLinkAction()));
											else if (nodeName.equals("customAction"))
											{
												IAction action = null;
												String clName = "";
												Attr attr = ((Element)childNodes.item(i)).getAttributeNode("className");
												action = (IAction) getClassInstanceForNodeValue(attr,IAction.class,ctx.getClassLoader());
												if (action != null) resList.add(new ActionContributionItem(action));
											}
										}
										return resList;
									}

								};
								return fs;

							}

						});

			}

			return wrapper;
		}
		return null;
	}
	
	protected Object getClassInstanceForNodeValue(Attr node, Class expectedClass, ClassLoader loader)
	{
		String clName = "";
		Object res = null;
		try
		{
			clName = node.getNodeValue().trim();
			Class cls = loader.loadClass(clName);
			res = cls.newInstance();			
			if (!expectedClass.isInstance(res)) throw new Exception("Loaded class " + clName + " is not an instance of " + expectedClass.getName());
			return res;
		}
		catch (Exception e)
		{
			Activator.getDefault().getLog().log(new Status(Status.WARNING,Activator.PLUGIN_ID,"Class " + clName + " not loaded. Reason: " + e.getMessage()));
		}
		return null;
	}

}
