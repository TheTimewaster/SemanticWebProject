package proj.web.resources.imp;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import proj.data.ResultMap;
import proj.web.parsing.ResourceParser;
import proj.web.resources.SingleWebResource;
import proj.web.workflow.WorkflowInterruptedException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


public class GoogleGeocodingResource extends SingleWebResource
{
	private double				_lat, _lng;

	private final static String	URL_TEMPLATE	= "https://maps.googleapis.com/maps/api/geocode/json?latlng=%s,%s&key=AIzaSyAV201q9SNmr2WXEzT9HrSVG_YdMEjQn-M";

	public GoogleGeocodingResource(double lat, double lng)
	{
		super(null);
		_lat = lat;
		_lng = lng;
	}

	@Override
	public void startWorkflow() throws WorkflowInterruptedException
	{
		ByteArrayOutputStream out = null;

		try
		{
			String requestUrl = String.format(URL_TEMPLATE, String.valueOf(_lat), String.valueOf(_lng));
			out = new ByteArrayOutputStream();

			executeRequest(requestUrl, out);

			String rawJson = new String(out.toByteArray(), "UTF-8");
			JsonObject resultObject = (JsonObject) ResourceParser.parseResource(rawJson, WebResourceType.JSON_OBJ);

			if ( !tryExtractAdress(resultObject, "sublocality_level_2") )
			{
				if ( !tryExtractAdress(resultObject, "sublocality_level_1") )
				{
					tryExtractAdress(resultObject, "locality");
				}
			}
		}
		catch (Exception e)
		{
			throw new WorkflowInterruptedException(e);
		}
		finally
		{
			if ( out != null )
			{
				try
				{
					out.close();
				}
				catch (IOException e)
				{
					throw new WorkflowInterruptedException(e);
				}
			}
		}

	}

	private boolean tryExtractAdress(JsonObject resultObject, String sublocalKey)
	{
		outerloop: for ( JsonElement componentPrimitive : resultObject.getAsJsonArray("results") )
		{
			JsonObject componentObj = componentPrimitive.getAsJsonObject();

			if ( componentObj.get("types").getAsJsonArray().contains(new JsonPrimitive(sublocalKey)) )
			{
				for ( JsonElement primitiveAdress : componentObj.get("address_components").getAsJsonArray() )
				{
					JsonObject adressObject = primitiveAdress.getAsJsonObject();

					if ( adressObject.get("types").getAsJsonArray().contains(new JsonPrimitive(sublocalKey)) )
					{
						String districtName = adressObject.get("long_name").getAsString();

						String address = resultObject.getAsJsonArray("results").get(0).getAsJsonObject()
						        .getAsJsonPrimitive("formatted_address").getAsString();

						_data = new ResultMap();
						List<Object> valueList = new ArrayList<>();
						valueList.add(districtName);
						valueList.add(address);

						_data.put(ResultMap.FULL_ADDRESS_KEY, valueList);
						break outerloop;
					}
				}
			}
		}

		return (_data != null);
	}

}
