package web.resources.impl;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import data.ResultMap;
import web.parsing.ResourceParser;
import web.resources.SingleWebResource;


/**
 * This parser parses the JSON from the Google Places API. Here we get
 * information about places. Then we try to map the address.
 * 
 * @author Tu
 *
 */
public class GooglePlacesResource extends SingleWebResource
{
	private static final String URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=bioladen+in+leipzig&key=AIzaSyAV201q9SNmr2WXEzT9HrSVG_YdMEjQn-M";

	private JSONObject _gPlacesObj;

	@Override
	public void startWorkflow() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executeRequest(URL, out);

		String response = new String(out.toByteArray(), "UTF-8");

		_gPlacesObj = (JSONObject) ResourceParser.parseResource(response, WebResourceType.JSON_OBJ);

		_data = new ResultMap();
		JSONArray array = (JSONArray) _gPlacesObj.get("results");

		int i = 0;

		for ( Object placeEntry : array )
		{
			JSONObject placeObject = (JSONObject) placeEntry;
			String name = placeObject.get("name").toString();
			String adress = placeObject.get("formatted_address").toString();

			List<Object> valueList = new ArrayList<Object>();
			valueList.add(name);
			valueList.add(adress);
			_data.put(Integer.toString(i), valueList);
			i++;
		}
	}

}
