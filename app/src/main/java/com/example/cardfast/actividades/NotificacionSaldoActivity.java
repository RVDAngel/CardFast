package com.example.cardfast.actividades;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.cardfast.R;
import com.example.cardfast.clases.BaseAppCompatActivity;
import com.example.cardfast.utils.Preferencias;
import com.example.cardfast.workers.NotificacionSaldoWorker;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.TimeUnit;

public class NotificacionSaldoActivity extends BaseAppCompatActivity implements View.OnClickListener {

    TextInputEditText etCantidadMinima;
    Spinner spinnerFrecuencia;
    Button btnGuardar, btnCancelar;
    Preferencias preferencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notificacion_saldo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        preferencias = new Preferencias(this);

        etCantidadMinima = findViewById(R.id.etCantidadMinima);
        spinnerFrecuencia = findViewById(R.id.spinnerFrecuencia);
        btnGuardar = findViewById(R.id.btnGuardarNotificacion);
        btnCancelar = findViewById(R.id.btnCancelarNotificacion);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.frecuencias_notificacion,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrecuencia.setAdapter(adapter);
        
        float saldoMinimo = preferencias.getSaldoMinimoNotificacion(0f);
        int frecuenciaGuardada = preferencias.getFrecuenciaNotificacion(60);

        if (saldoMinimo > 0f) {
            etCantidadMinima.setText(String.valueOf(saldoMinimo));
        }

        int position = 2;

        switch (frecuenciaGuardada) {
            case 1:
                position = 0;
                break;
            case 5:
                position = 1;
                break;
            case 60:
                position = 2;
                break;
            case 1440:
                position = 3;
                break;
        }
        spinnerFrecuencia.setSelection(position);
        btnGuardar.setOnClickListener(this);
        btnCancelar.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnGuardarNotificacion) {
            guardarConfiguracion();
            crearCanalDeNotificacion();
        } else if (v.getId() == R.id.btnCancelarNotificacion) {
            cancelarConfiguracion();
        }

    }
    private void cancelarConfiguracion() {
        finish();
    }
    private void guardarConfiguracion() {
        String cantidadStr = etCantidadMinima.getText().toString();
        int frecuencia = obtenerMinutosDesdeSpinner(spinnerFrecuencia.getSelectedItem().toString());

        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.mensaje_ingresa_cantidad), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float cantidad = Float.parseFloat(cantidadStr);
            preferencias.setSaldoMinimoNotificacion(cantidad);
            preferencias.setFrecuenciaNotificacion(frecuencia);
            OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(NotificacionSaldoWorker.class)
                    .setInitialDelay(frecuencia, TimeUnit.MINUTES)
                    .build();

            WorkManager.getInstance(this).enqueue(trabajo);
            Toast.makeText(this, getString(R.string.mensaje_configuracion_guardada), Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.mensaje_cantidad_invalida), Toast.LENGTH_SHORT).show();
        }
    }
    private int obtenerMinutosDesdeSpinner(String seleccion) {
        if (seleccion.contains("1 minuto")) return 1;
        if (seleccion.contains("5 minutos")) return 5;
        if (seleccion.contains("1 hora")) return 60;
        if (seleccion.contains("día")) return 1440;
        return 60;
    }
    private void crearCanalDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    getString(R.string.canal_saldo_id),
                    getString(R.string.canal_saldo_nombre),
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription(getString(R.string.canal_saldo_descripcion));
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(canal);
        }
    }
}