package bolinocuitino.agentemovil.ontologia;

import jade.content.onto.BasicOntology;
import jade.content.onto.CFReflectiveIntrospector;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;


public class AppOntology extends Ontology implements AppVocabulary {

	// singleton
	private static Ontology appOntology = new AppOntology();

	public static Ontology getInstance() {
		return appOntology;
	}


	private AppOntology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance(), new CFReflectiveIntrospector());

		try {
			this.add(new PredicateSchema(JOINED), Joined.class);
			this.add(new PredicateSchema(LEFT), Left.class);
			this.add(new PredicateSchema(SPOKEN), Spoken.class);
			PredicateSchema oe = (PredicateSchema)this.getSchema(JOINED);
			oe.add(JOINED_WHO, (ConceptSchema)this.getSchema(BasicOntology.AID), 1, -1);
			oe = (PredicateSchema)this.getSchema(LEFT);
			oe.add(LEFT_WHO, (ConceptSchema)this.getSchema(BasicOntology.AID), 1, -1);
			oe = (PredicateSchema)this.getSchema(SPOKEN);
			oe.add(SPOKEN_WHAT, (PrimitiveSchema)this.getSchema(BasicOntology.STRING));
		} catch (OntologyException var2) {
			var2.printStackTrace();
		}

	}
}