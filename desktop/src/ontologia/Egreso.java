package ontologia;

import java.util.List;

import jade.content.Predicate;
import jade.core.AID;


public class Egreso implements Predicate {
	private static final long serialVersionUID = -4097688915842498566L;
	
	private List<AID> agentesEgreso;

	public void setAgentesEgreso(List<AID> listaAgentes) {
		agentesEgreso = listaAgentes;
	}

	public List<AID> getAgentesEgreso() {
		return agentesEgreso;
	}

}