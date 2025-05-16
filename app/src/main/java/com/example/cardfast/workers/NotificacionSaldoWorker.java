package com.example.cardfast.workers;

import android.app.NotificationManager;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.cardfast.R;
import com.example.cardfast.clases.Tarjeta;
import com.example.cardfast.sqlite.BaseDatos;
import com.example.cardfast.utils.LocaleHelper;
import com.example.cardfast.utils.Preferencias;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificacionSaldoWorker extends Worker {

    private Context context;

    public NotificacionSaldoWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = LocaleHelper.actualizarIdioma(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        Preferencias preferencias = new Preferencias(context);
        float cantidadMinima = preferencias.getSaldoMinimoNotificacion(10f);

        BaseDatos bd = new BaseDatos(context);
        List<Tarjeta> tarjetasBajoSaldo = bd.obtenerTarjetasConSaldoBajo(cantidadMinima);

        if (!tarjetasBajoSaldo.isEmpty()) {
            StringBuilder detalleTarjetas = new StringBuilder();
            for (Tarjeta tarjeta : tarjetasBajoSaldo) {
                detalleTarjetas.append("- ")
                        .append(tarjeta.getTitulo())
                        .append(" (S/ ")
                        .append(String.format("%.2f", tarjeta.getSaldo()))
                        .append(")\n");
            }

            String mensaje = context.getString(R.string.mensaje_saldo_bajo) + "\n" + detalleTarjetas.toString().trim();
            mostrarNotificacion(context.getString(R.string.mensaje_tarjetas_bajo_saldo), mensaje);
        }
        int frecuenciaGuardada = preferencias.getFrecuenciaNotificacion(60);

        OneTimeWorkRequest nuevoTrabajo = new OneTimeWorkRequest.Builder(NotificacionSaldoWorker.class)
                .setInitialDelay(frecuenciaGuardada, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(nuevoTrabajo);

        return Result.success();
    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "saldo_bajo")
                .setSmallIcon(R.drawable.ic_warning)
                .setContentTitle(titulo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
