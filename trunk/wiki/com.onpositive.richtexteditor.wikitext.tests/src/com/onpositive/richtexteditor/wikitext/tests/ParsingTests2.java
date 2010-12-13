package com.onpositive.richtexteditor.wikitext.tests;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Proxy;
import java.util.HashMap;

import junit.framework.TestCase;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.text.Document;
import org.eclipse.mylyn.commons.net.IProxyProvider;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.internal.trac.core.TracClientFactory;
import org.eclipse.mylyn.internal.trac.core.client.ITracClient;
import org.eclipse.mylyn.internal.trac.core.client.ITracWikiClient;
import org.eclipse.mylyn.internal.trac.core.client.TracException;
import org.eclipse.mylyn.internal.trac.core.client.ITracClient.Version;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtexteditor.io.ModelBasedLineInformationProvider;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.util.DocumentWrapper;
import com.onpositive.richtexteditor.wikitext.io.DefaultWikitextLoader;
import com.onpositive.richtexteditor.wikitext.io.WikitextSerializer;
import com.onpositive.richtexteditor.wikitext.ui.WikitextPartitionLayer;


public class ParsingTests2 extends TestCase
{
	private static final String TEST = "test";
	BasePartition linkPrototypePartition = null;
	BasePartitionLayer layer = null;
	private ITracClient repository;
	private String repositoryUrl;
	private String username;
	private String password;
	
	public void test01()
	{
		simpleParsingTest("2.txt");
	}
	
//	public void test02()
//	{
//		simpleParsingTest("rst2.txt");
//	}
//	
//	public void test03()
//	{
//		simpleParsingTest("rst3.txt");
//	}
//	
//	public void test04()
//	{
//		simpleParsingTest("rst4.txt");
//	}
	
	
	
		/**
	 * Used for simple checking, that all is ok, and no tests causes Exceptions or crashes
	 * @param fName
	 */
	private void simpleParsingTest(String fName)
	{
		layer = new WikitextPartitionLayer();
		linkPrototypePartition = new BasePartition(layer.getDoc(), 0, 0);
		linkPrototypePartition.setStyleMask(FontStyle.UNDERLINED);
		DefaultWikitextLoader loader = new DefaultWikitextLoader(layer, null, linkPrototypePartition);
		InputStream stream = ParsingTests2.class.getResourceAsStream(fName);
		String src = readString(stream);
		
		final ISimpleRichTextModel model = loader.parse(src);
		Document document = new Document(model.getText());
		layer.connectToDocument(new DocumentWrapper(document));
		layer.getStorage().setPartitions(model.getPartitions());
		WikitextSerializer serializer = new WikitextSerializer(layer,  new DocumentWrapper(document), new ModelBasedLineInformationProvider(model));
		String res = serializer.serializeAllToStr();
		
		src=src.replaceAll("\r\n", "\n");
		res=res.replaceAll("\r\n", "\n");
		if (res.endsWith("\n")){
			res=res.substring(0,res.length()-1);
		}
		if (res.endsWith("\n")){
			res=res.substring(0,res.length()-1);
		}
		assertEquals(src, res);
		
	}
	
	private String getSerialized(String src)
	{
		layer = new WikitextPartitionLayer();
		linkPrototypePartition = new BasePartition(layer.getDoc(), 0, 0);
		linkPrototypePartition.setStyleMask(FontStyle.UNDERLINED);
		DefaultWikitextLoader loader = new DefaultWikitextLoader(layer, null, linkPrototypePartition);
		final ISimpleRichTextModel model = loader.parse(src);
		Document document = new Document(model.getText());
		layer.connectToDocument(new DocumentWrapper(document));
		layer.getStorage().setPartitions(model.getPartitions());
		WikitextSerializer serializer = new WikitextSerializer(layer, new DocumentWrapper(document), new ModelBasedLineInformationProvider(model));
		final String res = serializer.serializeAllToStr();
		return res;
	}
	
