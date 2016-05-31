package proj.data;


import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.Md5Crypt;


public class IdGenerator
{

	public static String generateMd5Id(String name, String adress)
	{
		String formattedName = name.toLowerCase().replaceAll("\\s", "");

		String[] adressComponents = adress.replaceAll("\\(.*\\)", "").split(",");
		StringBuilder builder = new StringBuilder(formattedName);

		for ( String s : adressComponents )
		{
			builder.append(s);
		}

		try
		{
			return Md5Crypt.md5Crypt(builder.toString().getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
}
