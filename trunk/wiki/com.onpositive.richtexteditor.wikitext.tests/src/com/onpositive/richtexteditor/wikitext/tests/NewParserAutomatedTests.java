package com.onpositive.richtexteditor.wikitext.tests;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import com.onpositive.richtexteditor.wikitext.io.ParserTestWikitextLoader;
import com.onpositive.richtexteditor.wikitext.io.WikitextSerializer;
import com.onpositive.richtexteditor.wikitext.ui.WikitextPartitionLayer;

public class NewParserAutomatedTests extends TestCase
{
	private static final String TEST = "test";
	BasePartition linkPrototypePartition = null;
	BasePartitionLayer layer = null;
	private ITracClient repository;
	private String repositoryUrl;
	private String username;
	private String password;
	private static final String LOG_FILE_NAME = "log.txt";
	private PrintWriter logWriter;
	private boolean FAIL_ON_FIRST = true;

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
		} catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}

	}

	/*
	 * public void test0() { try { final long currentTime =
	 * System.currentTimeMillis(); File f = new File(currentTime+ ".log");
	 * PrintWriter pw = new PrintWriter(f);
	 * 
	 * if (repository == null)
	 * connect("http://www.onpositive.com:8000/semantic", "kor", "svetlana",
	 * Proxy.NO_PROXY, Version.XML_RPC); repository.updateAttributes(new
	 * NullProgressMonitor(), true); TracComponent[] components =
	 * repository.getComponents(); ITracWikiClient wc = (ITracWikiClient)
	 * repository; String[] allWikiPageNames = wc.getAllWikiPageNames(new
	 * NullProgressMonitor()); int passed = 0, failed = 0; for (int i = 0; i <
	 * allWikiPageNames.length; i++) { try { doTest(allWikiPageNames[i]);
	 * passed++; } catch (Throwable e) { failed++;
	 * System.out.println("Current: " + allWikiPageNames[i]);
	 * pw.println(allWikiPageNames[i] + " " + e); }
	 * System.out.println("Current: " + allWikiPageNames[i]);
	 * System.out.println("Passed: " + passed + " Failed: " + failed);
	 * System.out.println("Total: " + allWikiPageNames.length); }
	 * System.out.println(
	 * "===================================******************======================================="
	 * ); System.out.println("Passed: " + passed + " Failed: " + failed);
	 * System.out.println("Total: " + allWikiPageNames.length); pw.close();
	 * calculateRegression(currentTime); } catch (Exception e) {
	 * e.printStackTrace();
	 * 
	 * } }
	 */
	/*
	 * public void test0() throws Exception { File dir = new
	 * File(System.getProperty("user.dir") + "\\testdocs"); File[] list =
	 * dir.listFiles(); for (int i = 0; i < list.length; i++) { if
	 * (list[i].getName().startsWith(".")) continue; FileReader reader = new
	 * FileReader(list[i]); String text = readString(reader); reader.close();
	 * text = text.substring(2); PrintWriter pw = new PrintWriter(list[i]);
	 * pw.print(text); pw.close(); } }
	 */

	/*public void test0()
	{
		PrintWriter pw = null;
		int failed = 0;
		try
		{
			final long currentTime = System.currentTimeMillis();
			File f = new File(currentTime + ".log");
			pw = new PrintWriter(f);
			File dir = new File(System.getProperty("user.dir") + "\\testdocs");
			FileReader reader = new FileReader(dir.getAbsolutePath() + File.separator + "TracLinks");
			try
			{
				parseAndComapreSrc(readString(reader));
			} catch (RuntimeException e)
			{
				failed++;
				System.out.println("Current: TracLinks ");
				if (FAIL_ON_FIRST)
					throw new RuntimeException("Parsing test failed on " + "TracLinks");
			}

		} catch (IOException e)
		{
			e.printStackTrace();

		} finally
		{
			pw.close();
		}
	}*/

	public void test1()
	{
		PrintWriter pw = null;
		int failed = 0;
		try
		{
			final long currentTime = System.currentTimeMillis();
			File f = new File(currentTime + ".log");
			pw = new PrintWriter(f);
			File dir = new File(System.getProperty("user.dir") + "\\testdocs");
			File[] list = dir.listFiles();
			for (int i = 0; i < list.length; i++)
			{
				File currentFile = list[i];
				if (currentFile.getName().startsWith("."))
					continue;
				FileReader reader = new FileReader(currentFile);
				try
				{
					parseAndComapreSrc(readString(reader));
				} catch (RuntimeException e)
				{
					failed++;
					System.out.println("Current: " + currentFile.getName());
					if (FAIL_ON_FIRST)
						throw new RuntimeException("Parsing test failed on " + currentFile.getName());
				}
			}

		} catch (IOException e)
		{
			e.printStackTrace();

		} finally
		{
			pw.close();
		}
	}

	private String readString(FileReader reader) throws IOException
	{
		StringBuilder bld = new StringBuilder();
		while (true)
		{
			int read = reader.read();
			if (read != -1)
			{
				bld.append((char) read);
			} else
			{
				break;
			}
		}
		return bld.toString();
	}

	private void parseAndComapreSrc(String src)
	{
		layer = new WikitextPartitionLayer();
		linkPrototypePartition = new BasePartition(layer.getDoc(), 0, 0);
		linkPrototypePartition.setStyleMask(FontStyle.UNDERLINED);
		ParserTestWikitextLoader loader = new ParserTestWikitextLoader(layer, null, linkPrototypePartition);
		ISimpleRichTextModel model1 = loader.parse(src);
		DefaultWikitextLoader loader2 = new DefaultWikitextLoader(layer, null, linkPrototypePartition);
		ISimpleRichTextModel model2 = loader2.parse(src);
		model1.equals(model2);
	}

	private void log(String string)
	{
		if (logWriter == null)
			try
			{
				logWriter = new PrintWriter(LOG_FILE_NAME);
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		logWriter.println(string);
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

	protected ITracClient connect(String url, String username, String password, final Proxy proxy, Version version) throws Exception
	{
		this.repositoryUrl = url;
		this.username = username;
		this.password = password;

		WebLocation location = new WebLocation(url, username, password, new IProxyProvider()
		{
			public Proxy getProxyForHost(String host, String proxyType)
			{
				return proxy;
			}
		});
		this.repository = TracClientFactory.createClient(location, version);
		return this.repository;
	}

}
