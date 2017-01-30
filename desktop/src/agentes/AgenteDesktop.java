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

	private static final String CHAT_ID = "__chat__";
	private static final String ADMIN_NAME = "manager";

	private ControlPanel controlPanel;
	private Set participants = new SortedSetImpl();
	private Codec codec = new SLCodec();
	private Ontology ontology = AppOntology.getInstance();
	private ACLMessage mensaje;

	protected void setup() {
		// Register language and ontology
		ContentManager cm = getContentManager();
		cm.registerLanguage(codec);
		cm.registerOntology(ontology);
		cm.setValidationMode(false);

		// Add initial behaviours
		addBehaviour(new ParticipantsManager(this));
		addBehaviour(new MensajeRecibido(this));

		// Initialize the message used to convey spoken sentences
		mensaje = new ACLMessage(ACLMessage.INFORM);
		mensaje.setConversationId(CHAT_ID);
		mensaje.setLanguage(codec.getName());
		mensaje.setOntology(ontology.getName());
		
		controlPanel = new ControlPanel(this);
		
	}

	protected void takeDown() {
		
	}

	private void notifySpoken(String speaker, InfoMensaje mensaje) {
		controlPanel.notifySpoken(speaker, mensaje);
	}
	
	
	class ParticipantsManager extends CyclicBehaviour {
		private static final long serialVersionUID = -4845730529175649756L;
		private MessageTemplate template;

		ParticipantsManager(Agent a) {
			super(a);
		}

		public void onStart() {
			// Subscribe as a chat participant to the ChatManager agent
			ACLMessage subscription = new ACLMessage(ACLMessage.SUBSCRIBE);
			subscription.setLanguage(codec.getName());
			subscription.setOntology(ontology.getName());
			String convId = "C-" + myAgent.getLocalName();
			subscription.setConversationId(convId);
			subscription
					.addReceiver(new AID(ADMIN_NAME, AID.ISLOCALNAME));
			myAgent.send(subscription);
			// Initialize the template used to receive notifications
			// from the ChatManagerAgent
			template = MessageTemplate.MatchConversationId(convId);
		}

		public void action() {
			// Receives information about people joining and leaving
			// the chat from the ChatManager agent
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						AbsPredicate p = (AbsPredicate) myAgent
							.getContentManager().extractAbsContent(msg);
						if (p.getTypeName().equals(AppOntology.JOINED)) {
							// Get new participants, add them to the list of
							// participants and notify the gui
							AbsAggregate agg = (AbsAggregate) p
									.getAbsTerm(AppOntology.JOINED_WHO);
							if (agg != null) {
								Iterator it = agg.iterator();
								while (it.hasNext()) {
									AbsConcept c = (AbsConcept) it.next();
									participants.add(BasicOntology
											.getInstance().toObject(c));
								}
							}
						}
						if (p.getTypeName().equals(AppOntology.LEFT)) {
							// Get old participants, remove them from the list
							// of participants and notify the gui
							AbsAggregate agg = (AbsAggregate) p
									.getAbsTerm(AppOntology.JOINED_WHO);
							if (agg != null) {
								Iterator it = agg.iterator();
								while (it.hasNext()) {
									AbsConcept c = (AbsConcept) it.next();
									participants.remove(BasicOntology
											.getInstance().toObject(c));
								}
							}
						}
					} catch (Exception e) {
						Logger.println(e.toString());
						e.printStackTrace();
					}
				} else {
					loguearErrores(msg);
				}
			} else {
				block();
			}
		}
	} 
	
	class MensajeRecibido extends CyclicBehaviour {
		private static final long serialVersionUID = 4881864151160276717L;
		private MessageTemplate template = MessageTemplate.MatchConversationId(CHAT_ID);
		
		MensajeRecibido(Agent a) {
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
		String[] pp = new String[participants.size()];
		Iterator it = participants.iterator();
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