	private void comparisonTest(String pageName) throws TracException
	{		
		if (repository == null)
			try
			{
				connect("http://www.onpositive.com:8000/semantic", "kor", "svetlana", Proxy.NO_PROXY, Version.XML_RPC);				
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		ITracWikiClient wc = (ITracWikiClient) repository;
		final String src = wc.getWikiPageContent(pageName,null);
		wc.putWikipage(TEST,src,new HashMap<String, Object>(),null);
		String s1 = wc.getWikiPageHtml(TEST, null);
		final String serialized = getSerialized(src);
		wc.putWikipage(TEST,serialized,new HashMap<String, Object>(),null);
		String s2 = wc.getWikiPageHtml(TEST, null);
		s1 = TestingTool.normalizeStr(s1);
		s2 = TestingTool.normalizeStr(s2);
		if (!s1.equals(s2))
		{			
			System.out.println("================= FAIL!!! ========================");
			System.out.println(src);
			System.out.println("==================================================");
			System.out.println(serialized);
			System.out.println("================= ENDFAIL ========================");
			//assertEquals(src,serialized);
			try{
				assertEquals(s1,s2);
			}catch (Exception e) {
				assertEquals(src,serialized);	
			}			
			throw new AssertionFailedException("Comparison failed for page " + pageName);
		}
	}

		/*private void formattingTest(String fName, String expected)
	{
		if (true)
		{
			return;
		}
		InputStream stream = FormattingTests.class.getResourceAsStream(fName);
		String readString = Utils.readString(stream);
		PHPCodeFormatter formatter = new PHPCodeFormatter();
		String format = formatter.format(readString, false, null, null, "\n"); //$NON-NLS-1$
		assertTrue(Utils.compareByTokens(readString, format));
		assertEquals(Utils.changeDelimeters(expected), Utils.changeDelimeters(format));
	}
	
	private void formattingTestHTML(String fName, String expected)
	{
		if (true)
		{
			return;
		}
		InputStream stream = FormattingTests.class.getResourceAsStream(fName);
		String readString = Utils.readString(stream);
		HTMLCodeFormatter formatter = new HTMLCodeFormatter();
		String format = formatter.format(readString, false, null, null, "\n"); //$NON-NLS-1$
		assertTrue(Utils.compareByTokens(readString, format));
		assertEquals(Utils.changeDelimeters(expected), Utils.changeDelimeters(format));
	}
	
	private void formattingTest(String fName, String expected,Map options)
	{
		if (true)
		{
			return;
		}
		InputStream stream = FormattingTests.class.getResourceAsStream(fName);
		String readString = Utils.readString(stream);
		PHPCodeFormatter formatter = new PHPCodeFormatter();
		String format = formatter.format(readString,false, options, null, separator);		
		assertTrue(Utils.compareByTokens(readString, format));
		assertEquals(Utils.changeDelimeters(expected), Utils.changeDelimeters(format));
	}*/

	private String getContent(String fileName)
	{
		InputStream stream = ParsingTests2.class.getResourceAsStream(fileName);
		return readString(stream);
		
	}

	private String readString(InputStream stream)
	{
		ByteArrayOutputStream buf = new ByteArrayOutputStream();		
		try
		{
			while (stream.available() >= 0)
			{
				int read = stream.read();
				
				if (read == -1)
				{
					break;
				}
				
				buf.write(read);
			}
			
			return new String(buf.toByteArray(), "UTF-8"); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}	

	}
	
/*	public void test0() {
		try {
			if (repository == null)
				connect("http://www.onpositive.com:8000/semantic", "kor", "svetlana", Proxy.NO_PROXY, Version.XML_RPC);
			repository.updateAttributes(new NullProgressMonitor(), true);
			TracComponent[] components = repository.getComponents();
			ITracWikiClient wc = (ITracWikiClient) repository;
			String[] allWikiPageNames = wc.getAllWikiPageNames(new NullProgressMonitor());
			for (String s : allWikiPageNames) {
				System.out.println("Page:" + s);
			}
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}*/

	protected ITracClient connect(String url, String username, String password, final Proxy proxy, Version version)
			throws Exception {
		this.repositoryUrl = url;
		this.username = username;
		this.password = password;

		WebLocation location = new WebLocation(url, username, password, new IProxyProvider() {
			public Proxy getProxyForHost(String host, String proxyType) {
				return proxy;
			}
		});
		this.repository = TracClientFactory.createClient(location, version);
		return this.repository;
	}
	
	

}
