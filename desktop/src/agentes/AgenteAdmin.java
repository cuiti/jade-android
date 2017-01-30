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
import ontologia.Joined;
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
		// Unsubscribe from the AMS
		send(myAMSSubscriber.getCancel());
		//FIXME: should inform current participants if any
	}

	public boolean register(Subscription subscription) throws RefuseException, NotUnderstoodException { 
		try {
			AID newId = subscription.getMessage().getSender();
			if (!conectados.isEmpty()) {
				ACLMessage notif1 = subscription.getMessage().createReply();
				notif1.setPerformative(ACLMessage.INFORM);
				
				ACLMessage notif2 = (ACLMessage) notif1.clone();
				notif2.clearAllReceiver();
				
				Joined joined = new Joined();
				List<AID> who = new ArrayList<AID>(1);
				who.add(newId);
				joined.setWho(who);
				getContentManager().fillContent(notif2, joined);
				who.clear();
				
				for (AID oldAid : conectados.keySet()) {
					Subscription oldS = (Subscription) conectados.get(oldAid);
					oldS.notify(notif2);
					
					who.add(oldAid);
				}

				getContentManager().fillContent(notif1, joined);
				subscription.notify(notif1);
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
					ACLMessage notif = subscription.getMessage().createReply();
					notif.setPerformative(ACLMessage.INFORM);
					notif.clearAllReceiver();
					AbsPredicate p = new AbsPredicate(AppOntology.LEFT);
					AbsAggregate agg = new AbsAggregate(BasicOntology.SEQUENCE);
					agg.add((AbsTerm) BasicOntology.getInstance().fromObject(oldId));
					p.set(AppOntology.LEFT_WHO, agg);
					getContentManager().fillContent(notif, p);

					for (Subscription sub : conectados.values()) {
						sub.notify(notif);
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
