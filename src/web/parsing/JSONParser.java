package web.parsing;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class JSONParser
{
	public JsonObject parseRawJson(String rawJson)
	{
		JsonParser parser = new JsonParser();

		return (JsonObject) parser.parse(rawJson);
	}

}
