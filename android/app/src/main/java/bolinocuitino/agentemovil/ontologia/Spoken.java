package bolinocuitino.agentemovil.ontologia;

import jade.content.Predicate;

public class Spoken implements Predicate {

	private String _what;

	public Spoken() {
	}

	public void setWhat(String what) {
		_what = what;
	}

	public String getWhat() {
		return _what;
	}

}