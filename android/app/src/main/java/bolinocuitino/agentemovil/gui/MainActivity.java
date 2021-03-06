package bolinocuitino.agentemovil.gui;

import java.util.logging.Level;

import bolinocuitino.agentemovil.agentes.AgenteMobile;
import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private MicroRuntimeServiceBinder microRuntimeServiceBinder;
	private ServiceConnection serviceConnection;  //para usar el servicio de android

	private MyReceiver myReceiver;
	private MyHandler myHandler;

	private TextView informacionTextView;
	private EditText campoDireccionIP;
	private EditText campoNumeroPuerto;
	private EditText campoIntervalo;

	private String nombreDispositivo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myReceiver = new MyReceiver();

		IntentFilter iniciarComunicacionFilter = new IntentFilter();
		iniciarComunicacionFilter.addAction("bolinocuitino.agentemovil.INICIAR_COMUNICACION");
		registerReceiver(myReceiver, iniciarComunicacionFilter);

		myHandler = new MyHandler();

		setContentView(R.layout.main);

		Button button = (Button) findViewById(R.id.boton_iniciar);
		button.setOnClickListener(botonComenzarListener);

		informacionTextView = (TextView) findViewById(R.id.infoTextView);
		informacionTextView.setText("");	//este texto es el que se usa para avisar cuando se está conectando

		//carga de las preferencias almacenadas
		SharedPreferences settings = getSharedPreferences("jadePreferencesFile",0);

		String host = settings.getString("defaultHost", "");
		String puerto = settings.getString("defaultPort", "");
		String intervalo = settings.getString("intervaloEnvio","");

		campoDireccionIP = (EditText) findViewById(R.id.direccionIP);
		campoDireccionIP.setText(host);

		campoNumeroPuerto = (EditText) findViewById(R.id.numeroDePuerto);
		campoNumeroPuerto.setText(puerto);

		campoIntervalo = (EditText) findViewById(R.id.campoIntervalo);
		campoIntervalo.setText(intervalo);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myReceiver);

		logger.log(Level.INFO, "Se destruye el activity");
	}

	protected void onResume(){
		super.onResume();
		informacionTextView.setText(" ");
		logger.log(Level.INFO, "Se ejecuta el resume del activity");
	}

	private OnClickListener botonComenzarListener = new OnClickListener() {
		public void onClick(View v) {
			//el nombre de usuario se genera con marca + modelo + entero random
			nombreDispositivo = Build.BRAND + "-" + Build.DEVICE+(int)Math.floor(Math.random() * 999)+1;

			//persistencia de los campos puerto e IP
			SharedPreferences settings = getSharedPreferences("jadePreferencesFile", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("defaultHost", campoDireccionIP.getText().toString());
			editor.putString("defaultPort", campoNumeroPuerto.getText().toString());
			editor.putString("intervaloEnvio",campoIntervalo.getText().toString());
			editor.commit();

			try {
				String host = settings.getString("defaultHost", "");
				String port = settings.getString("defaultPort", "");
				informacionTextView.setText(getString(R.string.msg_connecting_to)
						+ " " + host + ":" + port + "...");
				iniciarComunicacion(nombreDispositivo, host, port, agentStartupCallback);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "excepcion al inicializar el agente!");
				informacionTextView.setText(getString(R.string.msg_unexpected));
			}
		}
	};

	private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
		@Override
		public void onSuccess(AgentController agent) {
		}

		@Override
		public void onFailure(Throwable throwable) {
			logger.log(Level.INFO, "Error al inicializar el agente!");
			myHandler.postError("Error al inicializar el agente!");
		}
	};

	public void ShowDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase("bolinocuitino.agentemovil.INICIAR_COMUNICACION")) {
				Intent iniciarComunicacion = new Intent(MainActivity.this,
						ComunicacionActivity.class);
				iniciarComunicacion.putExtra("nombreDispositivo", nombreDispositivo);
				startActivity(iniciarComunicacion);
			}
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			if (bundle.containsKey("error")) {
				informacionTextView.setText("");
				String message = bundle.getString("error");
				ShowDialog(message);
			}
		}

		public void postError(String error) {
			Message msg = obtainMessage();
			Bundle b = new Bundle();
			b.putString("error", error);
			msg.setData(b);
			sendMessage(msg);
		}
	}

	public void iniciarComunicacion(final String nombreDispositivo, final String host,
									final String port,
									final RuntimeCallback<AgentController> agentStartupCallback) {

		//la clase Properties es de JADE LEAP para guardar informacion clave valor
		//Profile es de JADE core, se usa para las propiedades de un container

		final Properties perfil = new Properties();
		perfil.setProperty(Profile.MAIN_HOST, host);
		perfil.setProperty(Profile.MAIN_PORT, port);
		perfil.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		perfil.setProperty(Profile.JVM, Profile.ANDROID);

		if (AndroidHelper.isEmulator()) {
			// Emulator: this is needed to work with emulated devices
			perfil.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
		} else {
			perfil.setProperty(Profile.LOCAL_HOST,
					AndroidHelper.getLocalIPAddress());
		}
		// Emulator: this is not really needed on a real device
		perfil.setProperty(Profile.LOCAL_PORT, "2000");

		if (microRuntimeServiceBinder == null) {
			serviceConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName className,
						IBinder service) {
					microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
					logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
					startContainer(nombreDispositivo, perfil, agentStartupCallback);
				}

				public void onServiceDisconnected(ComponentName className) {
					microRuntimeServiceBinder = null;
					logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");
				}
			};
			logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");
			bindService(new Intent(getApplicationContext(),
					MicroRuntimeService.class), serviceConnection,
					Context.BIND_AUTO_CREATE);
		} else {
			logger.log(Level.INFO, "MicroRumtimeGateway already binded to service");
			startContainer(nombreDispositivo, perfil, agentStartupCallback);
		}
	}

	private void startContainer(final String nombreDispositivo, Properties profile,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			microRuntimeServiceBinder.startAgentContainer(profile,
					new RuntimeCallback<Void>() {
						@Override
						public void onSuccess(Void thisIsNull) {
							logger.log(Level.INFO, "Successfully start of the container...");
							startAgent(nombreDispositivo, agentStartupCallback);
						}

						@Override
						public void onFailure(Throwable throwable) {
							logger.log(Level.SEVERE, "Failed to start the container...");
						}
					});
		} else {
			startAgent(nombreDispositivo, agentStartupCallback);
		}
	}

	private void startAgent(final String nombreDispositivo,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		microRuntimeServiceBinder.startAgent(nombreDispositivo,
				AgenteMobile.class.getName(),
				new Object[] { getApplicationContext() },
				new RuntimeCallback<Void>() {
					@Override
					public void onSuccess(Void thisIsNull) {
						logger.log(Level.INFO, "Inicio correcto del agente "
								+ AgenteMobile.class.getName() + "...");
						try {
							agentStartupCallback.onSuccess(MicroRuntime
									.getAgent(nombreDispositivo));
						} catch (ControllerException e) {
							// Should never happen
							agentStartupCallback.onFailure(e);
						}
					}

					@Override
					public void onFailure(Throwable throwable) {
						logger.log(Level.SEVERE, "Fallo al iniciar "
								+ AgenteMobile.class.getName() + "...");
						agentStartupCallback.onFailure(throwable);
					}
				});
	}

}