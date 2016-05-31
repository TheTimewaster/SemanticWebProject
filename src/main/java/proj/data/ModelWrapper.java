package proj.data;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;


public class ModelWrapper
{
	Model rdfModel;

	public ModelWrapper()
	{
		rdfModel = ModelFactory.createDefaultModel();
	}
	
	public Resource createResource(String id)
	{
		return rdfModel.createResource(id);
	}
	
	public void addProperty(String id, Property key, String value)
	{
		Resource resource = rdfModel.getResource(id);
		resource.addProperty(key, value);
	}
}
