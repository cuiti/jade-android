package ontologia;

import jade.content.Predicate;

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