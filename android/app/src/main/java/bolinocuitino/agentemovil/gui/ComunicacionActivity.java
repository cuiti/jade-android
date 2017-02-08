package bolinocuitino.agentemovil.gui;

import java.io.RandomAccessFile;
import java.util.Date;
import java.util.logging.Level;

import bolinocuitino.agentemovil.agentes.IAgenteMobile;
import bolinocuitino.agentemovil.ontologia.InfoMensaje;
import jade.core.MicroRuntime;
import jade.util.Logger;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.Toast;

public class ComunicacionActivity extends Activity {
	private Logger logger = Logger.getJADELogger(this.getClass().getName());

	private MyReceiver myReceiver;

	private String nombreDispositivo;
	private IAgenteMobile interfazAgente;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			nombreDispositivo = extras.getString("nombreDispositivo");
		}

		try {
			interfazAgente = MicroRuntime.getAgent(nombreDispositivo)
					.getO2AInterface(IAgenteMobile.class);
		} catch (StaleProxyException e) {
			Toast.makeText(this,"Error interno al crear Runtime JADE",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (ControllerException e) {
			Toast.makeText(this,"Error interno al crear Runtime JADE",Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		interfazAgente.obtenerActivity(this);

		myReceiver = new MyReceiver();

		IntentFilter actualizarInformacionFilter = new IntentFilter();
		actualizarInformacionFilter.addAction("bolinocuitino.agentemovil.ACTUALIZAR");
		registerReceiver(myReceiver, actualizarInformacionFilter);

		setContentView(R.layout.info);

		Button boton_salir = (Button) findViewById(R.id.boton_salir);
		boton_salir.setOnClickListener(botonSalirListener);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(myReceiver);
		logger.log(Level.INFO, "Se destruye el activity");
	}

	protected void onStop() {
		super.onStop();
		interfazAgente.detenerEnvioDeInformacion();
		interfazAgente.avisoDeSalida();
		logger.log(Level.INFO,"Se ejecutó el Stop del activity");
	}

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
			if (action.equalsIgnoreCase("bolinocuitino.agentemovil.ACTUALIZAR")) {
				final TextView campoDeTexto = (TextView) findViewById(R.id.textViewPrincipal);
				campoDeTexto.append(intent.getExtras().getString("informacion"));
				scrollDown();
			}
		}
	}

	private void scrollDown() {
		final ScrollView scroller = (ScrollView) findViewById(R.id.scroller);
		final TextView campoDeTexto = (TextView) findViewById(R.id.textViewPrincipal);
		scroller.smoothScrollTo(0, campoDeTexto.getBottom());
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		final TextView campoDeTexto = (TextView) findViewById(R.id.textViewPrincipal);
		savedInstanceState.putString("campoDeTexto", campoDeTexto.getText()
				.toString());
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		final TextView campoDeTexto = (TextView) findViewById(R.id.textViewPrincipal);
		campoDeTexto.setText(savedInstanceState.getString("campoDeTexto"));
	}

	public InfoMensaje obtenerInformacionDelDispositivo() {
        InfoMensaje infoMensaje = new InfoMensaje();

        infoMensaje.setMensaje("Configurar un mensaje");
        infoMensaje.setFecha(new Date());
        infoMensaje.setNombreHardware(Build.HARDWARE);
        infoMensaje.setSDKversionNumber(Integer.parseInt(Build.VERSION.SDK));
        infoMensaje.setNombreDisplay(Build.DISPLAY);
        infoMensaje.setNombreMarcaModelo(Build.BOARD + " " + Build.BRAND + " " + Build.MODEL);
        infoMensaje.setUltimoSMS(getSmsMasReciente());
		infoMensaje.setPorcentajeUsoCpu(usoDeCpu());

        TelephonyManager telephonyManager =(TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);

		String operador = telephonyManager.getNetworkOperatorName();

		if(operador != null && !operador.isEmpty())
			infoMensaje.setOperadorDeTelefono(operador);

		Location geolocalizacion = getInformacionGeolocalizacion();

		if(geolocalizacion != null) {
			infoMensaje.setLatitud(geolocalizacion.getLatitude());
			infoMensaje.setLongitud(geolocalizacion.getLongitude());
			infoMensaje.setAltitud(geolocalizacion.getAltitude());
		}

		return infoMensaje;
	}

	private String getSmsMasReciente() {
		String smsMessage = "";
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
		if (cursor.moveToFirst()) {
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

	private float usoDeCpu() {
		try {
			RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
			String load = reader.readLine();

			String[] toks = load.split(" +");  // Split on one or more spaces

			long idle1 = Long.parseLong(toks[4]);
			long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			try {
				Thread.sleep(360);
			} catch (Exception e) {}

			reader.seek(0);
			load = reader.readLine();
			reader.close();

			toks = load.split(" +");

			long idle2 = Long.parseLong(toks[4]);
			long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
					+ Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

			return (float)(cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return 0;
	}

	private double getMemoriaLibre(){
		//devuelve el uso de memoria en megabytes
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		activityManager.getMemoryInfo(mi);
		return mi.availMem / 0x100000L;
	}

}