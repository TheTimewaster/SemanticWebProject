package proj.web.parsing;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class JSONParser
{
	public JsonObject parseRawJson(String rawJson)
	{
		JsonParser parser = new JsonParser();

		if ( parser.parse(rawJson).isJsonNull() )
		{
			System.out.println("here");
		}

		return (JsonObject) parser.parse(rawJson);
	}

}
