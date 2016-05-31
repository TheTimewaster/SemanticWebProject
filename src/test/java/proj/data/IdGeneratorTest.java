package proj.data;


import org.junit.Test;


public class IdGeneratorTest
{
	@Test
	public void generatorTest()
	{
		String hash = IdGenerator.generateMd5Id("51.343636", "12.374173");
		String hash0 = IdGenerator.generateMd5Id("51.3435736", "12.3741949");
		System.out.println(hash);
		System.out.println(hash0);
	}

}
