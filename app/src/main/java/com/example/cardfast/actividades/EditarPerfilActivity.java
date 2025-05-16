package com.example.cardfast.actividades;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cardfast.R;
import com.example.cardfast.clases.BaseAppCompatActivity;
import com.example.cardfast.clases.Hash;
import com.example.cardfast.sqlite.BaseDatos;
import com.example.cardfast.utils.Preferencias;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cz.msebera.android.httpclient.Header;

public class EditarPerfilActivity extends BaseAppCompatActivity implements View.OnClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String urlActualizarUsuario = "http://bdtransporte.atwebpages.com/ws/actualizarUsuario.php";
    private static final String urlObtenerFoto = "http://bdtransporte.atwebpages.com/ws/obtenerFotoPerfil.php";

    Toolbar toolbar;
    ShapeableImageView ivFotoPerfil;
    FloatingActionButton btnCambiarFoto;
    TextInputEditText etNombre, etApellidos, etCorreo, etClaveActual, etNuevaClave;
    private Button btnGuardar;
    private ProgressDialog progressDialog;
    Preferencias preferencias;
    BaseDatos db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toolbar = findViewById(R.id.toolbarEditarPerfil);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        ivFotoPerfil = findViewById(R.id.ivFotoPerfil);
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);

        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etCorreo = findViewById(R.id.etCorreo);
        etClaveActual = findViewById(R.id.etClaveActual);
        etNuevaClave = findViewById(R.id.etNuevaClave);

        btnCambiarFoto.setOnClickListener(this);
        btnGuardar.setOnClickListener(this);
        
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Actualizando perfil...");
        progressDialog.setCancelable(false);

        cargarDatosUsuario();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnCambiarFoto)
            abrirGaleria();
        else if(v.getId() == R.id.btnGuardar)
            actualizarPerfil();
    }

    private void cargarDatosUsuario() {
        db = new BaseDatos(this);
        preferencias = new Preferencias(this);
        int idUsuario = preferencias.getIdUsuario();
        if (idUsuario != -1) {
            String nombre = preferencias.getNombreUsuario();
            String apellidos = preferencias.getApellidos();
            String correo = preferencias.getCorreoUsuario();
            String fotoBase64 = preferencias.getFotoPerfil();
            
            etNombre.setText(nombre);
            etApellidos.setText(apellidos);
            etCorreo.setText(correo);
            
            if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                byte[] decodedString = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivFotoPerfil.setImageBitmap(decodedByte);
            }else {
                obtenerFotoDesdeWS(correo);
            }
        } else {
            Toast.makeText(this, "No se ha encontrado el ID del usuario", Toast.LENGTH_SHORT).show();
        }
    }

    private void obtenerFotoDesdeWS(String correo) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("correo", correo);

        client.post(urlObtenerFoto, params, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if (statusCode == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(rawJsonResponse);
                        String fotoBase64 = jsonObject.getString("foto_perfil");

                        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                            preferencias.setFotoPerfil(fotoBase64);
                            byte[] imageBytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT);
                            Bitmap decodedBitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            ivFotoPerfil.setImageBitmap(decodedBitmap);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                Toast.makeText(getApplicationContext(), "Error de conexión: " + statusCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) {
                return null;
            }
        });
    }
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ivFotoPerfil.setImageURI(data.getData());
        }
    }
    private void actualizarPerfil() {
        preferencias = new Preferencias(this);
        int idUsuario = preferencias.getIdUsuario();

        String claveActual = etClaveActual.getText().toString().trim();
        String nuevaClave = etNuevaClave.getText().toString().trim();
        String claveActualHash = "";
        String nuevaClaveHash = "";

        boolean cambiarClave = !nuevaClave.isEmpty();

        if (cambiarClave && claveActual.isEmpty()) {
            Toast.makeText(this, "Ingrese la clave actual para cambiar la contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cambiarClave) {
            Hash hash = new Hash();
            claveActualHash = hash.StringToHash(claveActual, "SHA-256");
            nuevaClaveHash = hash.StringToHash(nuevaClave, "SHA-256");
        }

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("nombre", etNombre.getText().toString());
        params.put("apellidos", etApellidos.getText().toString());
        params.put("correo", etCorreo.getText().toString());
        params.put("foto_perfil", imageViewToBase64(ivFotoPerfil));
        params.put("id_usuario", String.valueOf(idUsuario));
        
        if (cambiarClave) {
            params.put("clave_actual", claveActualHash);
            params.put("nueva_clave", nuevaClaveHash);
        }
        
        final String claveFinal = cambiarClave ? nuevaClaveHash : null;

        progressDialog.show();

        client.post(urlActualizarUsuario, params, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                progressDialog.dismiss();

                if (statusCode == 200) {
                    try {
                        int ret_val = rawJsonResponse.length() == 0 ? 0 : Integer.parseInt(rawJsonResponse);
                        if (ret_val == 1) {
                            Toast.makeText(getApplicationContext(), "Perfil actualizado correctamente.", Toast.LENGTH_SHORT).show();

                            preferencias.guardarDatosUsuario(
                                    etNombre.getText().toString(),
                                    etApellidos.getText().toString(),
                                    etCorreo.getText().toString(),
                                    idUsuario,
                                    imageViewToBase64(ivFotoPerfil)
                            );

                            boolean actualizado = db.actualizarUsuario(
                                    idUsuario,
                                    etNombre.getText().toString(),
                                    etApellidos.getText().toString(),
                                    etCorreo.getText().toString(),
                                    claveFinal
                            );

                            if (!actualizado) {
                                Log.e("BD_LOCAL", "No se pudo actualizar el usuario en la BD local.");
                            }
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error al actualizar perfil.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("RESP_PARSE", "Error al parsear respuesta", e);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "ERROR: " + statusCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error de conexión: " + statusCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }
    private String imageViewToBase64(ShapeableImageView imageView) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}