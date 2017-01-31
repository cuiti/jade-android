package ontologia;

import java.util.List;

import jade.content.Predicate;
import jade.core.AID;

public class Ingreso implements Predicate {
	private static final long serialVersionUID = 5571491119318811418L;
	
	private List<AID> agentesIngreso;

	public void setAgentesIngreso(List<AID> listaAgentes) {
		agentesIngreso = listaAgentes;
	}

	public List<AID> getAgentesIngreso() {
		return agentesIngreso;
	}

}
