/*******************************************************************************
 * Copyright (c) 2007, 2008 OnPositive Technologies (http://www.onpositive.com/) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     OnPositive Technologies (http://www.onpositive.com/) - initial API and implementation
 *******************************************************************************/
/**
* @author 32kda
* made in USSR
*/
package com.onpositive.richtexteditor.io.html_scaner;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.onpositive.richtexteditor.io.LexEvent;
import com.onpositive.richtexteditor.io.html.HTMLTokenProvider;
import com.onpositive.richtexteditor.model.Logger;

/**
 * HTML scanner
 * @author 32kda
 * 
 */


public class Scanner 
{
				
		char[] nonTagNameSymbols = new char[]{'"', '\'', '<', '>', '/', '\\', '='};

		protected int uk; 
		protected char[] t;
		protected HashMap<String, Integer> tagKeywords = new HashMap<String,Integer>();
		protected HashMap<String, Integer> attrKeywords = new HashMap<String,Integer>();
		protected ArrayList<ILexListener> listeners = new ArrayList<ILexListener>();
		
		protected static Scanner scaner;
		protected boolean textScanned;
		protected boolean clearEnters = true;
		
		/**
		 * Checks, can this symbol be an ident beginning or not
		 * @param c symbol
		 * @return true/false
		 */
		protected boolean checkForIdentBeginning(char c)
		{
			if ((c>='a') && (c<='z') || 
					   (c>='A') && (c<='Z')
					   || (c == '_')) 
				return true;
			return false;
		}
		
		/**
		 * adds new LexListener
		 * @param lexListener LexListener to add
		 */
		public void addLexListener(ILexListener lexListener)
		{
			listeners.add(lexListener);
		}

		
		/**
		 * Removes LexListener
		 * @param lexListener LexListener to remove
		 */
		public void removeLexListener(ILexListener lexListener)
		{
			listeners.remove(lexListener);
		}
		
		
		/**
		 * Default constructor
		 */
		public Scanner()
		{
			fillKeywords();
		}
		
		protected void fillKeywords()
		{
		
			
		}
		
		
		protected boolean checkForTagNameSymbol(char c)
		{
			if (!isIn(c, nonTagNameSymbols) && !Character.isWhitespace(c)) return true;
			return false;
		}
					
		/**
		 * Checks, can this symbol be an ident symbol(not beginning) or not
		 * @param c symbol
		 * @return true/false
		 */
		protected boolean checkForIndentSymbol(char c)
		{
			if (checkForIdentBeginning(c)) return true; //Может быть или начальный символ, или цифра
			if ((t[uk]<='9') && (t[uk]>='0')) return true;
			return false;
		}

		void PutUK (int i) {uk = i;}
		int GetUK () {return uk;};

		void LogError(FileWriter LogFile, String msg) throws IOException
		  {
			if (LogFile != null) LogFile.write("Ошибка! " + msg);
		  }

		
		/**
		 * Main func
		 * @throws Exception
		 */
		void scaner()
		{
			uk = 0;
			while (true)
			{
			  LexEvent lxm = new LexEvent();
			  //pass();
			  if (uk >= t.length) 
				  return ;
			  if (t[uk] == '<')
			    {
				  ++uk;
				  scanForTag();
			    }
			  else
			  	{
				  while (uk < t.length && t[uk] != '<')
					  lxm.text = lxm.text + t[uk++];
				  if (clearEnters) lxm.text = clearFromEnters(lxm.text);
				  handleLexEvent(lxm);
			  	}	
			}
		}
		
		protected void scanForTag()
		{
			pass();
			
			boolean open = true;
			if (t[uk] == '/')
			{				
				open = false;
				uk++;
			}
			if (t[uk] == '!' && uk < t.length - 2 && t[uk+1] == '-' && t[uk+2] == '-') //Comment 
			{
				
				while (uk < t.length && !(t[uk] == '>' && t[uk-1] == '-' && t[uk-2] == '-')) uk++;
				uk++;
				return;					
			}
			
			
			String ident = scanForTagName().toLowerCase();
			
			
			
			int tagType = getTagKeywordType(ident);
			handleLexEvent(new TagLexEvent(ident,tagType,open));
			uk++;
			pass();			
			while (uk < t.length && t[uk] != '>')
			{				
				scanForAttribute();
				pass();
			}			
			if (t[uk] == '>') uk++;
			handleLexEvent(new TagEndEvent(tagType,open));
		}

		protected void scanForAttribute()
		{
			if (!checkForIdentBeginning(t[uk]))
			{
				neutrilzeUpTo('>');
				return;
			}
			StringBuilder str1=new StringBuilder(String.valueOf(t[uk]));
			uk++;
			while(checkForTagNameSymbol(t[uk]))
			{
				str1.append(t[uk++]);
			}
			pass();
			String str=str1.toString();
			if (t[uk] != '=') 
			{
				neutrilzeUpTo('>');
				return;
			}
			uk++;
			pass();
			String value = getDequotedValueString();
			handleLexEvent(new AttrValueLexEvent(value,getAttrKeywordType(str.toLowerCase())));
		}

