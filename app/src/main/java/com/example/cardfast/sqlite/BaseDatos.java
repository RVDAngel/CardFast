package com.example.cardfast.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.cardfast.clases.Recarga;
import com.example.cardfast.clases.Tarjeta;
import com.example.cardfast.clases.Usuario;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BaseDatos extends SQLiteOpenHelper {

    private static final String DB_NAME = "cardfast.db";
    private static final int DB_VERSION = 1;

    public BaseDatos(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE distrito (" +
                "id_distrito INTEGER PRIMARY KEY, " +
                "nombre_distrito TEXT)");
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY," +
                "dni TEXT," +
                "nombre TEXT," +
                "apellido TEXT," +
                "fecha_nacimiento TEXT," +
                "correo TEXT," +
                "clave TEXT," +
                "sexo TEXT," +
                "distrito TEXT,"+
                "FOREIGN KEY (distrito) REFERENCES distrito(id_distrito))");

        db.execSQL("CREATE TABLE recargas (" +
                "id INTEGER PRIMARY KEY, " +
                "id_usuario INTEGER NOT NULL, " +
                "id_tarjeta INTEGER NOT NULL, " +
                "monto REAL NOT NULL, " +
                "telefono TEXT NOT NULL, " +
                "metodo_pago TEXT NOT NULL, " +
                "codigo_aprobacion TEXT NOT NULL, " +
                "fecha TEXT NOT NULL, " +
                "nombre_tarjeta TEXT, " +
                "FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_tarjeta) REFERENCES tarjetas(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE tarjetas (" +
                "id INTEGER PRIMARY KEY, " +
                "nombre TEXT, " +
                "codigo TEXT, " +
                "saldo REAL, " +
                "estado TEXT DEFAULT 'activa', " +
                "id_usuario INTEGER NOT NULL, " +
                        "FOREIGN KEY (id_usuario) REFERENCES usuarios(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        db.execSQL("DROP TABLE IF EXISTS recargas");
        db.execSQL("DROP TABLE IF EXISTS tarjetas");
        onCreate(db);
    }

    // ---------- MÉTODOS USUARIOS ----------
    public boolean registrarUsuario(int id_usuario, String dni, String nombre, String apellido, String fechaNac,
                                    String correo, String clave, String sexo, String distrito) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id_usuario);
        values.put("dni", dni);
        values.put("nombre", nombre);
        values.put("apellido", apellido);
        values.put("fecha_nacimiento", fechaNac);
        values.put("correo", correo);
        values.put("clave", clave);
        values.put("sexo", sexo);
        values.put("distrito", distrito);

        long resultado = db.insert("usuarios", null, values);
        return resultado != -1;
    }

    public int obtenerUltimoIdUsuario() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM usuarios", null);
        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }
        cursor.close();
        return -1;
    }

    public boolean validarLogin(String correo, String clave) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM usuarios WHERE correo=? AND clave=?", new String[]{correo, clave});
        boolean existe = cursor.moveToFirst();
        cursor.close();
        return existe;
    }

    public String obtenerNombreUsuario(String correo) {
        if (correo == null) {
            Log.e("BaseDatos", "Correo es null, no se puede buscar en la base de datos.");
            return null;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT nombre || ' ' || apellido AS nombre_completo FROM usuarios WHERE correo = ?",
                new String[]{correo}
        );

        if (cursor != null && cursor.moveToFirst()) {
            String nombreCompleto = cursor.getString(0);
            cursor.close();
            return nombreCompleto;
        }
        return null;
    }
    public boolean actualizarUsuario(int idUsuario, String nombre, String apellidos, String correo, String claveNueva) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("apellido", apellidos);
        values.put("correo", correo);

        if (claveNueva != null && !claveNueva.isEmpty()) {
            values.put("clave", claveNueva);
        }

        int filasActualizadas = db.update("usuarios", values, "id = ?", new String[]{String.valueOf(idUsuario)});
        db.close();
        return filasActualizadas > 0;
    }
    public String obtenerFotoPerfil(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT fotoPerfil FROM usuarios WHERE correo = ?", new String[]{correo});

        if (cursor.moveToFirst()) {
            String fotoPerfil = cursor.getString(cursor.getColumnIndexOrThrow("fotoPerfil"));
            cursor.close();
            return fotoPerfil;
        }

        cursor.close();
        return null;
    }
    public Usuario obtenerUsuarioPorCorreo(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM usuarios WHERE correo=?", new String[]{correo});
        if (cursor.moveToFirst()) {
            Usuario usuario = new Usuario();
            usuario.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            usuario.setDni(cursor.getString(cursor.getColumnIndexOrThrow("dni")));
            usuario.setNombre(cursor.getString(cursor.getColumnIndexOrThrow("nombre")));
            usuario.setApellidos(cursor.getString(cursor.getColumnIndexOrThrow("apellido")));
            usuario.setCorreo(cursor.getString(cursor.getColumnIndexOrThrow("correo")));
            usuario.setClave(cursor.getString(cursor.getColumnIndexOrThrow("clave")));
            usuario.setSexo(cursor.getString(cursor.getColumnIndexOrThrow("sexo")).charAt(0));
            usuario.setIdDistrito(Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow("distrito"))));
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                usuario.setFechaNac(df.parse(cursor.getString(cursor.getColumnIndexOrThrow("fecha_nacimiento"))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            cursor.close();
            return usuario;
        }
        cursor.close();
        return null;
    }

    public boolean eliminarUsuario(int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        int filas = db.delete("usuarios", "id = ?", new String[]{String.valueOf(idUsuario)});
        db.close();
        return filas > 0;
    }

    // ---------- MÉTODOS RECARGAS ----------
    public long insertarRecarga(int idRecarga, int idUsuario, int idTarjeta, double monto, String telefono, String metodoPago, String codigoAprobacion, String fecha, String nombreTarjeta) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("id", idRecarga);
        values.put("id_usuario", idUsuario);
        values.put("id_tarjeta", idTarjeta);
        values.put("monto", monto);
        values.put("telefono", telefono);
        values.put("metodo_pago", metodoPago);
        values.put("codigo_aprobacion", codigoAprobacion);
        values.put("fecha", fecha);
        values.put("nombre_tarjeta", nombreTarjeta);

        long result = db.insertWithOnConflict("recargas", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result;
    }
    public List<Recarga> obtenerRecargasUsuario(int idUsuario) {
        List<Recarga> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM recargas WHERE id_usuario = ?", new String[]{String.valueOf(idUsuario)});

        if (cursor.moveToFirst()) {
            do {
                Recarga r = new Recarga(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        cursor.getDouble(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre_tarjeta"))
                );
                lista.add(r);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // ---------- MÉTODOS TARJETAS ----------

    public long insertarTarjeta(int idTarjetaWS, String nombre, String codigo, double saldo, int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", idTarjetaWS);
        values.put("nombre", nombre);
        values.put("codigo", codigo);
        values.put("saldo", saldo);
        values.put("id_usuario", idUsuario);

        long result = db.insert("tarjetas", null, values);
        db.close();
        return result;
    }

    public List<Tarjeta> obtenerTarjetasPorUsuario(int idUsuario) {
        List<Tarjeta> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tarjetas WHERE id_usuario = ?", new String[]{String.valueOf(idUsuario)});
        if (cursor.moveToFirst()) {
            do {
                Tarjeta tarjeta = new Tarjeta(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                        cursor.getString(cursor.getColumnIndexOrThrow("codigo")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("saldo")),
                        cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario"))
                );
                lista.add(tarjeta);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public void actualizarSaldo(String codigo, double nuevoSaldo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("saldo", nuevoSaldo);
        db.update("tarjetas", valores, "codigo=?", new String[]{codigo});
    }

    public List<Tarjeta> obtenerTarjetas() {
        List<Tarjeta> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tarjetas ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                Tarjeta tarjeta = new Tarjeta(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3)
                );
                lista.add(tarjeta);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }
    public void actualizarTarjeta(int id, String nombre, String codigo, double saldo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("codigo", codigo);
        valores.put("saldo", saldo);
        db.update("tarjetas", valores, "id = ?", new String[]{String.valueOf(id)});
    }
    public void eliminarTarjeta(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("tarjetas", "id = ?", new String[]{String.valueOf(id)});
    }

    public List<Tarjeta> obtenerTarjetasConSaldoBajo(double cantidadMinima) {
        List<Tarjeta> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM tarjetas WHERE saldo <= ?", new String[]{String.valueOf(cantidadMinima)});
        if (cursor.moveToFirst()) {
            do {
                Tarjeta tarjeta = new Tarjeta(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getDouble(3)
                );
                lista.add(tarjeta);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }
}
