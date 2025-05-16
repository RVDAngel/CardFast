package com.example.cardfast.clases;

public class Tarjeta {
    private int id;
    private String titulo;
    private String codigo;
    private double saldo;
    private int idUsuario;
    private String estado;

    public Tarjeta() {
    }
    public Tarjeta(int id, String titulo, String codigo, double saldo) {
        this.id = id;
        this.titulo = titulo;
        this.codigo = codigo;
        this.saldo = saldo;
    }

    public Tarjeta(int id, String titulo, String codigo, double saldo, String estado, int idUsuario) {
        this.id = id;
        this.titulo = titulo;
        this.codigo = codigo;
        this.saldo = saldo;
        this.estado = estado;
        this.idUsuario = idUsuario;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
