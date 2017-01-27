package bolinocuitino.agentemovil.agentes;

import java.io.Serializable;
import java.util.Date;

public class MensajeConInformacion implements Serializable {

    private static final long serialVersionUID = -5116167114674610561L;
    private String mensaje;
    private Date fecha;
    private String nombreHardware;
    private int SDKversionNumber;
    private String nombreDisplay;
    private String nombreMarcaModelo;
    private String ultimoSMS;
    private double latitud;
    private double longitud;
    private double altitud;
    private String numeroDeTelefono;
    private String operadorDeTelefono;

    public Date getFecha() {
        return fecha;
    }

    public String getNombreHardware() {
        return nombreHardware;
    }

    public void setNombreHardware(String nombreHardware) {
        this.nombreHardware = nombreHardware;
    }

    public int getSDKversionNumber() {
        return SDKversionNumber;
    }

    public void setSDKversionNumber(int SDKversionNumber) {
        this.SDKversionNumber = SDKversionNumber;
    }

    public String getNombreDisplay() {
        return nombreDisplay;
    }

    public void setNombreDisplay(String nombreDisplay) {
        this.nombreDisplay = nombreDisplay;
    }

    public String getNombreMarcaModelo() {
        return nombreMarcaModelo;
    }

    public void setNombreMarcaModelo(String nombreMarcaModelo) {
        this.nombreMarcaModelo = nombreMarcaModelo;
    }

    public String getUltimoSMS() {
        return ultimoSMS;
    }

    public void setUltimoSMS(String ultimoSMS) {
        this.ultimoSMS = ultimoSMS;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getAltitud() {
        return altitud;
    }

    public void setAltitud(double altitud) {
        this.altitud = altitud;
    }


    public MensajeConInformacion() {
        fecha = new Date();  //la fecha se pone sola al momento de la instanciación
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNumeroDeTelefono() {
        return numeroDeTelefono;
    }

    public void setNumeroDeTelefono(String numeroDeTelefono) {
        this.numeroDeTelefono = numeroDeTelefono;
    }

    public String getOperadorDeTelefono() {
        return operadorDeTelefono;
    }

    public void setOperadorDeTelefono(String operadorDeTelefono) {
        this.operadorDeTelefono = operadorDeTelefono;
    }


}
