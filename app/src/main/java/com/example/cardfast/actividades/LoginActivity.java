package com.example.cardfast.actividades;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cardfast.MainActivity;
import com.example.cardfast.R;
import com.example.cardfast.clases.BaseAppCompatActivity;
import com.example.cardfast.clases.Hash;
import com.example.cardfast.clases.Usuario;
import com.example.cardfast.sqlite.BaseDatos;
import com.example.cardfast.utils.Preferencias;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends BaseAppCompatActivity implements View.OnClickListener {

    private static final String urlInicioSesion = "http://bdtransporte.atwebpages.com/ws/iniciarSesion.php";
    private static final String urlObtenerFoto = "http://bdtransporte.atwebpages.com/ws/obtenerFotoPerfil.php";

    EditText txtCorreo, txtClave;
    CheckBox checkRecordarSesion;
    Button btnLogin;
    TextView tvRegistrate;
    BaseDatos db;
    Preferencias preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        crearCanalNotificaciones();
        pedirPermisoNotificaciones();
        pedirPermisoAlarmaExacta();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView logo = findViewById(R.id.logoApp);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.logo_entrada);
        logo.startAnimation(anim);
        db = new BaseDatos(this);
        preferencias = new Preferencias(this);

        txtCorreo = findViewById(R.id.txtCorreo);
        txtClave = findViewById(R.id.txtClave);
        checkRecordarSesion = findViewById(R.id.checkRecordarSesion);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegistrate = findViewById(R.id.tvRegistrate);

        btnLogin.setOnClickListener(this);
        tvRegistrate.setOnClickListener(this);
        
        checkRecordarSesion.setChecked(preferencias.isSesionIniciada());
        if (preferencias.isSesionIniciada()) {
            irAPantallaPrincipal();
        }
    }
    private void pedirPermisoAlarmaExacta() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM},
                        102
                );
            }
        }
    }
    private void irAPantallaPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnLogin)
            iniciarSesion(txtCorreo.getText().toString(), txtClave.getText().toString());
        else if(v.getId() == R.id.tvRegistrate)
            crearCuenta();
    }
    private void crearCuenta() {
        Intent iRegistro = new Intent(this, RegistroActivity.class);
        startActivity(iRegistro);
    }
    private void iniciarSesion(String correo, String clave) {
        correo = txtCorreo.getText().toString().trim();
        clave = txtClave.getText().toString().trim();

        if (correo.isEmpty() || clave.isEmpty()) {
            Toast.makeText(this, getString(R.string.mensaje_completar_campos), Toast.LENGTH_SHORT).show();
            return;
        }
        
        Hash hash = new Hash();
        String claveHash = hash.StringToHash(clave, "SHA-256").toLowerCase();
        
        AsyncHttpClient ahcIniciarSesion = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("correo", correo);
        params.put("clave", claveHash);

        ahcIniciarSesion.post(urlInicioSesion, params, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if (statusCode == 200) {
                    try {
                        JSONArray jsonArray = new JSONArray(rawJsonResponse);
                        if (jsonArray.length() == 0) {
                            Toast.makeText(getApplicationContext(), getString(R.string.mensaje_credenciales_incorrectas), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONObject obj = jsonArray.getJSONObject(0);
                        Usuario usuario = new Usuario();
                        usuario.setId(obj.getInt("id_usuario"));
                        usuario.setDni(obj.getString("dni"));
                        usuario.setNombre(obj.getString("nombre"));
                        usuario.setApellidos(obj.getString("apellidos"));
                        usuario.setCorreo(obj.getString("correo"));
                        usuario.setClave(claveHash);
                        usuario.setSexo(obj.getString("sexo").charAt(0));
                        usuario.setFoto_perfil(obj.getString("foto_perfil"));
                        usuario.setIdDistrito(Integer.parseInt(obj.getString("id_distrito")));

                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                        Date fechaNac = dateFormat.parse(jsonArray.getJSONObject(0).getString("fecha_nac"));
                        usuario.setFechaNac(fechaNac);

                        db.registrarUsuario(
                                usuario.getId(),
                                usuario.getDni(),
                                usuario.getNombre(),
                                usuario.getApellidos(),
                                obj.getString("fecha_nac"),
                                usuario.getCorreo(),
                                usuario.getClave(),
                                String.valueOf(usuario.getSexo()),
                                obj.getString("id_distrito")
                        );

                        preferencias.guardarDatosUsuario(usuario.getNombre(), usuario.getApellidos(), usuario.getCorreo(), usuario.getId(), usuario.getFoto_perfil());
                        preferencias.setRecordarSesion(checkRecordarSesion.isChecked());
                        Toast.makeText(LoginActivity.this, getString(R.string.mensaje_bienvenida, usuario.getNombre()), Toast.LENGTH_SHORT).show();
                        irAPantallaPrincipal();
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this, getString(R.string.mensaje_credenciales_incorrectas), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                Toast.makeText(LoginActivity.this, "Error de conexión: " + statusCode, Toast.LENGTH_SHORT).show();
            }
            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) {
                return null;
            }
        });
    }
    private void obtenerFotoPerfilDesdeWS(final Usuario usuario, final Runnable onComplete) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("correo", usuario.getCorreo());
        client.post(urlObtenerFoto, params, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if (statusCode == 200) {
                    try {
                        JSONObject jsonResponse = new JSONObject(rawJsonResponse);
                        String fotoPerfil = jsonResponse.getString("foto_perfil");
                        usuario.setFoto_perfil(fotoPerfil);
                        preferencias.setFotoPerfil(fotoPerfil);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                onComplete.run();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                onComplete.run();
            }
            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) {
                return null;
            }
        });
    }
    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.canal_recargas_nombre);
            String description = getString(R.string.canal_recargas_descripcion);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("recarga_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

    }
    private void pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }
}