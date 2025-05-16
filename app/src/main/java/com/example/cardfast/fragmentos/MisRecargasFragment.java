package com.example.cardfast.fragmentos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cardfast.R;
import com.example.cardfast.adaptadores.RecargaAdapter;
import com.example.cardfast.clases.BaseAppCompatFragment;
import com.example.cardfast.clases.Recarga;
import com.example.cardfast.sqlite.BaseDatos;
import com.example.cardfast.utils.Preferencias;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MisRecargasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MisRecargasFragment extends BaseAppCompatFragment {
    private final static String urlListarRecargas = "http://bdtransporte.atwebpages.com/ws/listarRecargasUsuario.php";

    RecyclerView recyclerRecargas;
    RecargaAdapter adapter;
    BaseDatos db;
    Preferencias preferencias;
    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<Recarga> listaRecargas = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MisRecargasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MisRecargasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MisRecargasFragment newInstance(String param1, String param2) {
        MisRecargasFragment fragment = new MisRecargasFragment();
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
        // Inflate the layout for this fragment
        View vista = inflater.inflate(R.layout.fragment_mis_recargas, container, false);
        swipeRefreshLayout = vista.findViewById(R.id.swipeRefreshLayout);
        recyclerRecargas = vista.findViewById(R.id.recyclerRecargas);
        recyclerRecargas.setLayoutManager(new LinearLayoutManager(getContext()));

        db = new BaseDatos(getContext());
        preferencias = new Preferencias(getContext());
        int idUsuario = preferencias.getIdUsuario();

        listaRecargas = new ArrayList<>(db.obtenerRecargasUsuario(idUsuario));

        if (listaRecargas.isEmpty()) {
            cargarRecargasDesdeWS(idUsuario);
        }

        adapter = new RecargaAdapter(getContext(), listaRecargas);
        recyclerRecargas.setAdapter(adapter);

        // Configurar el SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cargarRecargasDesdeWS(idUsuario);
            }
        });

        return vista;
    }

    private void cargarRecargasDesdeWS(int idUsuario) {
        swipeRefreshLayout.setRefreshing(true);

        AsyncHttpClient client = new AsyncHttpClient();
        String url = urlListarRecargas + "?id_usuario=" + idUsuario;

        client.get(url, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if (statusCode == 200) {
                    try {
                        JSONArray jsonArray = new JSONArray(rawJsonResponse);
                        listaRecargas.clear();
                        db = new BaseDatos(getContext());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            int idRecarga = obj.getInt("id_recarga");
                            int idUsuario = obj.getInt("id_usuario");
                            int idTarjeta = obj.getInt("id_tarjeta");
                            double monto = obj.getDouble("monto");
                            String telefono = obj.getString("telefono");
                            String metodoPago = obj.getString("metodo_pago");
                            String codigoAprobacion = obj.getString("codigo_aprobacion");
                            String fecha = obj.getString("fecha");
                            String nombreTarjeta = obj.optString("nombre_tarjeta");

                            Recarga recarga = new Recarga(idRecarga, idUsuario, idTarjeta, monto, telefono, metodoPago, codigoAprobacion, fecha,nombreTarjeta);
                            listaRecargas.add(recarga);
                            db.insertarRecarga(idRecarga, idUsuario, idTarjeta, monto, telefono, metodoPago, codigoAprobacion, fecha, nombreTarjeta);
                        }

                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Error al procesar recargas", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "ERROR: " + statusCode, Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Error al cargar recargas", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) {
                return null;
            }
        });
    }
}