package com.example.cardfast.actividades;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cardfast.R;
import com.example.cardfast.clases.BaseAppCompatActivity;
import com.example.cardfast.clases.Hash;
import com.example.cardfast.sqlite.BaseDatos;
import com.example.cardfast.utils.Preferencias;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class RegistroActivity extends BaseAppCompatActivity implements View.OnClickListener {
    private static final String urlMostrarDistritos = "http://bdtransporte.atwebpages.com/ws/mostrarDistritos.php";
    private static final String urlRegistrarUsuario = "http://bdtransporte.atwebpages.com/ws/agregarUsuario.php";

    EditText txtDni, txtNombre, txtApellido, txtFechaNac, txtCorreo, txtClave, txtClave2;
    RadioGroup grpSexo;
    RadioButton rbtSinDefinir, rbtFemenino, rbtMasculino;
    AutoCompleteTextView cboDistritos;
    CheckBox chkTerminos;
    Button btnCrear, btnRegresar;
    BaseDatos db;
    Preferencias preferencias;
    private ArrayAdapter<String> distritosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtDni = findViewById(R.id.regTxtDni);
        txtNombre = findViewById(R.id.regTxtNombre);
        txtApellido = findViewById(R.id.regTxtApellido);
        txtFechaNac = findViewById(R.id.regTxtFechaNac);
        txtCorreo = findViewById(R.id.regTxtCorreo);
        txtClave = findViewById(R.id.regTxtClave);
        txtClave2 = findViewById(R.id.regTxtClave2);

        grpSexo = findViewById(R.id.regGrpSexo);
        rbtSinDefinir = findViewById(R.id.regRbtSinDefinir);
        rbtFemenino = findViewById(R.id.regRbtFemenino);
        rbtMasculino = findViewById(R.id.regRbtMasculino);

        cboDistritos = findViewById(R.id.regCboDistritos);
        chkTerminos = findViewById(R.id.regChkTerminos);

        btnCrear = findViewById(R.id.regBtnCrear);
        btnRegresar = findViewById(R.id.regBtnRegresar);

        txtFechaNac.setOnClickListener(this);
        chkTerminos.setOnClickListener(this);
        btnCrear.setOnClickListener(this);
        btnRegresar.setOnClickListener(this);

        db = new BaseDatos(this);
        preferencias = new Preferencias(this);
        llenarCboDistritos();
    }

    private void llenarCboDistritos() {
        AsyncHttpClient ahcDistritos = new AsyncHttpClient();

        cboDistritos.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[] {}));

        ahcDistritos.get(urlMostrarDistritos, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if(statusCode == 200){
                    try {
                        JSONArray jsonArray = new JSONArray(rawJsonResponse);
                        String[] distritos = new String[jsonArray.length()];
                        for (int i = 0; i < jsonArray.length(); i++) {
                            distritos[i] = jsonArray.getJSONObject(i).getString("nombre_distrito");
                        }
                        distritosAdapter = new ArrayAdapter<>(RegistroActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                distritos);
                        cboDistritos.setAdapter(distritosAdapter);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "ERROR: "+statusCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                Toast.makeText(getApplicationContext(), "ERROR: "+statusCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.regTxtFechaNac)
            mostrarDatePicker();
        else if(v.getId() == R.id.regChkTerminos)
            mostrarTerminos();
        else if(v.getId() == R.id.regBtnCrear)
            crearCuenta();
        else if(v.getId() == R.id.regBtnRegresar)
            regresar();
    }
    private void crearCuenta() {
        int rbtSelecionado;
        Hash hash = new Hash();

        if(!validarFormularo())
            return;

        AsyncHttpClient ahcCuenta = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("dni", txtDni.getText().toString());
        params.put("nombre", txtNombre.getText().toString());
        params.put("apellidos", txtApellido.getText().toString());
        params.put("fecha_nac", txtFechaNac.getText().toString());
        rbtSelecionado = grpSexo.getCheckedRadioButtonId();
        if (rbtSelecionado == R.id.regRbtSinDefinir)
            params.put("sexo", "X");
        else if(rbtSelecionado == R.id.regRbtMasculino)
            params.put("sexo", "M");
        else if(rbtSelecionado == R.id.regRbtFemenino)
            params.put("sexo", "F");
        params.put("correo", txtCorreo.getText().toString());
        params.put("clave", hash.StringToHash(txtClave.getText().toString(), "SHA256").toLowerCase());
        String selectedDistrito = cboDistritos.getText().toString();
        int selectedPosition = distritosAdapter.getPosition(selectedDistrito);
        params.put("id_distrito", selectedPosition);

        ahcCuenta.post(urlRegistrarUsuario, params, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if(statusCode == 200){
                    try {
                        JSONObject json = new JSONObject(rawJsonResponse);
                        String status = json.getString("status");
                        if (status.equalsIgnoreCase("success")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.toast_registro_exitoso), Toast.LENGTH_SHORT).show();
                            Intent iSesion = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(iSesion);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.toast_registro_error), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Respuesta inválida del servidor.", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "ERROR: "+statusCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                Toast.makeText(getApplicationContext(), "ERROR: "+statusCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    private boolean validarFormularo() {
        String selectedDistrito = cboDistritos.getText().toString();
        int selectedPosition = distritosAdapter.getPosition(selectedDistrito);
        if (txtDni.getText().toString().trim().isEmpty() ||
                txtNombre.getText().toString().trim().isEmpty() ||
                txtApellido.getText().toString().trim().isEmpty() ||
                txtFechaNac.getText().toString().trim().isEmpty() ||
                txtCorreo.getText().toString().trim().isEmpty() ||
                txtClave.getText().toString().trim().isEmpty() ||
                txtClave2.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (selectedPosition == 0) {
            Toast.makeText(this, "Por favor seleccione un distrito.", Toast.LENGTH_LONG).show();
            return false;
        }
        String correo = txtCorreo.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, getString(R.string.error_correo), Toast.LENGTH_LONG).show();
            return false;
        }
        String dni = txtDni.getText().toString().trim();
        if (!dni.matches("\\d{8}")) {
            Toast.makeText(this,  getString(R.string.error_dni), Toast.LENGTH_LONG).show();
            return false;
        }
        String clave = txtClave.getText().toString().trim();
        String clave2 = txtClave2.getText().toString().trim();
        if (!clave.equals(clave2)) {
            Toast.makeText(this,  getString(R.string.error_clave_diferente), Toast.LENGTH_LONG).show();
            return false;
        }
        if (!chkTerminos.isChecked()) {
            Toast.makeText(this, getString(R.string.error_terminos), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
    private void mostrarDatePicker() {
        DatePickerDialog dpd;
        final Calendar fechaActual = Calendar.getInstance();
        int dia = fechaActual.get(Calendar.DAY_OF_MONTH);
        int mes = fechaActual.get(Calendar.MONTH);
        int anho = fechaActual.get(Calendar.YEAR);

        dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int y, int m, int d) {
                txtFechaNac.setText(y+"-"+((m+1)< 10?"0"+(m+1):(m+1))+"-"+(d<10?"0"+d:d));
            }
        },anho,mes,dia);
        dpd.show();
    }
    private void regresar() {
        Intent iLogin = new Intent(this, LoginActivity.class);
        startActivity(iLogin);
        finish();
    }
    private void mostrarTerminos() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Términos y Condiciones");

        builder.setMessage("Términos y Condiciones de Uso\n" +
                "Actualizado el 15-05-2025\n" +
                "\n" +
                "Bienvenido a Card Fast. Al acceder y utilizar nuestra aplicación móvil, " +
                "aceptas los siguientes términos y condiciones. Si no estás de acuerdo " +
                "con estos términos, por favor no utilices la app.\n" +
                "\n" +
                "1. Aceptación de los Términos\n" +
                "\n" +
                "Al descargar, instalar o usar Card Fast, aceptas estos términos y condiciones " +
                "y nuestra política de privacidad. Si no aceptas estos términos, no debes usar la app.\n" +
                "\n" +
                "2. Uso de la App\n" +
                "\n" +
                "2.1 Licencia de Uso: Te otorgamos una licencia no exclusiva, intransferible y revocable " +
                "para usar la app en tu dispositivo móvil conforme a estos términos.\n" +
                "\n" +
                "2.2 Restricciones: No puedes modificar, reproducir, distribuir, vender o crear trabajos derivados " +
                "de la app sin nuestro consentimiento previo por escrito. Tampoco debes usar la app para fines ilegales " +
                "o no autorizados.\n" +
                "\n" +
                "3. Registro y Seguridad\n" +
                "\n" +
                "3.1 Cuenta de Usuario: Para acceder a ciertas funciones, debes crear una cuenta proporcionando " +
                "información veraz y completa. Eres responsable de mantener la confidencialidad de tu cuenta y contraseña.\n" +
                "\n" +
                "3.2 Seguridad: Nos reservamos el derecho de suspender o cancelar tu cuenta si sospechamos que se está " +
                "utilizando de manera fraudulenta o en violación de estos términos.\n" +
                "\n" +
                "4. Recargas y Saldos\n" +
                "\n" +
                "Card Fast permite la recarga de tarjetas de transporte y la consulta de saldos. El usuario es responsable " +
                "de verificar la información antes de realizar cualquier transacción.\n" +
                "\n" +
                "5. Propiedad Intelectual\n" +
                "\n" +
                "Todos los derechos de propiedad intelectual sobre la app y su contenido, incluyendo marcas registradas, " +
                "derechos de autor y patentes, pertenecen a Card Fast o a sus licenciantes.\n" +
                "\n" +
                "6. Modificaciones de la App y Términos\n" +
                "\n" +
                "Nos reservamos el derecho de modificar o interrumpir la app en cualquier momento, así como de actualizar " +
                "estos términos. Las modificaciones entrarán en vigor en cuanto se publiquen en la app. Tu uso continuado " +
                "de la app después de dichas modificaciones implica tu aceptación de los nuevos términos.\n" +
                "\n" +
                "7. Limitación de Responsabilidad\n" +
                "\n" +
                "La app se proporciona \"tal cual\" y \"según disponibilidad\". No garantizamos que la app estará libre de " +
                "errores o que funcionará sin interrupciones. En la máxima medida permitida por la ley, no seremos responsables " +
                "de ningún daño indirecto, incidental o consecuente que surja del uso o la imposibilidad de uso de la app.\n" +
                "\n" +
                "8. Ley Aplicable\n" +
                "\n" +
                "Estos términos se rigen por las leyes de Perú. Cualquier disputa que surja en relación con estos términos " +
                "será resuelta en los tribunales competentes de Lima.\n" +
                "\n" +
                "9. Contacto\n" +
                "\n" +
                "Si tienes preguntas sobre estos términos, puedes contactarnos en " +
                "soporte@cardfast.pe o en Av El Sol 461, San Juan de Lurigancho 15434\n" +
                "\n" +
                "10. Terminación\n" +
                "\n" +
                "Podemos suspender o terminar tu acceso a la app si incumples " +
                "estos términos o por cualquier motivo que consideremos necesario " +
                "para proteger la integridad de la app.\n");
        chkTerminos.setChecked(false);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chkTerminos.setChecked(true);
                btnCrear.setEnabled(true);
                dialog.dismiss();
            }
        });
        AlertDialog terminos = builder.create();
        terminos.setCancelable(false);
        terminos.setCanceledOnTouchOutside(false);
        terminos.show();
    }
}