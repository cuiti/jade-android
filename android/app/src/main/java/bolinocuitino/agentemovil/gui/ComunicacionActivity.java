package bolinocuitino.agentemovil.gui;

import java.util.Date;
import java.util.logging.Level;

import bolinocuitino.agentemovil.agentes.IAgenteMovil;
import bolinocuitino.agentemovil.agentes.MensajeConInformacion;
import jade.core.MicroRuntime;
import jade.util.Logger;
import jade.wrapper.ControllerException;
import jade.wrapper.O2AException;
import jade.wrapper.StaleProxyException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class ComunicacionActivity extends Activity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private MyReceiver myReceiver;

	private String nombreDispositivo;
	private IAgenteMovil interfazAgente;
	private String DISPOSITIVO_MARCA_MODELO = Build.BRAND + " " + Build.DEVICE;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			nombreDispositivo = extras.getString("nombreDispositivo");
		}

		try {
			interfazAgente = MicroRuntime.getAgent(nombreDispositivo)
					.getO2AInterface(IAgenteMovil.class);
		} catch (StaleProxyException e) {
			showAlertDialog(getString(R.string.msg_interface_exc), true);
		} catch (ControllerException e) {
			showAlertDialog(getString(R.string.msg_controller_exc), true);
		}

		myReceiver = new MyReceiver();

		IntentFilter refreshChatFilter = new IntentFilter();
		refreshChatFilter.addAction("jade.demo.chat.REFRESH_CHAT");
		registerReceiver(myReceiver, refreshChatFilter);

		IntentFilter clearChatFilter = new IntentFilter();
		clearChatFilter.addAction("jade.demo.chat.CLEAR_CHAT");
		registerReceiver(myReceiver, clearChatFilter);

		setContentView(R.layout.chat);

		Button button = (Button) findViewById(R.id.button_send);
		button.setOnClickListener(botonEnviarListener);

		enviarInformacionDelDispositivo();

		Button boton_salir = (Button) findViewById(R.id.boton_salir);
		boton_salir.setOnClickListener(botonSalirListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
		logger.log(Level.INFO, "Destroy activity!");
	}

	protected void onStop() {
		super.onStop();
		/*interfazAgente.handleSpoken("---------------------------------------------- \n" +
							"El dispositivo " + DISPOSITIVO_MARCA_MODELO + " ha salido del sistema \n" +
							"-----------------------------------------------");*/
	}

	private OnClickListener botonEnviarListener = new OnClickListener() {
		public void onClick(View v) {
			//Boton para volver a enviar los datos
			enviarInformacionDelDispositivo();
		}
	};

	private OnClickListener botonSalirListener = new OnClickListener() {
		public void onClick(View v) {
			//Boton para salir de la app
			moveTaskToBack(true);
			finish();
		}
	};

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			logger.log(Level.INFO, "Received intent " + action);
			if (action.equalsIgnoreCase("jade.demo.chat.REFRESH_CHAT")) {
				final TextView chatField = (TextView) findViewById(R.id.chatTextView);
				chatField.append(intent.getExtras().getString("sentence"));
				scrollDown();
			}
			if (action.equalsIgnoreCase("jade.demo.chat.CLEAR_CHAT")) {
				final TextView chatField = (TextView) findViewById(R.id.chatTextView);
				chatField.setText("");
			}
		}
	}

	private void scrollDown() {
		final ScrollView scroller = (ScrollView) findViewById(R.id.scroller);
		final TextView chatField = (TextView) findViewById(R.id.chatTextView);
		scroller.smoothScrollTo(0, chatField.getBottom());
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		final TextView chatField = (TextView) findViewById(R.id.chatTextView);
		savedInstanceState.putString("chatField", chatField.getText()
				.toString());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		final TextView chatField = (TextView) findViewById(R.id.chatTextView);
		chatField.setText(savedInstanceState.getString("chatField"));
	}

	private void showAlertDialog(String message, final boolean fatal) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				ComunicacionActivity.this);
		builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog, int id) {
								dialog.cancel();
								if (fatal) finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void enviarInformacionDelDispositivo() {
		String ubicacionInfo = "";
		String mensaje = "";
		Date fecha = new Date();
		String smsMsgData = getSMSdata();
		Location geolocalizacion = getInformacionGeolocalizacion();

		TelephonyManager tMgr =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
		String telefono = tMgr.getLine1Number();
		if (telefono==null) telefono = "No se pudo obtener el número de teléfono";

		if (geolocalizacion != null) {
			ubicacionInfo += "Latitud: " + geolocalizacion.getLatitude() + " \n"
					+ "Longitud: " + geolocalizacion.getLongitude() + " \n"
					+ "Altitud: " + geolocalizacion.getAltitude() + " \n"
					+ "Extras: " + geolocalizacion.getExtras() + " \n";
		} else {
			ubicacionInfo += "No se pueden obtener datos de geolocalización";
		}

		try {

			mensaje += "----- " + DISPOSITIVO_MARCA_MODELO + " -----\n"
					+ "Fecha: "+fecha.toLocaleString()+ " \n"
					+ "ID: " + Build.ID + " \n"
					+ "Teléfono: "+ telefono + " \n"
					+ "Hardware name: " + Build.HARDWARE + " \n"
					+ "SDK version: " + Build.VERSION.SDK + " \n"
					+ "Display: " + Build.DISPLAY + " \n"
					+ "-------  Ultimo SMS recibido  -------:  \n"
					+ smsMsgData + " \n"
					+ ubicacionInfo + " \n"
					+ " -------------------------------------";
			/*//envia las coordenadas por separado para mostrarlas en el mapa
			if (geolocalizacion != null) { //location puede ser null cuando el usuario dehabilita la geolocalizacion
				String latitude = String.valueOf(geolocalizacion.getLatitude());
				String longitude = String.valueOf(geolocalizacion.getLongitude());
				interfazAgente.handleSpoken(latitude + "#" + longitude);
			}*/

			MensajeConInformacion datos = new MensajeConInformacion();
			datos.setMensaje("holis");
			interfazAgente.handleSpoken(datos);



		} catch (O2AException e) {
			showAlertDialog(e.getMessage(), false);
		}

	}

	private String getSMSdata() {
		String smsMessage = "";
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		if (cursor.moveToFirst()) { // must check the result to prevent exception
			//do {
			smsMessage += cursor.getString(cursor.getColumnIndexOrThrow("address")) + " \n";
			smsMessage += cursor.getString(cursor.getColumnIndexOrThrow("body")) + " \n";
			//for(int idx=0;idx<cursor.getColumnCount();idx++)
			//{
			//smsMessage += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx)+ " \n";
			//}
			//} while (cursor.moveToNext());	//comento el while para que levante solo el 1er SMS
		} else {
			Log.d("SMS", "no se encontraron sms");
			smsMessage = "No se encontraron SMS";
		}
		cursor.close();
		return smsMessage;
	}

	private Location getInformacionGeolocalizacion() {
		//String locationInfo = "";
		Context context = getApplicationContext();
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();

		int currentapiVersion = Build.VERSION.SDK_INT;
		if (currentapiVersion >= 14) {
			criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setAltitudeRequired(true);
			criteria.setBearingRequired(true);
			criteria.setSpeedRequired(true);
		}
		String provider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(provider);
		return location;
	}

}