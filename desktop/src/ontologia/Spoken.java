package ontologia;

import jade.content.Predicate;

/**
 * Spoken predicate used by chat ontology.
 * 
 * @author Michele Izzo - Telecomitalia
 */

@SuppressWarnings("serial")
public class Spoken implements Predicate {

	private String _what;

	public void setWhat(String what) {
		_what = what;
	}

	public String getWhat() {
		return _what;
	}

}