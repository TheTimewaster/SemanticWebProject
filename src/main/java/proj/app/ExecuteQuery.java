package proj.app;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoublePredicate;

import javax.swing.text.ChangedCharSetException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;


public class ExecuteQuery
{

	private static final String	OTHER_KEY		= "other";
	private static final String	SUPERMARKET_KEY	= "supermarket";
	private static final String	DISCOUNTER_KEY	= "discounter";
	private static final String	ORGANIC_KEY		= "organic";
	private static final String	RENT_KEY		= "rent";

	public static void main(String[] args)
	{
		HttpClient client = HttpClientBuilder.create().build();

		try
		{
			String mainQuery = "query=PREFIX thoangth: <http://www.imn.htwk-leipzig.de/thoangth#>"
			        + " SELECT ?name ?rent ?storename (group_concat(?type) as ?types)" + " WHERE {"
			        + " ?district thoangth:name ?name." + " ?district thoangth:rent ?rent."
			        + " ?district thoangth:store ?store." + " ?store thoangth:storetype ?type."
			        + " ?store thoangth:name ?storename." + " FILTER EXISTS {?district thoangth:store ?store}."
			        + " FILTER EXISTS {?district thoangth:rent ?rent}}" + " GROUP BY ?name ?rent ?storename"
			        + " ORDER BY DESC(?name)";

			HttpPost post = new HttpPost();
			post.setURI(new URI("http://localhost:3030/stores/query"));
			post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			post.setHeader("Accept", "application/sparql-results+json,*/*;q=0.9");
			HttpEntity queryEntity = new StringEntity(mainQuery, Charset.forName("UTF-8"));
			post.setEntity(queryEntity);

			HttpResponse response = client.execute(post);
			HttpEntity responseEntity = response.getEntity();

			InputStream responseInput = responseEntity.getContent();

			JsonReader reader = new JsonReader(new InputStreamReader(responseInput, Charset.forName("UTF-8")));
			JsonParser jsonParser = new JsonParser();
			JsonObject mainObject = jsonParser.parse(reader).getAsJsonObject();

			JsonArray resultsArray = mainObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();

			Map<String, Map<String, Object>> resultMap = new HashMap<>();

			for ( JsonElement resultElement : resultsArray )
			{
				String districtName = resultElement.getAsJsonObject().get("name").getAsJsonObject().get("value")
				        .getAsString();

				Map<String, Object> dataMap = null;

				if ( !resultMap.containsKey(districtName) )
				{
					dataMap = new HashMap<String, Object>();
					String districtAvgRent = resultElement.getAsJsonObject().get(RENT_KEY).getAsJsonObject()
					        .get("value").getAsString();
					dataMap.put(RENT_KEY, Double.parseDouble(districtAvgRent));
					dataMap.put(ORGANIC_KEY, 0);
					dataMap.put(DISCOUNTER_KEY, 0);
					dataMap.put(SUPERMARKET_KEY, 0);
					dataMap.put(OTHER_KEY, 0);

					resultMap.put(districtName, dataMap);
				}
				else
				{
					dataMap = resultMap.get(districtName);
				}

				String storeName = resultElement.getAsJsonObject().get("storename").getAsJsonObject().get("value")
				        .getAsString();
				String storeTypes = resultElement.getAsJsonObject().get("types").getAsJsonObject().get("value")
				        .getAsString();

				if ( storeTypes.toLowerCase().contains(ORGANIC_KEY) || storeTypes.contains("bio") )
				{
					dataMap.put(ORGANIC_KEY, ((int) dataMap.get(ORGANIC_KEY)) + 1);
				}
				else if ( storeName.toLowerCase().contains("aldi") || storeName.toLowerCase().contains("lidl")
				        || storeName.toLowerCase().contains("netto") || storeName.toLowerCase().contains("penny")
				        || storeName.toLowerCase().contains("norma") )
				{
					dataMap.put(DISCOUNTER_KEY, ((int) dataMap.get(DISCOUNTER_KEY)) + 1);
				}
				else if ( storeName.toLowerCase().contains("edeka") || storeName.toLowerCase().contains("rewe")
				        || storeName.toLowerCase().contains("konsum") || storeName.toLowerCase().contains("kaufland") )
				{
					dataMap.put(SUPERMARKET_KEY, ((int) dataMap.get(SUPERMARKET_KEY)) + 1);
				}
				else
				{
					dataMap.put(OTHER_KEY, ((int) dataMap.get(OTHER_KEY)) + 1);
				}
			}

			JsonArray ret = new JsonArray();
			Set<String> keySet = resultMap.keySet();
			for ( String key : keySet )
			{
				JsonObject object = new JsonObject();
				object.addProperty("district", key);
				Map<String, Object> dataMap = resultMap.get(key);

				object.addProperty(RENT_KEY, (Double) dataMap.get(RENT_KEY));
				object.addProperty(ORGANIC_KEY, (Integer) dataMap.get(ORGANIC_KEY));
				object.addProperty(DISCOUNTER_KEY, (Integer) dataMap.get(DISCOUNTER_KEY));
				object.addProperty(SUPERMARKET_KEY, (Integer) dataMap.get(SUPERMARKET_KEY));
				object.addProperty(OTHER_KEY, (Integer) dataMap.get(OTHER_KEY));

				ret.add(object);
			}

			System.out.println(ret.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

}
