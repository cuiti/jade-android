package bolinocuitino.agentemovil.ontologia;

import java.util.List;

import jade.content.Predicate;
import jade.core.AID;

public class Joined implements Predicate {

	private List<AID> _who;

	public Joined() {
	}

	public void setWho(List<AID> who) {
		_who = who;
	}

	public List<AID> getWho() {
		return _who;
	}

}
