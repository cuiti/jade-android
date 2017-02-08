package bolinocuitino.agentemovil.agentes;

import bolinocuitino.agentemovil.gui.ComunicacionActivity;
import bolinocuitino.agentemovil.ontologia.InfoMensaje;

public interface IAgenteMobile {
	void obtenerActivity(ComunicacionActivity comunicacionActivity);
    InfoMensaje obtenerInformacion(ComunicacionActivity comunicacionActivity);
    void detenerEnvioDeInformacion();
}