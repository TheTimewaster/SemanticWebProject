package web.resources.impl;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
	private static final String	URL	= "https://maps.googleapis.com/maps/api/place/textsearch/json?query=bioladen+in+leipzig&key=AIzaSyAV201q9SNmr2WXEzT9HrSVG_YdMEjQn-M";

	private JsonObject			_gPlacesObj;

	@Override
	public void startWorkflow() throws Exception
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		executeRequest(URL, out);

		String response = new String(out.toByteArray(), "UTF-8");

		_gPlacesObj = (JsonObject) ResourceParser.parseResource(response, WebResourceType.JSON_OBJ);

		_data = new ResultMap();
		JsonArray array = _gPlacesObj.getAsJsonArray("results");

		int i = 0;

		for ( Object placeEntry : array )
		{
			JsonObject placeObject = (JsonObject) placeEntry;
			String name = placeObject.get("name").getAsString();
			String adress = placeObject.get("formatted_address").getAsString();

			String lat = placeObject.getAsJsonObject("geometry").getAsJsonObject("location").get("lat").getAsString();
			String lng = placeObject.getAsJsonObject("geometry").getAsJsonObject("location").get("lng").getAsString();

			List<Object> valueList = new ArrayList<Object>();
			valueList.add(name);
			valueList.add(adress);
			valueList.add(lat);
			valueList.add(lng);

			String district = searchForDistrict(Double.valueOf(lat), Double.valueOf(lng));

			if ( district != null )
			{
				valueList.add(district);
			}

			_data.put(Integer.toString(i), valueList);
			i++;

			System.out.println(valueList);
		}
	}

	private String searchForDistrict(double lat, double lng)
	{
		SingleWebResource districtSearchResource = new GoogleGeocodingResource(lat, lng);

		try
		{
			districtSearchResource.startWorkflow();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}

		return districtSearchResource.getData().get("district").get(0).toString();
	}

}
