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

	protected void setup() {
		ContentManager cm = getContentManager();
		cm.registerLanguage(codec);
		cm.registerOntology(ontology);
		cm.setValidationMode(false);

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

	private void notifySpoken(String speaker, InfoMensaje mensaje) {
		controlPanel.notifySpoken(speaker, mensaje);
	}
		
	class AdministradorDeSuscripcion extends CyclicBehaviour {
		private static final long serialVersionUID = -4845730529175649756L;
		private MessageTemplate template;

		AdministradorDeSuscripcion(Agent a) {
			super(a);
		}

		public void onStart() {
			ACLMessage subscripcion = new ACLMessage(ACLMessage.SUBSCRIBE);
			subscripcion.setLanguage(codec.getName());
			subscripcion.setOntology(ontology.getName());
			String convId = "C-" + myAgent.getLocalName();
			subscripcion.setConversationId(convId);
			subscripcion.addReceiver(new AID(ADMIN_NAME, AID.ISLOCALNAME));
			myAgent.send(subscripcion);
			template = MessageTemplate.MatchConversationId(convId);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						AbsPredicate p = (AbsPredicate) myAgent.getContentManager().extractAbsContent(msg);
						if (p.getTypeName().equals(AppOntology.JOINED)) {
							AbsAggregate agg = (AbsAggregate) p.getAbsTerm(AppOntology.JOINED_WHO);
							if (agg != null) {
								Iterator it = agg.iterator();
								while (it.hasNext()) {
									AbsConcept c = (AbsConcept) it.next();
									conectados.add(BasicOntology.getInstance().toObject(c));
								}
							}
						}
						if (p.getTypeName().equals(AppOntology.LEFT)) {
							AbsAggregate agg = (AbsAggregate) p.getAbsTerm(AppOntology.JOINED_WHO);
							if (agg != null) {
								Iterator it = agg.iterator();
								while (it.hasNext()) {
									AbsConcept c = (AbsConcept) it.next();
									conectados.remove(BasicOntology.getInstance().toObject(c));
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
		
		InformacionRecibida(Agent a) {
			super(a);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						ContentManager cm = myAgent.getContentManager();
						InfoMensaje infoMensaje = (InfoMensaje) cm.extractContent(msg);
						notifySpoken(msg.getSender().getLocalName(),infoMensaje);
					} 
					catch (OntologyException | CodecException e) {
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
	
	public String[] getParticipantNames() {
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
