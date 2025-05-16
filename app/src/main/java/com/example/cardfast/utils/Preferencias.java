package com.example.cardfast.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferencias {

    private static final String PREF_NAME = "mi_app_preferencias";
    private static final String KEY_NOMBRE = "nombre_usuario";
    private static final String KEY_APELLIDOS = "apellidos_usuario";
    private static final String KEY_RECORDAR_SESION = "recordar_sesion";
    private static final String KEY_CORREO = "correo_usuario";
    private static final String KEY_ID = "id_usuario";
    private static final String KEY_MODO_NOCHE = "modo_noche";
    private static final String KEY_NOTIFICACIONES = "notificaciones";
    private static final String KEY_CUENTA_PRIVADA = "cuenta_privada";
    private static final String KEY_SALDO_MINIMO = "saldo_minimo";
    private static final String KEY_FRECUENCIA_NOTIFICACION = "frecuencia_notificacion";
    private static final String KEY_IDIOMA = "idioma_seleccionado";
    private static final String KEY_FOTO_PERFIL = "foto_perfil";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public Preferencias(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ==============================
    // DATOS DE USUARIO
    // ==============================
    public void guardarDatosUsuario(String nombre, String apellidos, String correo, int id, String fotoPerfilBase64) {
        editor.putString(KEY_NOMBRE, nombre);
        editor.putString(KEY_APELLIDOS, apellidos);
        editor.putString(KEY_CORREO, correo);
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_FOTO_PERFIL, fotoPerfilBase64);
        editor.apply();
    }
    public String getNombreUsuario() {
        return prefs.getString(KEY_NOMBRE, null);
    }
    public String getApellidos() {
        return prefs.getString(KEY_APELLIDOS, null);
    }
    public String getCorreoUsuario() {
        return prefs.getString(KEY_CORREO, null);
    }
    public int getIdUsuario() {
        return prefs.getInt(KEY_ID, -1);
    }
    public String getFotoPerfil() {
        return prefs.getString(KEY_FOTO_PERFIL, null);
    }
    public void setFotoPerfil(String fotoBase64) {
        editor.putString(KEY_FOTO_PERFIL, fotoBase64);
        editor.apply();
    }

    // ==============================
    // MÉTODOS DE SESIÓN
    // ==============================
    public void setRecordarSesion(boolean recordar) {
        editor.putBoolean(KEY_RECORDAR_SESION, recordar);
        editor.apply();
    }
    public boolean isSesionIniciada() {
        return prefs.getBoolean(KEY_RECORDAR_SESION, false);
    }
    public void cerrarSesion() {
        editor.clear();
        editor.apply();
    }

    // ==============================
    // CONFIGURACIONES
    // ==============================
    public void setModoNoche(boolean estado) {
        editor.putBoolean(KEY_MODO_NOCHE, estado);
        editor.apply();
    }
    public boolean getModoNoche(boolean defaultValue) {
        return prefs.getBoolean(KEY_MODO_NOCHE, defaultValue);
    }
    public void setNotificaciones(boolean estado) {
        editor.putBoolean(KEY_NOTIFICACIONES, estado);
        editor.apply();
    }
    public boolean getNotificaciones(boolean defaultValue) {
        return prefs.getBoolean(KEY_NOTIFICACIONES, defaultValue);
    }
    public void setSaldoMinimoNotificacion(float valor) {
        editor.putFloat(KEY_SALDO_MINIMO, valor);
        editor.apply();
    }
    public float getSaldoMinimoNotificacion(float defaultValue) {
        return prefs.getFloat(KEY_SALDO_MINIMO, defaultValue);
    }
    public void setFrecuenciaNotificacion(Integer frecuencia) {
        editor.putInt(KEY_FRECUENCIA_NOTIFICACION, frecuencia);
        editor.apply();
    }
    public int getFrecuenciaNotificacion(Integer defaultValue) {
        return prefs.getInt(KEY_FRECUENCIA_NOTIFICACION, defaultValue);
    }
    public void setCuentaPrivada(boolean estado) {
        editor.putBoolean(KEY_CUENTA_PRIVADA, estado);
        editor.apply();
    }
    public boolean getCuentaPrivada(boolean defaultValue) {
        return prefs.getBoolean(KEY_CUENTA_PRIVADA, defaultValue);
    }

    // ==============================
    // MÉTODOS ESTÁTICOS PARA ACCESO RÁPIDO
    // ==============================
    public static boolean obtenerModoOscuro(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_MODO_NOCHE, false);
    }
    public static String obtenerCorreo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_CORREO, null);
    }

    // ==============================
    // METODOS DE IDIOMA
    // ==============================
    public void setIdioma(String idioma) {
        editor.putString(KEY_IDIOMA, idioma);
        editor.apply();
    }
    public String getIdioma(String defaultIdioma) {
        return prefs.getString(KEY_IDIOMA, defaultIdioma);
    }
}
