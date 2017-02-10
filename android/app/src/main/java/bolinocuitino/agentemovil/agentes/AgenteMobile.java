package bolinocuitino.agentemovil.agentes;

import java.util.List;
import java.util.logging.Level;

import bolinocuitino.agentemovil.gui.ComunicacionActivity;
import bolinocuitino.agentemovil.ontologia.InfoMensaje;
import bolinocuitino.agentemovil.ontologia.Ingreso;
import bolinocuitino.agentemovil.ontologia.AppOntology;
import bolinocuitino.agentemovil.ontologia.Egreso;

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
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.Iterator;
import jade.util.leap.Set;
import jade.util.leap.SortedSetImpl;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;

public class AgenteMobile extends Agent implements IAgenteMobile {
	private static final long serialVersionUID = 1594371294421614291L;
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private static final String INFO_ID = "__info__";
	private static final String ADMIN_NAME = "manager";

    private Set conectados = new SortedSetImpl();
	private Codec codec = new SLCodec();
	private Ontology ontology = AppOntology.getInstance();
	private ACLMessage mensaje;
	private Context context;
	private InfomacionEnviada infomacionEnviadaBehaviour;

	private ComunicacionActivity comActivity;

	protected void setup() {
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			if (args[0] instanceof Context) {
				context = (Context) args[0];
			}
		}
		
		ContentManager contentManager = getContentManager();
		contentManager.registerLanguage(codec);
		contentManager.registerOntology(ontology);
		contentManager.setValidationMode(false);

		SharedPreferences settings = context.getSharedPreferences("jadePreferencesFile", 0);
		int intervalo = Integer.parseInt(settings.getString("intervaloEnvio",""));

		infomacionEnviadaBehaviour = new InfomacionEnviada(this,intervalo);

		addBehaviour(new AdministradorDeSuscripcion(this));
		addBehaviour(infomacionEnviadaBehaviour);
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

	private void avisoDeInformacionEnviada(String emisor, InfoMensaje infoMensaje) {
		Intent broadcast = new Intent();
		broadcast.setAction("bolinocuitino.agentemovil.ACTUALIZAR");
		broadcast.putExtra("informacion", emisor + ": " + infoMensaje + "\n");
		logger.log(Level.INFO, "Sending broadcast " + broadcast.getAction());
		context.sendBroadcast(broadcast);
	}

	@Override
	public void obtenerActivity(ComunicacionActivity comunicacionActivity) {
		this.comActivity = comunicacionActivity;
	}

	@Override
	public InfoMensaje obtenerInformacion(ComunicacionActivity comunicacionActivity) {
		return comunicacionActivity.obtenerInformacionDelDispositivo();
	}

	public void detenerEnvioDeInformacion(){
		infomacionEnviadaBehaviour.stop();
	}

	public void avisoDeSalida(){
		addBehaviour(new MensajeIndividual(this,"salida"));
	}

	public void avisoDeEntrada(){
		addBehaviour(new MensajeIndividual(this,"entrada"));
	}


	/**
	 * Este comportamiento sirve para registrar al agente mobile contra el
	 * agente manager que se ejecuta en el desktop, usando los mensajes ACL
	 * Los comportamientos cyclic viven durante toda la ejecución del agente,
	 * por eso sirve para quedar escuchando al manager y que vaya llegando la nueva info
	 */
	class AdministradorDeSuscripcion extends CyclicBehaviour {
		private MessageTemplate template;

		AdministradorDeSuscripcion(Agent agente) {
			super(agente);
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
			String conversationId = "C-" + myAgent.getLocalName();
			suscripcion.setConversationId(conversationId);
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
			template = MessageTemplate.MatchConversationId(conversationId);
		}

		public void action() {
			ACLMessage msg = myAgent.receive(template);
			if (msg != null) {
				if (msg.getPerformative() == ACLMessage.INFORM) {
					try {
						Predicate predicate = (Predicate) myAgent.getContentManager().extractContent(msg);
						if(predicate instanceof Ingreso) {
							Ingreso ingreso = (Ingreso) predicate;
							List<AID> agentesIngreso = ingreso.getAgentesIngreso();
							for(AID aid : agentesIngreso)
								conectados.add(aid);
						}
						if(predicate instanceof Egreso) {
							Egreso egreso = (Egreso) predicate;
							List<AID> agentesEgreso = egreso.getAgentesEgreso();
							for(AID aid : agentesEgreso)
								conectados.remove(aid);
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

	private class InformacionRecibida extends CyclicBehaviour {
		private static final long serialVersionUID = 741233963737842521L;
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
                        avisoDeInformacionEnviada(mensaje.getSender().getLocalName(), infoMensaje);
                    }
                    catch (OntologyException  e1) {
                        e1.printStackTrace();
                    }
                    catch (Codec.CodecException e2){
                        e2.printStackTrace();
                    }
				} else {
					handleUnexpected(mensaje);
				}
			} else {
				block();
			}
		}
	}

	private class InfomacionEnviada extends TickerBehaviour {
		private InfoMensaje datos;

        private InfomacionEnviada(Agent agente,int intervalo) {
			super(agente, (intervalo * 1000));
		}

		@Override
		protected void onTick() {
			mensaje.clearAllReceiver();
			Iterator it = conectados.iterator();
			while (it.hasNext()) {
				mensaje.addReceiver((AID) it.next());
			}

			datos = obtenerInformacion(comActivity);

			try {
				ContentManager contentManager = myAgent.getContentManager();
				contentManager.fillContent(mensaje,datos);
			} catch (Codec.CodecException e1) {
				e1.printStackTrace();
			} catch (OntologyException e2) {
				e2.printStackTrace();
			}
			avisoDeInformacionEnviada(myAgent.getLocalName(), datos);
			send(mensaje);
		}
	}

	private class MensajeIndividual extends OneShotBehaviour{

		private InfoMensaje datos;
		private String textoMsj;

		private MensajeIndividual(Agent agente, String texto) {
			super(agente);
			this.textoMsj = texto;
		}

		@Override
		public void action() {
			mensaje.clearAllReceiver();
			Iterator it = conectados.iterator();
			while (it.hasNext()) {
				mensaje.addReceiver((AID) it.next());
			}

			datos = obtenerInformacion(comActivity);
			datos.setMensaje(textoMsj);

			try {
				ContentManager contentManager = myAgent.getContentManager();
				contentManager.fillContent(mensaje,datos);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			avisoDeInformacionEnviada(myAgent.getLocalName(), datos);
			send(mensaje);
		}
	}


	private void handleUnexpected(ACLMessage msg) {
		if (logger.isLoggable(Logger.WARNING)) {
			logger.log(Logger.WARNING, "Unexpected message received from "
					+ msg.getSender().getName());
			logger.log(Logger.WARNING, "Content is: " + msg.getContent());
		}
	}
}