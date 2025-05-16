package com.example.cardfast.fragmentos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.example.cardfast.R;
import com.example.cardfast.adaptadores.TarjetaAdapter;
import com.example.cardfast.clases.BaseAppCompatFragment;
import com.example.cardfast.clases.Tarjeta;
import com.example.cardfast.sqlite.BaseDatos;
import com.example.cardfast.utils.Preferencias;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class TarjetaFragment extends BaseAppCompatFragment implements View.OnClickListener {
    private final static String urlMostrarTarjetas = "http://bdtransporte.atwebpages.com/ws/listarTarjetasUsuario.php";
    private final static String urlRegistrarTarjeta = "http://bdtransporte.atwebpages.com/ws/registrarTarjeta.php";
    private final static String urlRegistrarRecarga = "http://bdtransporte.atwebpages.com/ws/registrarRecarga.php";
    private final static String urlActualizarSaldo = "http://bdtransporte.atwebpages.com/ws/actualizarSaldoTarjeta.php";
    private final static String urlEliminarTarjeta = "http://bdtransporte.atwebpages.com/ws/eliminarTarjeta.php";
    private final static String urlActualizarTarjeta = "http://bdtransporte.atwebpages.com/ws/actualizarTarjeta.php";
    FloatingActionButton mainFabBtn, editFabBtn, deleteFabBtn;
    RecyclerView recyclerView;
    BaseDatos db;
    Preferencias preferencias;
    TarjetaAdapter adapter;
    ArrayList<Tarjeta> listaTarjetas = new ArrayList<>();
    ArrayList<AlertDialog> dialogosAbiertos = new ArrayList<>();

    boolean fabAbierto = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TarjetaFragment() {
        // Required empty public constructor
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TarjetaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TarjetaFragment newInstance(String param1, String param2) {
        TarjetaFragment fragment = new TarjetaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_tarjeta, container, false);

        mainFabBtn = vista.findViewById(R.id.mainFabBtn);
        editFabBtn = vista.findViewById(R.id.editFabBtn);
        deleteFabBtn = vista.findViewById(R.id.deleteFabBtn);
        recyclerView = vista.findViewById(R.id.recyclerView);

        mainFabBtn.setOnClickListener(this);
        editFabBtn.setOnClickListener(this);
        deleteFabBtn.setOnClickListener(this);

        editFabBtn.hide();
        deleteFabBtn.hide();

        db = new BaseDatos(getContext());
        int idUsuario = new Preferencias(getContext()).getIdUsuario();
        listaTarjetas = new ArrayList<>(db.obtenerTarjetasPorUsuario(idUsuario));

        if (listaTarjetas.isEmpty()) {
            mostrarTarjetasUsuario(idUsuario);
        }
        adapter = new TarjetaAdapter(listaTarjetas, new TarjetaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Tarjeta seleccionada = adapter.getItem(position);
                Toast.makeText(getContext(), "Seleccionaste: " + seleccionada.getTitulo(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVerMasClick(int position) {
                mostrarDetalleTarjeta(adapter.getItem(position));
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setAdapter(adapter);

        return vista;
    }
    private void mostrarTarjetasUsuario(int idUsuario) {
        AsyncHttpClient ahcListarTarjeta = new AsyncHttpClient();
        String url = urlMostrarTarjetas + "?id_usuario=" + idUsuario;
        ahcListarTarjeta.get(url, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if(statusCode == 200){
                    try {
                        JSONArray jsonArray = new JSONArray(rawJsonResponse);
                        listaTarjetas.clear();
                        db = new BaseDatos(getContext());

                        for (int i = 0; i < jsonArray.length(); i++) {
                            int idTarjeta = jsonArray.getJSONObject(i).getInt("id_tarjeta");
                            String nombre = jsonArray.getJSONObject(i).getString("nombre");
                            String numero = jsonArray.getJSONObject(i).getString("numero_tarjeta");
                            double saldo = jsonArray.getJSONObject(i).getDouble("saldo");
                            String titulo = "Tarjeta " + nombre;
                            String estado = "activa";

                            Tarjeta tarjeta = new Tarjeta(idTarjeta, nombre, numero, saldo, estado, idUsuario);
                            listaTarjetas.add(tarjeta);
                            db.insertarTarjeta(idTarjeta, nombre, numero, saldo, idUsuario);
                        }

                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error al procesar tarjetas", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getContext(), "ERROR: " +statusCode, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                Toast.makeText(getContext(), "Error de conexión: " + statusCode, Toast.LENGTH_SHORT).show();
            }
            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) {
                return null;
            }
        });
    }
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.mainFabBtn) {
            if (fabAbierto) {
                editFabBtn.hide();
                deleteFabBtn.hide();
            } else {
                editFabBtn.show();
                deleteFabBtn.show();
            }
            fabAbierto = !fabAbierto;
        } else if (id == R.id.editFabBtn) {
            int pos = adapter.getSelectedPosition();
            if (pos == -1) {
                mostrarDialogo(null, -1);
            } else {
                mostrarDialogo(adapter.getItem(pos), pos);
            }
        } else if (id == R.id.deleteFabBtn) {
            int pos = adapter.getSelectedPosition();
            if (pos != -1) {
                Tarjeta tarjeta = adapter.getItem(pos);
                db = new BaseDatos(getContext());
                AsyncHttpClient clientEliminar = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("id_tarjeta", tarjeta.getId());

                clientEliminar.post(urlEliminarTarjeta, params, new BaseJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                        db.eliminarTarjeta(tarjeta.getId());
                        adapter.eliminarItem(pos);
                        Toast.makeText(getContext(), "Tarjeta eliminada correctamente", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                        Log.e("WS_ELIMINAR", "Error al eliminar tarjeta: " + rawJsonData);
                        Toast.makeText(getContext(), "Error al eliminar en servidor", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected Object parseResponse(String rawJsonData, boolean isFailure) {
                        return null;
                    }
                });
            }
        }
    }
    private void mostrarDialogo(Tarjeta tarjeta, int posicion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(tarjeta == null ? getString(R.string.titulo_nueva_tarjeta) : getString(R.string.titulo_editar_tarjeta));

        View view = LayoutInflater.from(requireContext()).inflate(R.layout.mostrar_dialog, null);

        EditText inputTitulo = view.findViewById(R.id.editTitulo);
        EditText inputCodigo = view.findViewById(R.id.editCodigo);
        EditText inputSaldo = view.findViewById(R.id.editSaldo);
        
        if (tarjeta != null) {
            inputTitulo.setText(tarjeta.getTitulo());
            inputCodigo.setText(tarjeta.getCodigo());
            inputSaldo.setText(String.valueOf(tarjeta.getSaldo()));
        }

        builder.setView(view);
        builder.setPositiveButton(getString(R.string.boton_guardar), (dialog, which) -> {
            String titulo = inputTitulo.getText().toString().trim();
            String codigo = inputCodigo.getText().toString().trim();
            String saldoTexto = inputSaldo.getText().toString().trim();

            if (titulo.isEmpty() || codigo.isEmpty() || saldoTexto.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.toast_campos_obligatorios), Toast.LENGTH_SHORT).show();
                return;
            }

            double saldo;
            try {
                saldo = Double.parseDouble(saldoTexto);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), getString(R.string.toast_saldo_invalido), Toast.LENGTH_SHORT).show();
                return;
            }

            int idTarjeta = tarjeta != null ? tarjeta.getId() : 0;

            Tarjeta nueva = new Tarjeta(idTarjeta, titulo, codigo, saldo);

            if (posicion == -1) {
                db = new BaseDatos(getContext());
                int idUsuario = new Preferencias(getContext()).getIdUsuario();
                AsyncHttpClient ahcRegistrarTarjeta = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                params.put("nombre", titulo);
                params.put("numero_tarjeta", codigo);
                params.put("saldo", saldo);
                params.put("id_usuario", idUsuario);

                ahcRegistrarTarjeta.post(urlRegistrarTarjeta, params, new BaseJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                        if(statusCode == 200){
                            try {
                                JSONObject json = new JSONObject(rawJsonResponse);
                                boolean success = json.getBoolean("success");

                                if (success) {
                                    int idTarjeta = json.getInt("id_tarjeta");
                                    db.insertarTarjeta(idTarjeta, titulo, codigo, saldo, idUsuario);
                                    adapter.agregarItem(nueva);
                                    Toast.makeText(getContext(), "Tarjeta registrada con éxito", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Error en el servidor", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(getContext(), "ERROR: " +statusCode, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                        Toast.makeText(getContext(), "Error de conexión al registrar tarjeta", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected Object parseResponse(String rawJsonData, boolean isFailure) {
                        return null;
                    }
                });
            } else {
                db = new BaseDatos(getContext());
                AsyncHttpClient clientEditar = new AsyncHttpClient();
                RequestParams paramsEditar = new RequestParams();
                paramsEditar.put("id_tarjeta", tarjeta.getId());
                paramsEditar.put("nombre", titulo);
                paramsEditar.put("numero_tarjeta", codigo);
                paramsEditar.put("saldo", saldo);

                clientEditar.post(urlActualizarTarjeta, paramsEditar, new BaseJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                        // Actualizar en SQLite
                        db.actualizarTarjeta(tarjeta.getId(), titulo, codigo, saldo);
                        // Actualizar en la lista y notificar al adaptador
                        tarjeta.setTitulo(titulo);
                        tarjeta.setCodigo(codigo);
                        tarjeta.setSaldo(saldo);
                        adapter.editarItem(posicion, tarjeta);

                        Toast.makeText(getContext(), "Tarjeta actualizada correctamente", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                        Log.e("WS_EDITAR", "Error al actualizar tarjeta: " + rawJsonData);
                        Toast.makeText(getContext(), "Error al actualizar en servidor", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected Object parseResponse(String rawJsonData, boolean isFailure) {
                        return null;
                    }
                });
            }
        });

        builder.setNegativeButton(getString(R.string.boton_cancelar), null);
        AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }
    private void mostrarNotificacion(Context context, String titulo, String mensaje) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "recarga_channel")
                .setSmallIcon(R.drawable.ic_person)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
    private void mostrarDetalleTarjeta(Tarjeta tarjeta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_detalle_tarjeta, null);

        TextView titulo = view.findViewById(R.id.txtTituloDetalle);
        TextView codigo = view.findViewById(R.id.txtCodigoDetalle);
        TextView saldo = view.findViewById(R.id.txtSaldoDetalle);
        ImageButton btnToggle = view.findViewById(R.id.btnToggleSaldo);
        Button btnRecargar = view.findViewById(R.id.btnRecargarSaldo);

        titulo.setText(tarjeta.getTitulo());
        codigo.setText(getString(R.string.codigo) + ": " + tarjeta.getCodigo());

        // Inicializamos el saldo como oculto
        saldo.setText(getString(R.string.saldo_oculto));
        btnToggle.setImageResource(R.drawable.ic_visibility_off);

        // Configuramos el comportamiento del botón para alternar la visibilidad del saldo
        final boolean[] visible = {false};
        btnToggle.setOnClickListener(v -> {
            if (visible[0]) {
                saldo.setText(getString(R.string.saldo_oculto));
                btnToggle.setImageResource(R.drawable.ic_visibility_off);
            } else {
                saldo.setText(getString(R.string.saldo_mostrado, tarjeta.getSaldo()));
                btnToggle.setImageResource(R.drawable.ic_visibility);
            }
            visible[0] = !visible[0];
        });

        btnRecargar.setOnClickListener(v -> {
            mostrarDialogoRecarga(tarjeta);
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialogosAbiertos.add(dialog);
        dialog.show();
    }
    private void mostrarDialogoRecarga(Tarjeta tarjeta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_recarga_saldo, null);

        EditText inputMonto = view.findViewById(R.id.edit_monto_recarga);
        RadioButton rbYape = view.findViewById(R.id.radio_yape);
        Button btnPagar = view.findViewById(R.id.btn_continuar_pago);

        TextView textSaldoActual = view.findViewById(R.id.text_saldo_actual);

        textSaldoActual.setText(getString(R.string.saldo_actual, tarjeta.getSaldo()));

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnPagar.setOnClickListener(v -> {
            String montoStr = inputMonto.getText().toString();
            if (montoStr.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.ingrese_monto_valido), Toast.LENGTH_SHORT).show();
                return;
            }

            double monto = Double.parseDouble(montoStr);
            if (!rbYape.isChecked()) {
                Toast.makeText(getContext(), getString(R.string.seleccionar_metodo_pago), Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            mostrarDialogoPagoFinal(tarjeta, monto);
        });
    }
    private void mostrarDialogoPagoFinal(Tarjeta tarjeta, double monto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_pago_final, null);

        EditText inputTelefono = view.findViewById(R.id.edit_celular_yape);
        EditText inputCodigo = view.findViewById(R.id.edit_codigo_aprobacion);
        Button btnConfirmar = view.findViewById(R.id.btn_yapear);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);

        btnConfirmar.setText(getString(R.string.yapear_monto, monto));

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialogosAbiertos.add(dialog);
        dialog.show();

        btnConfirmar.setOnClickListener(v -> {
            String telefono = inputTelefono.getText().toString().trim();
            String codigo = inputCodigo.getText().toString().trim();

            // Validaciones
            if (telefono.isEmpty()) {
                inputTelefono.setError(getString(R.string.ingrese_numero_yape));
                inputTelefono.requestFocus();
                return;
            }

            if (!telefono.matches("\\d{9}")) {
                inputTelefono.setError(getString(R.string.numero_invalido));
                inputTelefono.requestFocus();
                return;
            }

            if (codigo.isEmpty()) {
                inputCodigo.setError(getString(R.string.ingrese_codigo_aprobacion));
                inputCodigo.requestFocus();
                return;
            }

            if (!codigo.matches("\\d{4,6}")) {
                inputCodigo.setError(getString(R.string.codigo_invalido));
                inputCodigo.requestFocus();
                return;
            }

            // Simulación de procesamiento de pago
            progressBar.setVisibility(View.VISIBLE);
            btnConfirmar.setEnabled(false);

            new Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                btnConfirmar.setEnabled(true);

                tarjeta.setSaldo(tarjeta.getSaldo() + monto);
                db = new BaseDatos(getContext());
                db.actualizarTarjeta(tarjeta.getId(), tarjeta.getTitulo(), tarjeta.getCodigo(), tarjeta.getSaldo());

                AsyncHttpClient clientActualizar = new AsyncHttpClient();
                RequestParams saldoParams = new RequestParams();
                saldoParams.put("id_tarjeta", tarjeta.getId());
                saldoParams.put("saldo", tarjeta.getSaldo());

                clientActualizar.post(urlActualizarSaldo, saldoParams, new BaseJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                    }

                    @Override
                    protected Object parseResponse(String rawJsonData, boolean isFailure) {
                        return null;
                    }
                });

                String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                String metodoPago = "Yape";
                if (monto <= 0) {
                    Toast.makeText(getContext(), getString(R.string.monto_mayor_que_0), Toast.LENGTH_SHORT).show();
                    return;
                }
                AsyncHttpClient client = new AsyncHttpClient();
                RequestParams params = new RequestParams();
                int idUsuario = new Preferencias(getContext()).getIdUsuario();

                params.put("id_usuario", idUsuario);
                params.put("id_tarjeta", tarjeta.getId());
                params.put("monto", monto);
                params.put("telefono", telefono);
                params.put("metodo_pago", metodoPago);
                params.put("codigo_aprobacion", codigo);
                params.put("fecha", fecha);

                client.post(urlRegistrarRecarga, params, new BaseJsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                        if (statusCode == 200) {
                            try {
                                JSONObject json = new JSONObject(rawJsonResponse);
                                if (json.getBoolean("success")) {
                                    int idRecarga = json.getInt("id_recarga");

                                    String nombreTarjeta = json.optString("nombre_tarjeta", "");

                                    // Guarda la recarga en SQLite local
                                    BaseDatos db = new BaseDatos(getContext());
                                    db.insertarRecarga(
                                            idRecarga,
                                            idUsuario,
                                            tarjeta.getId(),
                                            monto,
                                            telefono,
                                            metodoPago,
                                            codigo,
                                            fecha,
                                            nombreTarjeta
                                    );

                                    Toast.makeText(getContext(), "Recarga guardada correctamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Error: " + json.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Error procesando respuesta", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "ERROR: " + statusCode, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                        Log.e("WS_RECARGA_ERROR", "WS Error: " + statusCode + " - " + rawJsonData);
                        Toast.makeText(getContext(), "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected Object parseResponse(String rawJsonData, boolean isFailure) {
                        return null;
                    }
                });

                preferencias = new Preferencias(getContext());

                if (preferencias.getNotificaciones(false)) {
                    mostrarNotificacion(
                            getContext(),
                            getString(R.string.recarga_exitosa),
                            getString(R.string.recarga_mensaje, monto, tarjeta.getCodigo())
                    );
                }
                Toast.makeText(getContext(), getString(R.string.recarga_exitosa), Toast.LENGTH_LONG).show();
                dialog.dismiss();

                mostrarDialogoRecibo(tarjeta, monto, telefono, fecha);
            }, 2000);
        });
    }
    private void mostrarDialogoRecibo(Tarjeta tarjeta, double monto, String telefono, String fecha) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_recibo, null);
        
        TextView tituloView = view.findViewById(R.id.text_recibo_titulo);
        TextView fechaView = view.findViewById(R.id.text_recibo_fecha);
        TextView montoView = view.findViewById(R.id.text_recibo_monto);
        TextView telefonoView = view.findViewById(R.id.text_recibo_telefono);
        TextView metodoView = view.findViewById(R.id.text_recibo_metodo);
        TextView tarjetaView = view.findViewById(R.id.text_recibo_tarjeta);
        Button btnFinalizar = view.findViewById(R.id.btn_finalizar);

        tituloView.setText(tarjeta.getTitulo());
        fechaView.setText(getString(R.string.dialogo_recibo_fecha, fecha));
        montoView.setText(getString(R.string.dialogo_recibo_monto, monto));
        telefonoView.setText(getString(R.string.dialogo_recibo_telefono, telefono));
        metodoView.setText(getString(R.string.dialogo_recibo_metodo));
        tarjetaView.setText(getString(R.string.dialogo_recibo_tarjeta, tarjeta.getCodigo()));

        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialogosAbiertos.add(dialog);
        dialog.show();

        // Configurar el evento del botón "Finalizar"
        btnFinalizar.setOnClickListener(v -> {
            // Cerrar todos los diálogos abiertos
            for (AlertDialog d : dialogosAbiertos) {
                if (d.isShowing()) {
                    d.dismiss();
                }
            }
            dialogosAbiertos.clear();  // Limpiar la lista de diálogos después de cerrarlos
        });

    }
}