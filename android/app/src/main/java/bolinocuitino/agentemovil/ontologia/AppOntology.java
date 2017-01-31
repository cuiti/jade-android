package bolinocuitino.agentemovil.ontologia;

import jade.content.onto.BasicOntology;
import jade.content.onto.CFReflectiveIntrospector;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ConceptSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;


public class AppOntology extends Ontology implements AppVocabulary {

	private static Ontology appOntology = new AppOntology();

	public static Ontology getInstance() {
		return appOntology;
	}


	private AppOntology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance(), new CFReflectiveIntrospector());

		try {
			this.add(new PredicateSchema(INGRESO), Ingreso.class);
			this.add(new PredicateSchema(EGRESO), Egreso.class);
			this.add(new PredicateSchema(INFOMENSAJE), InfoMensaje.class);

			PredicateSchema ps = (PredicateSchema) getSchema(INGRESO);
			ps.add(INGRESO_AGENTESINGRESO, (ConceptSchema)this.getSchema(BasicOntology.AID), 1, -1);

			ps = (PredicateSchema) getSchema(EGRESO);
			ps.add(EGRESO_AGENTESEGRESO, (ConceptSchema)this.getSchema(BasicOntology.AID), 1, -1);

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