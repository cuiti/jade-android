package bolinocuitino.agentemovil.ontologia;

import java.util.List;

import jade.content.Predicate;
import jade.core.AID;

public class Ingreso implements Predicate {

	private List<AID> agentesIngreso;

	public Ingreso() {
	}

	public void setAgentesIngreso(List<AID> listaAgentes) {
		agentesIngreso = listaAgentes;
	}

	public List<AID> getAgentesIngreso() {
		return agentesIngreso;
	}

}
