package bolinocuitino.agentemovil.agentes;

import java.util.List;
import java.util.logging.Level;

import bolinocuitino.agentemovil.ontologia.InfoMensaje;
import bolinocuitino.agentemovil.ontologia.Joined;
import bolinocuitino.agentemovil.ontologia.AppOntology;
import bolinocuitino.agentemovil.ontologia.Left;

import jade.content.ContentManager;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;
import android.content.Intent;
import android.content.Context;

public class AgenteMobile extends Agent implements IAgenteMobile {
	private static final long serialVersionUID = 1594371294421614291L;
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private static final String INFO_ID = "__info__";
	private static final String ADMIN_NAME = "manager";

    private Set participants = new SortedSetImpl();
	private Codec codec = new SLCodec();
	private Ontology ontology = AppOntology.getInstance();
	private ACLMessage mensaje;
	private Context context;

	protected void setup() {
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
			}
		}
		
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

		registerO2AInterface(IAgenteMobile.class, this);

		//una vez que el Agente arranca, avisa por broadcast, y el MainActivity escucha e incia
		//el activity de comunicación
		Intent broadcast = new Intent();
		broadcast.setAction("bolinocuitino.agentemovil.INICIAR_COMUNICACION");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}

	protected void takeDown() {
	}

	private void notifySpoken(String emisor, InfoMensaje infoMensaje) {
		Intent broadcast = new Intent();
		broadcast.setAction("bolinocuitino.agentemovil.ACTUALIZAR");
		broadcast.putExtra("informacion", emisor + ": " + infoMensaje + "\n");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}
	

	/**
	 * Este comportamiento sirve para registrar al agente mobile contra el
	 * agente manager que se ejecuta en el desktop, usando los mensajes ACL
	 * Los comportamientos cyclic viven durante toda la ejecución del agente,
	 * por eso sirve para quedar escuchando al manager y que vaya llegando la nueva info
	 */
	class AdministradorDeSuscripcion extends CyclicBehaviour {
		private MessageTemplate template;

		AdministradorDeSuscripcion(Agent a) {
			super(a);
		}

		public void onStart() {
			/**
			 * Un ACL message es un mensaje del estandar "Agent Communication Language" que
			 * es provisto y usado por JADE.
			 * Declarando el tipo "subscribe" ya no necesitamos ponerle mensaje porque se
			 * usa solo para esto
			 */
			ACLMessage suscripcion = new ACLMessage(ACLMessage.SUBSCRIBE);
			suscripcion.setLanguage(codec.getName());
			suscripcion.setOntology(ontology.getName());
			String convId = "C-" + myAgent.getLocalName();
			suscripcion.setConversationId(convId);
			/**
			 *  AID es AgentID, esta clase se usa internamente en JADE para mantener los datos de
			 *  los agentes en las tablas de agentes.
			 * Con esta línea el agente se agrega a si mismo como receptor de la suscripcion
			   */
			suscripcion.addReceiver(new AID(ADMIN_NAME, AID.ISLOCALNAME));
			myAgent.send(suscripcion);
			/**
			 * La clase MessageTemplate sirve para filtrar la información dentro de los ACLmessage,
			 * en este caso nos interesa recibir la informacion que esté dirigida a este agente,
			 * para eso usamos el conversation ID
			 */
			template = MessageTemplate.MatchConversationId(convId);
		}

		public void action() {
			// Receives information about people joining and leaving
			// the chat from the ChatManager agent
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						Predicate p = (Predicate) myAgent.getContentManager().extractContent(msg);
						if(p instanceof Joined) {
							Joined joined = (Joined) p;
							List<AID> aid = (List<AID>) joined.getWho();
							for(AID a : aid)
								participants.add(a);
						}
						if(p instanceof Left) {
							Left left = (Left) p;
							List<AID> aid = (List<AID>) left.getWho();
							for(AID a : aid)
								participants.remove(a);
						}
					} catch (Exception e) {
						Logger.println(e.toString());
						e.printStackTrace();
					}
				} else {
					handleUnexpected(msg);
				}
			} else {
				block();
			}
		}
	}

	/**
	 * Inner class ChatListener. This behaviour registers as a chat participant
	 * and keeps the list of participants up to date by managing the information
	 * received from the ChatManager agent.
	 */
	private class InformacionRecibida extends CyclicBehaviour {
		private static final long serialVersionUID = 741233963737842521L;
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
                        notifySpoken(msg.getSender().getLocalName(), infoMensaje);
                    }
                    catch (OntologyException  e1) {
                        e1.printStackTrace();
                    }
                    catch (Codec.CodecException e2){
                        e2.printStackTrace();
                    }
				} else {
					handleUnexpected(msg);
				}
			} else {
				block();
			}
		}
	}

	private class EnvioDeInformacion extends OneShotBehaviour {
		private InfoMensaje datos;

		private EnvioDeInformacion(Agent a, InfoMensaje infoMensaje) {
			super(a);
			datos = infoMensaje;
		}

		public void action() {
			mensaje.clearAllReceiver();
			Iterator it = participants.iterator();
			while (it.hasNext()) {
				mensaje.addReceiver((AID) it.next());
            }

			try {
                ContentManager cm = myAgent.getContentManager();
                cm.fillContent(mensaje,datos);
			} catch (Codec.CodecException e1) {
				e1.printStackTrace();
			} catch (OntologyException e2) {
				e2.printStackTrace();
			}
			notifySpoken(myAgent.getLocalName(), datos);
    		send(mensaje);
		}
	}

	public void handleSpoken(InfoMensaje infoMensaje) {
		// usa el behaviour EnvioDeInformacion para enviar la info a todos los otros dispositivos
		addBehaviour(new EnvioDeInformacion(this, infoMensaje));
	}

	private void handleUnexpected(ACLMessage msg) {
		if (logger.isLoggable(Logger.WARNING)) {
			logger.log(Logger.WARNING, "Unexpected message received from "
					+ msg.getSender().getName());
			logger.log(Logger.WARNING, "Content is: " + msg.getContent());
		}
	}
}