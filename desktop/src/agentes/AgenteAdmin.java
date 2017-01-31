package agentes;

import jade.core.Agent;
import jade.core.AID;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.BasicOntology;
import jade.content.abs.*;

import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.SubscriptionManager;
import ontologia.AppOntology;
import ontologia.Ingreso;
import jade.proto.SubscriptionResponder.Subscription;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.FailureException;

import jade.domain.introspection.IntrospectionOntology;
import jade.domain.introspection.Event;
import jade.domain.introspection.DeadAgent;
import jade.domain.introspection.AMSSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AgenteAdmin extends Agent implements SubscriptionManager {
	private static final long serialVersionUID = -2551564051681805734L;
	private Map<AID, Subscription> conectados = new HashMap<AID, Subscription>();
	private Codec codec = new SLCodec();
	private Ontology ontology = AppOntology.getInstance();
	private AMSSubscriber myAMSSubscriber;

	protected void setup() {
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		MessageTemplate messageTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
				MessageTemplate.and(
						MessageTemplate.MatchLanguage(codec.getName()),
						MessageTemplate.MatchOntology(ontology.getName())));
		
		addBehaviour(new SubscriptionResponder(this, messageTemplate, this));

		
		myAMSSubscriber = new AMSSubscriber() {
			private static final long serialVersionUID = 3451935788989870232L;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			protected void installHandlers(Map handlersTable) {
				
				handlersTable.put(IntrospectionOntology.DEADAGENT, new EventHandler() {
					
					private static final long serialVersionUID = -8595295021484121699L;

					public void handle(Event event) {
						DeadAgent deadAgent = (DeadAgent)event;
						AID aid = deadAgent.getAgent();
						if (conectados.containsKey(aid)) {
							try {
								deregister((Subscription) conectados.get(aid));
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					
				});
			}
		};
		
		addBehaviour(myAMSSubscriber);
	}

	protected void takeDown() {
		send(myAMSSubscriber.getCancel());
	}

	public boolean register(Subscription subscription) throws RefuseException, NotUnderstoodException { 
		try {
			AID newId = subscription.getMessage().getSender();
			if (!conectados.isEmpty()) {
				ACLMessage mensajeOriginal = subscription.getMessage().createReply();
				mensajeOriginal.setPerformative(ACLMessage.INFORM);
				
				ACLMessage mensajeCopia = (ACLMessage) mensajeOriginal.clone();
				mensajeCopia.clearAllReceiver();
				
				Ingreso ingreso = new Ingreso();
				List<AID> agentesIngreso = new ArrayList<AID>(1);
				agentesIngreso.add(newId);
				ingreso.setAgentesIngreso(agentesIngreso);
				getContentManager().fillContent(mensajeCopia, ingreso);
				agentesIngreso.clear();
			
				for (AID oldAid : conectados.keySet()) {
					Subscription oldS = (Subscription) conectados.get(oldAid);
					oldS.notify(mensajeCopia);
					
					agentesIngreso.add(oldAid);
				}

				getContentManager().fillContent(mensajeOriginal, ingreso);
				subscription.notify(mensajeOriginal);
			}
			
			conectados.put(newId, subscription);
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RefuseException("Subscription error");
		}		
	}

	public boolean deregister(Subscription subscription) throws FailureException {
		AID oldId = subscription.getMessage().getSender();
		if (conectados.remove(oldId) != null) {
			if (!conectados.isEmpty()) {
				try {
					ACLMessage mensaje = subscription.getMessage().createReply();
					mensaje.setPerformative(ACLMessage.INFORM);
					mensaje.clearAllReceiver();
					AbsPredicate predicate = new AbsPredicate(AppOntology.EGRESO);
					AbsAggregate agg = new AbsAggregate(BasicOntology.SEQUENCE);
					agg.add((AbsTerm) BasicOntology.getInstance().fromObject(oldId));
					predicate.set(AppOntology.EGRESO_AGENTESEGRESO, agg);
					getContentManager().fillContent(mensaje, predicate);

					for (Subscription sub : conectados.values()) {
						sub.notify(mensaje);
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
