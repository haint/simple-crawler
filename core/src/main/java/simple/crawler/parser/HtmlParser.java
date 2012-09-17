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
package simple.crawler.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class HtmlParser
{
	/** . */
	private final DOMParser nonWellFormParser;
	
	/** . */
	private final DocumentBuilder wellFormParser;
	
	public HtmlParser() throws Exception 
	{
		//
		nonWellFormParser = new DOMParser();
		nonWellFormParser.setFeature("http://cyberneko.org/html/features/augmentations", true);
		nonWellFormParser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
		
		//
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		wellFormParser = factory.newDocumentBuilder();
		wellFormParser.setEntityResolver(new EntityResolver()
		{
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
			{
				return new InputSource(new StringReader(""));
			}
		});
		
		wellFormParser.setErrorHandler(new ErrorHandler()
		{
			public void warning(SAXParseException exception) throws SAXException
			{
			}
			public void fatalError(SAXParseException exception) throws SAXException
			{
			}
			public void error(SAXParseException exception) throws SAXException
			{
			}
		});
	}
	
	public Document parseNonWellForm(String html) throws Exception
	{
		return parseNonWellForm(new StringReader(html));
	}
	
	public Document parseNonWellForm(Reader reader) throws Exception
	{
		nonWellFormParser.parse(new InputSource(reader));
		return nonWellFormParser.getDocument();
	}
	
	public Document parseNonWellForm(InputStream in) throws Exception
	{
		nonWellFormParser.parse(new InputSource(in));
		return nonWellFormParser.getDocument();
	}
	
	public Document parseWellForm(String html) throws Exception
	{
		return parseWellForm(new StringReader(html));
	}
	
	public Document parseWellForm(Reader reader) throws Exception
	{
		return wellFormParser.parse(new InputSource(reader));
	}
	
	public Document parseWellForm(InputStream in) throws Exception
	{
		return wellFormParser.parse(in);
	}
}
