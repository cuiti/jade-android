package chat.client.gui;

import java.util.logging.Level;

import chat.client.agent.ChatClientAgent;
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
	private ServiceConnection serviceConnection;

	static final int CHAT_REQUEST = 0;
	static final int SETTINGS_REQUEST = 1;

	private MyReceiver myReceiver;
	private MyHandler myHandler;

	private TextView infoTextView;
	private EditText campoDireccionIP;
	private EditText campoNumeroPuerto;

	private String nickname;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		myReceiver = new MyReceiver();

		IntentFilter killFilter = new IntentFilter();
		killFilter.addAction("jade.demo.chat.KILL");
		registerReceiver(myReceiver, killFilter);

		IntentFilter showChatFilter = new IntentFilter();
		showChatFilter.addAction("jade.demo.chat.SHOW_CHAT");
		registerReceiver(myReceiver, showChatFilter);

		myHandler = new MyHandler();

		setContentView(R.layout.main);

		Button button = (Button) findViewById(R.id.button_chat);
		button.setOnClickListener(buttonChatListener);

		infoTextView = (TextView) findViewById(R.id.infoTextView);
		infoTextView.setText("");	//este texto es el que se usa para avisar cuando se está conectando

		//carga de las preferencias almacenadas
		SharedPreferences settings = getSharedPreferences("jadePreferencesFile",0);

		String host = settings.getString("defaultHost", "");
		String puerto = settings.getString("defaultPort", "");

		campoDireccionIP = (EditText) findViewById(R.id.direccionIP);
		campoDireccionIP.setText(host);

		campoNumeroPuerto = (EditText) findViewById(R.id.numeroDePuerto);
		campoNumeroPuerto.setText(puerto);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myReceiver);

		logger.log(Level.INFO, "Destroy activity!");
	}

	private OnClickListener buttonChatListener = new OnClickListener() {
		public void onClick(View v) {

			//persistencia de los campos puerto e IP
			SharedPreferences settings = getSharedPreferences("jadePreferencesFile", 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("defaultHost", campoDireccionIP.getText().toString());
			editor.putString("defaultPort", campoNumeroPuerto.getText().toString());
			editor.commit();


			//final EditText nameField = (EditText) findViewById(R.id.edit_nickname);
			nickname = Build.BRAND + "-" + Build.DEVICE+(int)Math.floor(Math.random() * 999)+1;
			//el nombre de usuario se genera con marca + modelo + entero random

			try {
				String host = settings.getString("defaultHost", "");
				String port = settings.getString("defaultPort", "");
				infoTextView.setText(getString(R.string.msg_connecting_to)
						+ " " + host + ":" + port + "...");
				startChat(nickname, host, port, agentStartupCallback);
			} catch (Exception ex) {
				logger.log(Level.SEVERE, "Unexpected exception creating chat agent!");
				infoTextView.setText(getString(R.string.msg_unexpected));
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHAT_REQUEST) {
			if (resultCode == RESULT_CANCELED) {
				// The chat activity was closed.
				infoTextView.setText("");
				logger.log(Level.INFO, "Stopping Jade...");
				microRuntimeServiceBinder
						.stopAgentContainer(new RuntimeCallback<Void>() {
							@Override
							public void onSuccess(Void thisIsNull) {
							}

							@Override
							public void onFailure(Throwable throwable) {
								logger.log(Level.SEVERE, "Failed to stop the "
										+ ChatClientAgent.class.getName()
										+ "...");
								agentStartupCallback.onFailure(throwable);
							}
						});
			}
		}
	}

	private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {
		@Override
		public void onSuccess(AgentController agent) {
		}

		@Override
		public void onFailure(Throwable throwable) {
			logger.log(Level.INFO, "Nickname already in use!");
			myHandler.postError(getString(R.string.msg_nickname_in_use));
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
			if (action.equalsIgnoreCase("jade.demo.chat.KILL")) {
				finish();
			}
			if (action.equalsIgnoreCase("jade.demo.chat.SHOW_CHAT")) {
				Intent showChat = new Intent(MainActivity.this,
						ChatActivity.class);
				showChat.putExtra("nickname", nickname);
				MainActivity.this
						.startActivityForResult(showChat, CHAT_REQUEST);
			}
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			if (bundle.containsKey("error")) {
				infoTextView.setText("");
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

	public void startChat(final String nickname, final String host,
			final String port,
			final RuntimeCallback<AgentController> agentStartupCallback) {

		final Properties profile = new Properties();
		profile.setProperty(Profile.MAIN_HOST, host);
		profile.setProperty(Profile.MAIN_PORT, port);
		profile.setProperty(Profile.MAIN, Boolean.FALSE.toString());
		profile.setProperty(Profile.JVM, Profile.ANDROID);

		if (AndroidHelper.isEmulator()) {
			// Emulator: this is needed to work with emulated devices
			profile.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
		} else {
			profile.setProperty(Profile.LOCAL_HOST,
					AndroidHelper.getLocalIPAddress());
		}
		// Emulator: this is not really needed on a real device
		profile.setProperty(Profile.LOCAL_PORT, "2000");

		if (microRuntimeServiceBinder == null) {
			serviceConnection = new ServiceConnection() {
				public void onServiceConnected(ComponentName className,
						IBinder service) {
					microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
					logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
					startContainer(nickname, profile, agentStartupCallback);
				};

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
			startContainer(nickname, profile, agentStartupCallback);
		}
	}

	private void startContainer(final String nickname, Properties profile,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		if (!MicroRuntime.isRunning()) {
			microRuntimeServiceBinder.startAgentContainer(profile,
					new RuntimeCallback<Void>() {
						@Override
						public void onSuccess(Void thisIsNull) {
							logger.log(Level.INFO, "Successfully start of the container...");
							startAgent(nickname, agentStartupCallback);
						}

						@Override
						public void onFailure(Throwable throwable) {
							logger.log(Level.SEVERE, "Failed to start the container...");
						}
					});
		} else {
			startAgent(nickname, agentStartupCallback);
		}
	}

	private void startAgent(final String nickname,
			final RuntimeCallback<AgentController> agentStartupCallback) {
		microRuntimeServiceBinder.startAgent(nickname,
				ChatClientAgent.class.getName(),
				new Object[] { getApplicationContext() },
				new RuntimeCallback<Void>() {
					@Override
					public void onSuccess(Void thisIsNull) {
						logger.log(Level.INFO, "Successfully start of the "
								+ ChatClientAgent.class.getName() + "...");
						try {
							agentStartupCallback.onSuccess(MicroRuntime
									.getAgent(nickname));
						} catch (ControllerException e) {
							// Should never happen
							agentStartupCallback.onFailure(e);
						}
					}

					@Override
					public void onFailure(Throwable throwable) {
						logger.log(Level.SEVERE, "Failed to start the "
								+ ChatClientAgent.class.getName() + "...");
						agentStartupCallback.onFailure(throwable);
					}
				});
	}

}