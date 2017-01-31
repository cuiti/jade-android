package bolinocuitino.agentemovil.ontologia;

import java.util.List;

import jade.content.Predicate;
import jade.core.AID;

public class Egreso implements Predicate {

	private List<AID> agentesEgreso;

	public void setAgentesEgreso(List<AID> listaAgentes) {
		agentesEgreso = listaAgentes;
	}

	public List<AID> getAgentesEgreso() {
		return agentesEgreso;
	}
}