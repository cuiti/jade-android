package bolinocuitino.agentemovil.agent;

import java.io.Serializable;

public interface IAgenteMovil {
	void handleSpoken(Serializable s);
	//String[] getParticipantNames();
}