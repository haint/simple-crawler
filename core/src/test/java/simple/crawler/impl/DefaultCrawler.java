/*
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package simple.crawler.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import simple.crawler.HttpClientFactory;
import simple.crawler.HttpClientUtil;
import simple.crawler.parser.HtmlParser;
import simple.crawler.parser.NodeCollectedVisitor;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class DefaultCrawler
{
	private static int counter = 0;
	
	private static final Set<String> keyHolder = new HashSet<String>();
	
	private static void getChildFile(final List<File> holder, File parent)
	{
		if(parent.isFile())
		{
			holder.add(parent);
		}
		else if(parent.isDirectory())
		{
			holder.addAll(Arrays.asList(parent.listFiles(new FileFilter()
			{
				public boolean accept(File file)
				{
					if(file.isDirectory())
					{
						getChildFile(holder, file);
						return false;
					}
					else if(file.getName().startsWith("data."))
					{
						int index = Integer.parseInt(file.getName().substring("data.".length()));
						if(index > counter) 
						{
							counter = index + 1;
						}
						return true;
					}
					
					return false;
				}
			})));
		}
	}
	
	public static void main(String[] args) throws Exception
   {
		//
	   String initialURL = "http://www.vn-zoom.com/f171/";
	   
		//
		DefaultHttpClient client = HttpClientFactory.getInstance();
		HtmlParser parser = new HtmlParser();
		
		//
		Map<String, String> dataHolder = new HashMap<String, String>();

		//
		File root = new File("src/test/resources/datum/vnzoom");
		List<File> fileHolder = new ArrayList<File>();
		getChildFile(fileHolder, root);
		System.out.println(counter);
		for(File file : fileHolder)
		{
			ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
			try
			{
				Map<String, String> map = (Map<String, String>)is.readObject();
				keyHolder.addAll(map.keySet());
				is.close();
			}
			catch(Exception e)
			{
				System.out.println("Remove interrupted file " + file);
				file.delete();
			}
		}
		
		//
		if(keyHolder.size() == 0)
		{
			fetch(dataHolder, Arrays.asList(initialURL), client, parser);
		}
		else
		{
			for(File file : fileHolder)
			{
				ObjectInputStream is = new ObjectInputStream(new FileInputStream(file));
				Map<String, String> map = (Map<String, String>)is.readObject();
				is.close();
				
				for(String html : map.values())
				{
					collectLink(dataHolder, html, client, parser);
				}
			}
		}
		
		//
		if(dataHolder.size() > 0) 
		{
			putToDisk(dataHolder);
		}
   }
	
	private static void putToDisk(Map<String, String> holder) throws IOException
	{
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("src/test/resources/datum/vnzoom/data." + (counter++)));
		os.writeObject(holder);
		os.close();
		holder.clear();
		System.out.println("persisted 100 pages");
	}
	
	private static void collectLink(Map<String, String> holder, String html, DefaultHttpClient client, HtmlParser parser) throws Exception
	{
		final LinkedList<String> list = new LinkedList<String>();
		NodeCollectedVisitor visitor = new NodeCollectedVisitor("a", "href")
		{
			@Override
			public void collect(Node node)
			{
				String href = ((Element) node).getAttribute("href");
				if(href.startsWith("http://www.vn-zoom.com/f171/") && href.endsWith(".html"))
				{
					list.add(href);
				}
			}
		};
		
		Document doc = parser.parseNonWellForm(html);
		visitor.traverse(doc);
		
		fetch(holder, list, client, parser);
	}
	
	private static void fetch(Map<String, String> holder, Collection<String> urls, DefaultHttpClient client, HtmlParser parser) throws Exception
	{
		//
		final LinkedList<String> list = new LinkedList<String>();
		NodeCollectedVisitor visitor = new NodeCollectedVisitor("a", "href")
		{
			@Override
			public void collect(Node node)
			{
				String href = ((Element) node).getAttribute("href");
				if(href.startsWith("http://www.vn-zoom.com/f171/") && href.endsWith(".html"))
				{
					list.add(href);
				}
			}
		};
		
		//
		for(String url : urls)
		{
			if(keyHolder.contains(url))
			{
				continue;
			}
			String html = HttpClientUtil.fetch(client, url);
			Document doc = parser.parseNonWellForm(html);
			visitor.traverse(doc);
			
			//
			holder.put(url, html);
			keyHolder.add(url);
			System.out.println("putting page: " + url);
			
			//
			if(holder.size() % 100 == 0)
			{
				putToDisk(holder);
			}
			
			//
			fetch(holder, list, client, parser);
		}
	}
}
