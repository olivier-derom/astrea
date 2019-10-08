package sharper.generators;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import shaper.model.ShaperModel;

public class OwlShaper implements ShaperModel{
	
	// 0. Query to create initial NodeShapes
	private final String QUERY_FETCH_CLASSES = "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"+
											  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
													 "PREFIX sh: <http://www.w3.org/ns/shacl#>" + 
													 "CONSTRUCT {  ?shapeUrl a sh:NodeShape ;  \n"
													 + "			   		sh:targetClass ?type ;\n"
													 + "			   		sh:deactivated \"false\";\n"
													 // Including data properties
													 + "					sh:property [ \n"
													 + "						sh:path ?dataProperty ;\n"
													 + "						sh:datatype ?datatype ;\n"
													 + "			   	 		sh:name ?dataPropertyName ;\n"
													 + "			   			sh:description ?dataPropertyComment ;\n"
													// + "			   			sh:message \"Error with property\""
													 + "]; "
													 // Including object properties
													 + "				sh:property [ \n"
													 + "						sh:path ?objectProperty ;\n"
													 + "						sh:class ?typeInRange ;"
													 + "			   	 		sh:name ?objectPropertyName ;\n"
													 + "			   			sh:description ?objectPropertyComment ;\n"
													 + "];"
													 
													 + " }"
													 + "WHERE { "
													 + "?type a owl:Class . \n"
													 // Data types extractor
													 + "OPTIONAL { ?dataProperty a owl:DatatypeProperty ;\n"
													 + "		 rdfs:domain ?type ;\n"
													 + "		 rdfs:range ?datatype ."
													 + "		 OPTIONAL {?dataProperty rdfs:label ?dataPropertyName } .\n"
													 + "		 OPTIONAL {?dataProperty rdfs:comment ?dataPropertyComment }.\n"
													 + "}"
													// Object properties extractor
													 + "OPTIONAL { ?objectProperty a owl:ObjectProperty ;\n"
													 + "		 rdfs:domain ?type ;\n"
													 + "		 rdfs:range ?typeInRange .\n"
													 + "		 OPTIONAL {?objectProperty rdfs:label ?objectPropertyName } .\n"
													 + "		 OPTIONAL {?objectProperty rdfs:comment ?objectPropertyComment }.\n"
													 + "		 FILTER (!isBlank(?objectProperty)) ."
													 + "}"
													 
													 + "FILTER (!isBlank(?type)) .\n"
													 + "BIND ( URI(CONCAT(STR(?type),\"Shape\")) AS ?shapeUrl) .\n"
													 + "}";
	
	// 0. Query to create initial NodeShapes
	private final String QUERY_FETCH_DATA_PROPERTIES = "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"+
												 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
												 "PREFIX sh: <http://www.w3.org/ns/shacl#> \n" + 
													 "CONSTRUCT {  ?shapeUrl a sh:PropertyShape .  \n"
													 + "			   ?shapeUrl sh:name ?propertyName .\n"
													 + "			   ?shapeUrl sh:description ?propertyComment .\n"
													 + "			   ?shapeUrl sh:node ?shapeNodeUrl . \n" // triplet to reference NodeShape (domain) of this property
													 + "			   ?shapeUrl sh:path ?propertyDatatype ."
													 + "			   ?shapeUrl sh:datatype ?propertyDatatype ."
													 + " }\n"
													 + "WHERE { \n"
													 + "?dataProperty a owl:DatatypeProperty .\n"
													 + "?dataProperty rdfs:domain ?type .\n"
													// + "?dataProperty rdfs:domain ?type ."
													 + "OPTIONAL { ?dataProperty rdfs:label ?propertyName . }\n"
													 + "OPTIONAL { ?dataProperty rdfs:comment ?propertyComment . }\n"
													 + "FILTER (!isBlank(?type) && !isBlank(?dataProperty)) .\n"
													 + "BIND ( URI(CONCAT(STR(?type),\"Shape\")) AS ?shapeNodeUrl) .\n"
													 + "BIND ( URI(CONCAT(STR(?dataProperty),\"-Shape\")) AS ?shapeUrl) .\n"
													 + "}";

	private final String QUERY_FETCH_OBJECT_PROPERTIES = "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"+
														 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
														 "PREFIX sh: <http://www.w3.org/ns/shacl#> \n" + 
															 "CONSTRUCT {  ?shapeUrl a sh:PropertyShape .  \n"
															 + "			   ?shapeUrl sh:name ?propertyName .\n"
															 + "			   ?shapeUrl sh:description ?propertyComment .\n"
															 + "			   ?shapeUrl sh:node ?shapeNodeUrl . \n" // triplet to reference NodeShape (domain) of this property
															 + "			   ?shapeUrl sh:path ?propertyDatatype .\n"
															 + "			   ?shapeUrl sh:class ?typeInRange .\n"
															 + " }\n"
															 + "WHERE { \n"
															 + "?dataProperty a owl:ObjectProperty .\n"
															 + "?dataProperty rdfs:domain ?type .\n"
															 + "?dataProperty rdfs:range ?typeInRange.\n"
															// + "?dataProperty rdfs:domain ?type ."
															 + "OPTIONAL { ?dataProperty rdfs:label ?propertyName . }\n"
															 + "OPTIONAL { ?dataProperty rdfs:comment ?propertyComment . }\n"
															 + "FILTER (!isBlank(?type) && !isBlank(?dataProperty)) .\n"
															 + "BIND ( URI(CONCAT(STR(?type),\"Shape\")) AS ?shapeNodeUrl) .\n"
															 + "BIND ( URI(CONCAT(STR(?dataProperty),\"-Shape\")) AS ?shapeUrl) .\n"
															 + "}";

	
	public Model fromModelToShapes(String owlUrl) {
		Model ontology = ModelFactory.createDefaultModel();
		ontology.read(owlUrl);
		Model shapes = ModelFactory.createDefaultModel();
//		Query queryClasses = QueryFactory.create(QUERY_FETCH_CLASSES);
//		QueryExecution qeClasses = QueryExecutionFactory.create(queryClasses, ontology);
//		Model shapes = qeClasses.execConstruct();
		
	
		Query queryDataProperties = QueryFactory.create(QUERY_FETCH_DATA_PROPERTIES);
		QueryExecution qeDataProperties = QueryExecutionFactory.create(queryDataProperties, ontology);
		Model shapesDataProperties = qeDataProperties.execConstruct();
		shapes.add(shapesDataProperties);
		
		Query queryObjectProperties = QueryFactory.create(QUERY_FETCH_DATA_PROPERTIES);
		QueryExecution qeObjectProperties = QueryExecutionFactory.create(queryObjectProperties, ontology);
		Model shapesObjectProperties = qeObjectProperties.execConstruct();
		shapes.add(shapesObjectProperties);
		
		shapes.write(System.out, "TURTLE");
		return shapes;
		
	}

}
