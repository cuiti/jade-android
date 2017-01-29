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
	private Map<AID, Subscription> participants = new HashMap<AID, Subscription>();
	private Codec codec = new SLCodec();
	private Ontology ontology = AppOntology.getInstance();
	private AMSSubscriber myAMSSubscriber;

	protected void setup() {
		// Prepare to accept subscriptions from chat participants
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);

		MessageTemplate sTemplate = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
				MessageTemplate.and(
						MessageTemplate.MatchLanguage(codec.getName()),
						MessageTemplate.MatchOntology(ontology.getName()) ) );
		addBehaviour(new SubscriptionResponder(this, sTemplate, this));

		// Register to the AMS to detect when chat participants suddenly die
		myAMSSubscriber = new AMSSubscriber() {
			protected void installHandlers(Map handlersTable) {
				// Fill the event handler table. We are only interested in the
				// DEADAGENT event
				handlersTable.put(IntrospectionOntology.DEADAGENT, new EventHandler() {
					public void handle(Event ev) {
						DeadAgent da = (DeadAgent)ev;
						AID id = da.getAgent();
						// If the agent was attending the chat --> notify all
						// other participants that it has just left.
						if (participants.containsKey(id)) {
							try {
								deregister((Subscription) participants.get(id));
							}
							catch (Exception e) {
								//Should never happen
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

	///////////////////////////////////////////////
	// SubscriptionManager interface implementation
	///////////////////////////////////////////////
	public boolean register(Subscription subscription) throws RefuseException, NotUnderstoodException { 
		try {
			AID newId = subscription.getMessage().getSender();
			// Notify the new participant about the others (if any) and VV
			if (!participants.isEmpty()) {
				// The message for the new participant
				ACLMessage notif1 = subscription.getMessage().createReply();
				notif1.setPerformative(ACLMessage.INFORM);

				// The message for the old participants.
				// NOTE that the message is the same for all receivers (a part from the
				// conversation-id that will be automatically adjusted by Subscription.notify()) 
				// --> Prepare it only once outside the loop
				ACLMessage notif2 = (ACLMessage) notif1.clone();
				notif2.clearAllReceiver();
				Joined joined = new Joined();
				List<AID> who = new ArrayList<AID>(1);
				who.add(newId);
				joined.setWho(who);
				getContentManager().fillContent(notif2, joined);

				who.clear();
				
				for (AID oldAid : participants.keySet()) {
					// Notify old participant
					Subscription oldS = (Subscription) participants.get(oldAid);
					oldS.notify(notif2);
					
					who.add(oldAid);
				}

				// Notify new participant
				getContentManager().fillContent(notif1, joined);
				subscription.notify(notif1);
			}
			
			// Add the new subscription
			participants.put(newId, subscription);
			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RefuseException("Subscription error");
		}		
	}

	public boolean deregister(Subscription subscription) throws FailureException {
		AID oldId = subscription.getMessage().getSender();
		// Remove the subscription
		if (participants.remove(oldId) != null) {
			// Notify other participants if any
			if (!participants.isEmpty()) {
				try {
					ACLMessage notif = subscription.getMessage().createReply();
					notif.setPerformative(ACLMessage.INFORM);
					notif.clearAllReceiver();
					AbsPredicate p = new AbsPredicate(AppOntology.LEFT);
					AbsAggregate agg = new AbsAggregate(BasicOntology.SEQUENCE);
					agg.add((AbsTerm) BasicOntology.getInstance().fromObject(oldId));
					p.set(AppOntology.LEFT_WHO, agg);
					getContentManager().fillContent(notif, p);

					for (Subscription sub : participants.values()) {
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
