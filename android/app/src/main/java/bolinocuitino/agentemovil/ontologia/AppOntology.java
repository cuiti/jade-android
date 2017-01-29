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
			this.add(new PredicateSchema(INFOMENSAJE), InfoMensaje.class);

			PredicateSchema ps = (PredicateSchema)this.getSchema(JOINED);
			ps.add(JOINED_WHO, (ConceptSchema)this.getSchema(BasicOntology.AID), 1, -1);

			ps = (PredicateSchema)this.getSchema(LEFT);
			ps.add(LEFT_WHO, (ConceptSchema)this.getSchema(BasicOntology.AID), 1, -1);

			ps = (PredicateSchema)this.getSchema(SPOKEN);
			ps.add(SPOKEN_WHAT, (PrimitiveSchema)this.getSchema(BasicOntology.STRING));

			ps = (PredicateSchema) getSchema(INFOMENSAJE);
			ps.add(INFOMENSAJE_MENSAJE,(PrimitiveSchema) getSchema(BasicOntology.STRING));
			ps.add(INFOMENSAJE_FECHA, (PrimitiveSchema) getSchema(BasicOntology.DATE));
			ps.add(INFOMENSAJE_NOMBREHARDWARE,(PrimitiveSchema) getSchema(BasicOntology.STRING));
			ps.add(INFOMENSAJE_SDKVERSIONNUMBER,(PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			ps.add(INFOMENSAJE_NOMBREDISPLAY,(PrimitiveSchema) getSchema(BasicOntology.STRING));
			ps.add(INFOMENSAJE_NOMBREMARCAMODELO,(PrimitiveSchema) getSchema(BasicOntology.STRING));
			ps.add(INFOMENSAJE_ULTIMOSMS,(PrimitiveSchema) getSchema(BasicOntology.STRING));
			ps.add(INFOMENSAJE_LATITUD,(PrimitiveSchema) getSchema(BasicOntology.FLOAT));
			ps.add(INFOMENSAJE_LONGITUD,(PrimitiveSchema) getSchema(BasicOntology.FLOAT));
			ps.add(INFOMENSAJE_ALTITUD,(PrimitiveSchema) getSchema(BasicOntology.FLOAT));
			ps.add(INFOMENSAJE_NUMERODETELEFONO,(PrimitiveSchema) getSchema(BasicOntology.STRING));
			ps.add(INFOMENSAJE_OPERADORDETELEFONO,(PrimitiveSchema) getSchema(BasicOntology.STRING));

		} catch (OntologyException var2) {
			var2.printStackTrace();
		}

	}
}