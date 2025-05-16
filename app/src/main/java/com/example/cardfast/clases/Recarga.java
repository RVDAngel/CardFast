package com.example.cardfast.clases;

public class Recarga {
    private int id;
    private int idUsuario;
    private int idTarjeta;
    private double monto;
    private String telefono;
    private String metodoPago;
    private String codigoAprobacion;
    private String fecha;
    private String nombreTarjeta;

    public Recarga() {
    }

    public Recarga(int id, int idUsuario, int idTarjeta, double monto, String telefono, String metodoPago, String codigoAprobacion, String fecha, String nombreTarjeta) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idTarjeta = idTarjeta;
        this.monto = monto;
        this.telefono = telefono;
        this.metodoPago = metodoPago;
        this.codigoAprobacion = codigoAprobacion;
        this.fecha = fecha;
        this.nombreTarjeta = nombreTarjeta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdTarjeta() {
        return idTarjeta;
    }

    public void setIdTarjeta(int idTarjeta) {
        this.idTarjeta = idTarjeta;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getCodigoAprobacion() {
        return codigoAprobacion;
    }

    public void setCodigoAprobacion(String codigoAprobacion) {
        this.codigoAprobacion = codigoAprobacion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNombreTarjeta() {
        return nombreTarjeta;
    }

    public void setNombreTarjeta(String nombreTarjeta) {
        this.nombreTarjeta = nombreTarjeta;
    }
}

