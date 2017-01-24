package bolinocuitino.agentemovil.agent;

public interface IAgenteMovil {
	void handleSpoken(String s);
	String[] getParticipantNames();
}