		protected String scanForTagName()
		{
			if (!checkForIdentBeginning(t[uk]))
			{
				neutrilzeUpTo('>');
				return "";
			}				
			StringBuilder str = new StringBuilder(String.valueOf(t[uk]));
			uk++;
			while(checkForTagNameSymbol(t[uk]))
				str.append(t[uk++]);
			uk--;
			return str.toString();
		}
		
		protected String getDequotedValueString()
		{
			char quote = '\0'; //Means no quote
			StringBuilder str = new StringBuilder();
			if (t[uk] == '"' || t[uk] == '\'')
			{
				quote = t[uk];
				uk++;
			}
			if (quote == '\0')
				while (uk < t.length && t[uk] != '>' && !Character.isWhitespace(t[uk]))
					str.append(t[uk++]);
			else
				while (uk < t.length && t[uk] != quote)
					str.append(t[uk++]);
			if (t[uk] == '"' || t[uk] == '\'') uk++;
			return str.toString().trim();
		}
		
		protected void neutrilzeUpTo(char symbol)
		{
			while (uk < t.length && t[uk] != symbol)
			{
				if ((symbol != '"' && t[uk] == '"') || (symbol != '\'' && t[uk] == '\'')) 
					passStr(t[uk]);				
				uk++;
			}
		}
		
		protected void passStr(char c)
		{
			while (uk < t.length && t[uk] != c)
			{
				if (t[uk] == '\\' && t[uk+1] == c) uk++;
				uk++;
			}
			//uk++; //Пропуск кавычки
		}

		protected void neutrilzeUpTo(char[] symbols)
		{			
			do
			{
				if ((!isIn('"',symbols) && t[uk] == '"') || (!isIn('\'',symbols) && t[uk] == '\'')) 
					passStr(t[uk]);
				if (isIn(t[uk],symbols)) return;
				uk++;
			}
			while (uk < t.length);
		}
		
		boolean isIn(char symbol, char[] array)
		{
			for (int i = 0; i < array.length; i++)
				if (array[i] == symbol) return true;
			return false;
		}
		
		protected int getTagKeywordType(String keyword)
		{
			Integer num = HTMLTokenProvider.getInstance().getKeywordConstant(keyword);
			if (num >= HTMLTokenProvider.MIN_ATTR_TYPE) return HTMLTokenProvider.TYPE_UNKNOWN;
			return num.intValue();
		}

		protected int getAttrKeywordType(String keyword)
		{
			Integer num = HTMLTokenProvider.getInstance().getKeywordConstant(keyword);
			if (num < HTMLTokenProvider.MIN_ATTR_TYPE) return HTMLTokenProvider.TYPE_UNKNOWN;
			return num.intValue();
		}
		
		/**
		 * Passes all unuseful symbols		 
		 */
		protected void pass()
		{		  
		  while (uk < t.length && ((t[uk] == ' ') || (t[uk] == '\n') || (t[uk] == '\r') || (t[uk] == '\t')))
		      uk++;
    	}
		
		/**
		 * Gets file's string representation
		 * @param input file
		 */
		
		public void process(File input)
		{
			long k;
			FileReader fr = null;
			k = input.length();
			t = new char[(int) k];		  
			try 
			{
				fr = new FileReader(input);
			} 
			catch (FileNotFoundException e) 
			{
	          Logger.log("Can't read such a file - " + input.getName() + "!");
	          throw new RuntimeException(e);
			}
			try 
			{
				process(fr,k);
			} 
			catch (IOException e) 
			{				
				Logger.log("Error reading file - " + input.getName() + "!");
		        return;		
		    }			
		}
		
		/**
		 * Method for processing Reader provided contents
		 * @param reader Reader
		 * @param charCount char count to read
		 * @throws IOException in case of i/o error
		 */
		public void process(Reader reader, long charCount) throws IOException
		{
			t = new char[(int) charCount];			
			int read = reader.read(t,0,(int) charCount);
			while (read<charCount&&read!=-1){
				read+=reader.read(t,read,(int) (charCount-read));
			}
			scaner();
			handleLexEvent(new EOFEvent());
			textScanned = true;
		}
		
		/**
		 * Gets file's string representation
		 * @param fileName File Name
		 * @throws IOException in case of file reading errors
		 */
		protected void process(String fileName) throws IOException
		{
			File input = new File(fileName);
			process(input);
		}
		
		protected void handleLexEvent(LexEvent event)
		{
			for (Iterator<ILexListener> iterator = listeners.iterator(); iterator.hasNext();)
			{
				ILexListener listener = (ILexListener) iterator.next();
				listener.handleLexEvent(event);
			}
		}
		
		protected String clearFromEnters(String l)
		{
			if (l.trim().length() == 0) return "";
			l = l.replace('\r',' ');
			l = l.replace('\n',' ');			
			return l;
		}

		
		boolean isScanned()
		{
			return textScanned;
		}

		
		/**
		 * @return true if parser clears text fragments from newline symbols
		 */
		public boolean isClearEnters()
		{
			return clearEnters;
		}

		
		/**
		 * @param clearEnters should parser clear text fragments from newline symbols?
		 */
		public void setClearEnters(boolean clearEnters)
		{
			this.clearEnters = clearEnters;
		}

}
