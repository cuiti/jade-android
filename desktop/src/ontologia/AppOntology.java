package ontologia;

import jade.content.onto.*;
import jade.content.schema.*;

public class AppOntology extends Ontology implements AppVocabulary {
	private static final long serialVersionUID = -5215913246226166205L;
	private static Ontology appOntology = new AppOntology();

	public static Ontology getInstance() {
		return appOntology;
	}

	private AppOntology() {
		super(ONTOLOGY_NAME, BasicOntology.getInstance(), new CFReflectiveIntrospector());

		try {
			add(new PredicateSchema(INGRESO), Ingreso.class);
			add(new PredicateSchema(EGRESO), Egreso.class);
			add(new PredicateSchema(INFOMENSAJE), InfoMensaje.class);
		
			PredicateSchema ps = (PredicateSchema) getSchema(INGRESO);
			ps.add(INGRESO_AGENTESINGRESO, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED);

			ps = (PredicateSchema) getSchema(EGRESO);
			ps.add(EGRESO_AGENTESEGRESO, (ConceptSchema) getSchema(BasicOntology.AID), 1, ObjectSchema.UNLIMITED);

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
			
		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}

}
