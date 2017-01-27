package ontologia;

import jade.content.onto.*;
import jade.content.schema.*;

public class AppOntology extends Ontology implements AppVocabulary {
	private static Ontology appOntology = new AppOntology();

	public static Ontology getInstance() {
		return appOntology;
	}

	private AppOntology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance(), new CFReflectiveIntrospector());

		try {
			add(new PredicateSchema(JOINED), Joined.class);
			add(new PredicateSchema(LEFT), Left.class);
			add(new PredicateSchema(SPOKEN), Spoken.class);
		
			PredicateSchema ps = (PredicateSchema) getSchema(JOINED);
			ps.add(JOINED_WHO, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED);

			ps = (PredicateSchema) getSchema(LEFT);
			ps.add(LEFT_WHO, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED);

			ps = (PredicateSchema) getSchema(SPOKEN);
			ps.add(SPOKEN_WHAT, (PrimitiveSchema) getSchema(BasicOntology.STRING));
		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}

}
