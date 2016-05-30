package web.resources.impl;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import data.ResultMap;
import web.parsing.ResourceParser;
import web.resources.SingleWebResource;


public class GoogleGeocodingResource extends SingleWebResource
{
	private double				_lat, _lng;

	private final static String	URL_TEMPLATE	= "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=AIzaSyAV201q9SNmr2WXEzT9HrSVG_YdMEjQn-M";

	public GoogleGeocodingResource(double lat, double lng)
	{
		_lat = lat;
		_lng = lng;
	}

	@Override
	public void startWorkflow() throws Exception
	{
		String requestUrl = String.format(URL_TEMPLATE, String.valueOf(_lat), String.valueOf(_lng));
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		executeRequest(requestUrl, out);

		String rawJson = new String(out.toByteArray(), "UTF-8");
		JsonObject resultObject = (JsonObject) ResourceParser.parseResource(rawJson, WebResourceType.JSON_OBJ);

		JsonArray componentsArray = resultObject.getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray("address_components");
		JsonObject districtObject = componentsArray.get(2).getAsJsonObject();
		String districtName = districtObject.get("long_name").getAsString();

		_data = new ResultMap();
		List<Object> valueList = new ArrayList<>();
		valueList.add(districtName);
		_data.put("district", valueList);
	}

}
