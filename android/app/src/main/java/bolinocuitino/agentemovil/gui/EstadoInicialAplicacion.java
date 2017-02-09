package bolinocuitino.agentemovil.gui;

import java.util.logging.Level;

import jade.util.Logger;
import android.app.Application;
import android.content.SharedPreferences;

public class EstadoInicialAplicacion extends Application {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences settings = getSharedPreferences("jadePreferencesFile", 0);
				
		String defaultHost = settings.getString("defaultHost", "");
		String defaultPort = settings.getString("defaultPort", "");
		String intervalo = settings.getString("intervaloEnvio","");

		if (defaultHost.isEmpty() || defaultPort.isEmpty() || intervalo.isEmpty()) {
			logger.log(Level.INFO, "Create default properties");
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("defaultHost", "192.168.0.108");
			editor.putString("defaultPort", "1099");
			editor.putString("intervaloEnvio","5000");
			editor.commit();
		}
	}
}
