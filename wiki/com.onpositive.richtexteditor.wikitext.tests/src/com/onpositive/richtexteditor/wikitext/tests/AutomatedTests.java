package com.onpositive.richtexteditor.wikitext.tests;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.mylyn.commons.net.IProxyProvider;
import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.internal.trac.core.TracClientFactory;
import org.eclipse.mylyn.internal.trac.core.client.ITracClient;
import org.eclipse.mylyn.internal.trac.core.client.ITracWikiClient;
import org.eclipse.mylyn.internal.trac.core.client.TracException;
import org.eclipse.mylyn.internal.trac.core.client.ITracClient.Version;
import org.eclipse.mylyn.internal.trac.core.model.TracComponent;

import com.onpositive.richtext.model.BasePartition;
import com.onpositive.richtext.model.BasePartitionLayer;
import com.onpositive.richtext.model.FontStyle;
import com.onpositive.richtexteditor.io.ModelBasedLineInformationProvider;
import com.onpositive.richtexteditor.model.ISimpleRichTextModel;
import com.onpositive.richtexteditor.util.DocumentWrapper;
import com.onpositive.richtexteditor.wikitext.io.DefaultWikitextLoader;
import com.onpositive.richtexteditor.wikitext.io.WikitextSerializer;
import com.onpositive.richtexteditor.wikitext.ui.WikitextPartitionLayer;


public class AutomatedTests extends TestCase
{
	private static final String TEST = "test";
	BasePartition linkPrototypePartition = null;
	BasePartitionLayer layer = null;
	private ITracClient repository;
	private String repositoryUrl;
	private String username;
	private String password;

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
		WikitextSerializer serializer = new WikitextSerializer(layer,new DocumentWrapper(document), new ModelBasedLineInformationProvider(model));
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
		String src = wc.getWikiPageContent(pageName,null);
		src = src.replaceAll("\\r\\n", "\n");
		src = src.replaceAll(" \\n", "\n");
		wc.putWikipage(TEST,src,new HashMap<String, Object>(),null);
		String s1 = wc.getWikiPageHtml(TEST, null);
		String serialized = getSerialized(src);
		serialized = serialized.replaceAll("\\r\\n", "\n");
		serialized = serialized.replaceAll(" \\n", "\n");
		wc.putWikipage(TEST,serialized,new HashMap<String, Object>(),null);
		String s2 = wc.getWikiPageHtml(TEST, null);
		s1 = TestingTool.normalizeStr(s1);
		s2 = TestingTool.normalizeStr(s2);
		if (!s1.equals(s2))
		{			
			/*System.out.println("================= FAIL!!! ========================");
			System.out.println(src);
			System.out.println("==================================================");
			System.out.println(serialized);
			System.out.println("================= ENDFAIL ========================");*/
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
	
	public void test0() {
		try {
			final long currentTime = System.currentTimeMillis();
			File f = new File(currentTime+ ".log");
			PrintWriter pw = new PrintWriter(f);
			if (repository == null)
				connect("http://www.onpositive.com:8000/semantic", "kor", "svetlana", Proxy.NO_PROXY, Version.XML_RPC);
			repository.updateAttributes(new NullProgressMonitor(), true);
			TracComponent[] components = repository.getComponents();
			ITracWikiClient wc = (ITracWikiClient) repository;
			String[] allWikiPageNames = wc.getAllWikiPageNames(new NullProgressMonitor());
			int passed = 0, failed = 0;
			for (int i = 0; i < allWikiPageNames.length; i++)
			{
				try 
				{
					comparisonTest(allWikiPageNames[i]);
					passed++;
				}
				catch (Throwable e) {
					failed++;
					System.out.println("Current: " + allWikiPageNames[i]);
					pw.println(allWikiPageNames[i]);	
				}
				/*System.out.println("Current: " + allWikiPageNames[i]);
				System.out.println("Passed: " + passed + " Failed: " + failed);
				System.out.println("Total: " + allWikiPageNames.length);*/
			}
			System.out.println("===================================******************=======================================");
			System.out.println("Passed: " + passed + " Failed: " + failed);
			System.out.println("Total: " + allWikiPageNames.length);			
			pw.close();			
			calculateRegression(currentTime);
		} catch (Exception e) {			
			e.printStackTrace();
			
		}
	}

	private void calculateRegression(long currentTime)
	{
		File f = new File(currentTime+ ".log");
		File root = f.getParentFile();
		final File[] listFiles = new File(".").listFiles();
		long nearest = -1;
		long min_distance = Long.MAX_VALUE;
		for (int i = 0; i < listFiles.length; i++)
		{
			String name = listFiles[i].getName();
			int idx = name.indexOf('.');
			if (idx < 1)
				continue;
			String fName = name.substring(0,idx);
			try
			{
				long num = Long.parseLong(fName);
				final long diff = currentTime - num;
				if (diff > 0 && diff < min_distance)
				{
					min_distance = diff;
					nearest = num;
				}
			}
			catch (Exception e) {
				continue;
			}
		}
		compare(currentTime, nearest);
		
	}

	private void compare(long currentTime, long nearest)
	{
		File f = new File("regress" + currentTime+ ".log");		
		try
		{	
			PrintWriter pw = new PrintWriter(f);
			ArrayList <String> curList = getLinesList(currentTime + ".log");
			ArrayList <String> prevList = getLinesList(nearest + ".log");;
			for (Iterator iterator = curList.iterator(); iterator.hasNext();)
			{
				String string = (String) iterator.next();
				if (prevList.indexOf(string) == -1)
					pw.println(string);
			}
			pw.close();
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private ArrayList<String> getLinesList(String fileName) throws IOException
	{
		ArrayList<String> res = new ArrayList<String>();
		FileReader fr1 = new FileReader(fileName);
		BufferedReader br1 = new BufferedReader(fr1);
		while (br1.ready())
			res.add(br1.readLine());
		fr1.close();
		return res;
	}

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
