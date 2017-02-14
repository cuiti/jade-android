package agentes;

import jade.content.ContentManager;
import jade.content.abs.AbsAggregate;
import jade.content.abs.AbsConcept;
import jade.content.abs.AbsPredicate;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;
import ontologia.AppOntology;
import ontologia.InfoMensaje;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import gui.ControlPanel;

public class AgenteDesktop extends Agent {
	private static final long serialVersionUID = 1594371294421614291L;
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	private static final String INFO_ID = "__info__";
	private static final String ADMIN_NAME = "manager";

	private ControlPanel controlPanel;
	private Set conectados = new SortedSetImpl();
	private Codec codec = new SLCodec();
	private Ontology ontology = AppOntology.getInstance();
	private ACLMessage mensaje;
	private String fecha="default";

	protected void setup() {
		ContentManager cm = getContentManager();
		cm.registerLanguage(codec);
		cm.registerOntology(ontology);
		cm.setValidationMode(false);

		SimpleDateFormat formatoDeFecha = new SimpleDateFormat("yyyy.MM.dd.HH.mm");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		fecha = formatoDeFecha.format(timestamp);
		
		addBehaviour(new AdministradorDeSuscripcion(this));
		addBehaviour(new InformacionRecibida(this));

		mensaje = new ACLMessage(ACLMessage.INFORM);
		mensaje.setConversationId(INFO_ID);
		mensaje.setLanguage(codec.getName());
		mensaje.setOntology(ontology.getName());
		
		controlPanel = new ControlPanel(this);
		
	}

	protected void takeDown() {
		
	}
		
	class AdministradorDeSuscripcion extends CyclicBehaviour {
		private static final long serialVersionUID = -4845730529175649756L;
		private MessageTemplate template;

		AdministradorDeSuscripcion(Agent agente) {
			super(agente);
		}

		public void onStart() {
			ACLMessage subscripcion = new ACLMessage(ACLMessage.SUBSCRIBE);
			subscripcion.setLanguage(codec.getName());
			subscripcion.setOntology(ontology.getName());
			String conversationId = "C-" + myAgent.getLocalName();
			subscripcion.setConversationId(conversationId);
			subscripcion.addReceiver(new AID(ADMIN_NAME, AID.ISLOCALNAME));
			myAgent.send(subscripcion);
			template = MessageTemplate.MatchConversationId(conversationId);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						AbsPredicate predicate = (AbsPredicate) myAgent.getContentManager().extractAbsContent(msg);
						if (predicate.getTypeName().equals(AppOntology.INGRESO)) {
							AbsAggregate aggregate = (AbsAggregate) predicate.getAbsTerm(AppOntology.INGRESO_AGENTESINGRESO);
							if (aggregate != null) {
								Iterator it = aggregate.iterator();
								while (it.hasNext()) {
									AbsConcept concept = (AbsConcept) it.next();
									conectados.add(BasicOntology.getInstance().toObject(concept));
								}
							}
						}
						if (predicate.getTypeName().equals(AppOntology.EGRESO)) {
							AbsAggregate aggregate = (AbsAggregate) predicate.getAbsTerm(AppOntology.EGRESO_AGENTESEGRESO);
							if (aggregate != null) {
								Iterator it = aggregate.iterator();
								while (it.hasNext()) {
									AbsConcept concept = (AbsConcept) it.next();
									conectados.remove(BasicOntology.getInstance().toObject(concept));
								}
							}
						}
					} 
					catch (Exception e) {
						Logger.println(e.toString());
						e.printStackTrace();
					}
				} 
				else {
					loguearErrores(msg);
				}
			} 
			else {
				block();
			}
		}
	} 
	
	class InformacionRecibida extends CyclicBehaviour {
		private static final long serialVersionUID = 4881864151160276717L;
		private MessageTemplate template = MessageTemplate.MatchConversationId(INFO_ID);
		
		InformacionRecibida(Agent agente) {
			super(agente);
		}

		public void action() {
			ACLMessage mensaje = myAgent.receive(template);
			if (mensaje != null) {
				if (mensaje.getPerformative() == ACLMessage.INFORM) {
					try {
						ContentManager contentManager = myAgent.getContentManager();
						InfoMensaje infoMensaje = (InfoMensaje) contentManager.extractContent(mensaje);
						loguearInformacion(infoMensaje);
						notifySpoken(mensaje.getSender().getLocalName(),infoMensaje);
					} 
					catch (OntologyException | CodecException e) {
						e.printStackTrace();
					}
				} 
				else {
					loguearErrores(mensaje);
				}
			}
			else {
				block();
			}
		}
	} 
	
	private void loguearInformacion(InfoMensaje infoMensaje){
		
		String directorio = System.getProperty("user.dir")+"/logs/log"+fecha+".txt";
		
		FileWriter fileWriter = null;
		PrintWriter printWriter = null;		
		try {
			fileWriter = new FileWriter(directorio, true);
			printWriter = new PrintWriter(fileWriter);			
			printWriter.println("Nombre: " + infoMensaje.getNombreMarcaModelo());
			printWriter.println("Fecha: " + infoMensaje.getFecha());	
			printWriter.println("Hardware: " + infoMensaje.getNombreHardware());
			printWriter.println("SDK: " + infoMensaje.getSDKversionNumber());
			printWriter.println("Display: " + infoMensaje.getNombreDisplay());			
			printWriter.println("Operador: " + infoMensaje.getOperadorDeTelefono());
			printWriter.println("Nivel Bateria: " + infoMensaje.nivelBateriaToString());
			printWriter.println("Uso de CPU: " + infoMensaje.porcentajeUsoCpuToString());
			printWriter.println("Memoria Libre: " + infoMensaje.memoriaLibreToString());
			printWriter.println("Latitud: " + infoMensaje.getLatitud());
			printWriter.println("Longitud: " + infoMensaje.getLongitud());
			printWriter.println("Altitud: " + infoMensaje.getAltitud());
			printWriter.println("Mensaje: " + infoMensaje.getMensaje());
			printWriter.println("Ultimo SMS: " + infoMensaje.getUltimoSMS());
			printWriter.println();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fileWriter != null){
				try {
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	private void notifySpoken(String speaker, InfoMensaje mensaje) {
		controlPanel.notifySpoken(speaker, mensaje);
	}
	
	public String[] nombresDeConectados() {
		String[] pp = new String[conectados.size()];
		Iterator it = conectados.iterator();
		int i = 0;
		while (it.hasNext()) {
			AID id = (AID) it.next();
			pp[i++] = id.getLocalName();
		}
		return pp;
	}

	private void loguearErrores(ACLMessage msg) {
		if (logger.isLoggable(Logger.WARNING)) {
			logger.log(Logger.WARNING, "Unexpected message received from " + msg.getSender().getName());
			logger.log(Logger.WARNING, "Content is: " + msg.getContent());
		}
	}

}
