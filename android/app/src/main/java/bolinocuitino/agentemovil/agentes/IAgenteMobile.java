package bolinocuitino.agentemovil.agentes;

import bolinocuitino.agentemovil.ontologia.InfoMensaje;

public interface IAgenteMobile {
	void handleSpoken(InfoMensaje infoMensaje);
}