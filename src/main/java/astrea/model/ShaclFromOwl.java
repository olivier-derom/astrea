package astrea.model;

import java.util.List;
import org.apache.jena.rdf.model.Model;

public interface ShaclFromOwl {

	public Model fromURL(String owlUrl);
	public Model fromURLs(List<String> owlUrls);
	public Model fromOwl(String owlContent, String format);
	public Model fromModel(Model ontology);
}
