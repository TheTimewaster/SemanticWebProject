package web.parsing;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.nodes.Element;

import web.resources.SingleWebResource.WebResourceType;

public class ResourceParser
{
	public static Object parseResource(String rawResource, WebResourceType type)
	{
		switch (type)
		{
			case HTML_DOC:
				HtmlParser htmlParser = new HtmlParser();
				Element _document = htmlParser.parseHtmlString(rawResource);
				return _document;
			case JSON_OBJ:
				JSONParser jsonParser =new JSONParser();
				try
				{
					Object _object = jsonParser.parse(rawResource);
					return _object;
				} 
				catch (ParseException e)
				{
					return null;
				}
			default:
				return null;
			
		}
	}
}
