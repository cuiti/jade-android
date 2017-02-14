package bolinocuitino.agentemovil.ontologia;

import java.util.Date;

import jade.content.Predicate;

public class InfoMensaje implements Predicate{
    private static final long serialVersionUID = 6672632206985267350L;
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
    private String operadorDeTelefono;
    private double porcentajeUsoCpu;
    private double memoriaLibre;
    private double nivelBateria;

    public InfoMensaje() {
        this.setMensaje("Mensaje por defecto");
        this.setFecha(new Date());
        this.setNombreHardware("Hardware por defecto");;
        this.setSDKversionNumber(0);
        this.setNombreDisplay("Display por defecto");
        this.setNombreMarcaModelo("Marca y Modelo por defecto");
        this.setUltimoSMS("No hay ultimo SMS");
        this.setLatitud(0);
        this.setLongitud(0);
        this.setAltitud(0);
        this.setOperadorDeTelefono("No se pudo obtener el operador");
        this.setPorcentajeUsoCpu(0);
        this.setMemoriaLibre(0);
        this.setNivelBateria(0);
    }

    @Override
    public String toString() {
        return  "Nombre: " + this.getNombreMarcaModelo() + '\n' + "Fecha: " + this.getFecha().toString() + '\n' +
                "Hardware: " + this.getNombreHardware() + '\n' + "SDK: " + this.getSDKversionNumber() + '\n' +
                "Display: " + this.getNombreDisplay() + '\n' + "Operador: " + this.getOperadorDeTelefono() + '\n' +
                "Nivel de Bateria: " + this.nivelBateriaToString() + '\n' + "Uso de CPU: " + this.porcentajeUsoCpuToString() + '\n' +
                "Memoria Libre: " + this.memoriaLibreToString() + '\n' + "Latitud: " + this.getLatitud() + '\n' +
                "Longitud: " + this.getLongitud() + '\n' + "Altitud: " + this.getAltitud() + '\n'+
                "Mensaje: " + this.getMensaje() + '\n' + "Ultimo SMS: " + this.getUltimoSMS() + '\n';
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getOperadorDeTelefono() {
        return operadorDeTelefono;
    }

    public void setOperadorDeTelefono(String operadorDeTelefono) {
        this.operadorDeTelefono = operadorDeTelefono;
    }

    public double getPorcentajeUsoCpu() {
        return porcentajeUsoCpu;
    }

    public void setPorcentajeUsoCpu(double porcentajeUsoCpu) {
        this.porcentajeUsoCpu = porcentajeUsoCpu;
    }

    public String porcentajeUsoCpuToString() {
        return String.format("%.2f", this.getPorcentajeUsoCpu() * 100) + " %";
    }

    public double getMemoriaLibre() {
        return memoriaLibre;
    }

    public void setMemoriaLibre(double memoriaLibre) {
        this.memoriaLibre = memoriaLibre;
    }

    public String memoriaLibreToString() {
        return this.getMemoriaLibre() + " MB";
    }

    public double getNivelBateria() {
        return nivelBateria;
    }

    public void setNivelBateria(double nivelBateria) {
        this.nivelBateria = nivelBateria;
    }

    public String nivelBateriaToString() {
        return this.getNivelBateria() + " %";
    }
}
