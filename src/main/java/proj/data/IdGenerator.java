package proj.data;


import java.math.BigInteger;
import java.security.MessageDigest;


public class IdGenerator
{

	public static String generateMd5Id(String lat, String lon)
	{
		if ( lat.length() < 7 )
		{
			lat = lat.concat("0");
		}

		if ( lon.length() < 7 )
		{
			lon = lon.concat("0");
		}

		String latString = lat.substring(0, 7);
		String lngString = lon.substring(0, 7);

		try
		{

			StringBuilder builder = new StringBuilder();
			builder.append(lngString).append(";").append(latString);

			MessageDigest mdg = MessageDigest.getInstance("MD5");
			mdg.update(builder.toString().getBytes(), 0, builder.toString().length());

			return new BigInteger(1, mdg.digest()).toString(16);
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
