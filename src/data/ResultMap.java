package data;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * To save some extra declaration writing ;)
 * 
 * @author Tu
 *
 */
public class ResultMap extends HashMap<String, List<Object>>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void writeToFile(String location, boolean append) throws IOException
	{
		FileWriter writer = null;

		try
		{
			Set<String> keySet = keySet();
			StringBuilder builder = new StringBuilder();

			for (String key : keySet)
			{
				String line = key + ";";
				List<Object> values = super.get(key);

				for (Object s : values)
				{
					line += s;
				}

				builder.append(line);
			}

			File file = new File(location);
			file.createNewFile();
			writer = new FileWriter(file, append);
			writer.write(builder.toString());
		} finally
		{
			if (writer != null)
			{
				writer.close();
			}
		}
	}
}
