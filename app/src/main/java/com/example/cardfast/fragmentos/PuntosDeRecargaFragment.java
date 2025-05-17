package com.example.cardfast.fragmentos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cardfast.clases.BaseAppCompatFragment;
import com.example.cardfast.utils.Preferencias;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.cardfast.R;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.tabs.TabLayout;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PuntosDeRecargaFragment extends BaseAppCompatFragment {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private final static String urlMostrarUbicaciones = "http://bdtransporte.atwebpages.com/ws/mostrarUbicaciones.php";
    GoogleMap mMap;
    TabLayout tabSelector;
    private Preferencias preferencias;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            Preferencias preferencias = new Preferencias(requireContext());
            boolean isModoNoche = preferencias.getModoNoche(false);

            int styleResId = isModoNoche ? R.raw.night_map_style : R.raw.day_map_style;

            try {
                boolean success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(requireContext(), styleResId));

                if (!success) {
                    Log.e("MAP_STYLE", "No se pudo aplicar el estilo del mapa");
                }
            } catch (Resources.NotFoundException e) {
                Log.e("MAP_STYLE", "Archivo de estilo no encontrado", e);
            }

            obtenerUbicaciones("Tren Eléctrico");
            habilitarUbicacionUsuario();
        }
    };

    private void habilitarUbicacionUsuario() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            iniciarActualizacionUbicacion();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void iniciarActualizacionUbicacion() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
            return;
        }

        mMap.setMyLocationEnabled(true);

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) return;

                LatLng ubicacionUsuario = new LatLng(
                        locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude()
                );
                
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacionUsuario, 15));
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                habilitarUbicacionUsuario();
            } else {
                Toast.makeText(getContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_puntos_de_recarga, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa el FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        tabSelector = view.findViewById(R.id.tabSelector);
        tabSelector.addTab(tabSelector.newTab().setText("Tren Eléctrico"));
        tabSelector.addTab(tabSelector.newTab().setText("Metropolitano"));
        tabSelector.addTab(tabSelector.newTab().setText("Corredor"));

        tabSelector.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (mMap == null) return;

                mMap.clear();

                switch (tab.getPosition()) {
                    case 0:
                        obtenerUbicaciones("Tren Eléctrico");
                        break;
                    case 1:
                        obtenerUbicaciones("Corredor");
                        break;
                    case 2:
                        obtenerUbicaciones("Metropolitano");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void dibujarRuta(LatLng[] puntos, int color) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(puntos)
                .color(color)
                .width(5);
        mMap.addPolyline(polylineOptions);
    }

    private void obtenerUbicaciones(String tipoTransporte) {
        AsyncHttpClient urlMostrarUbica = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("tipo_transporte", tipoTransporte);

        urlMostrarUbica.get(urlMostrarUbicaciones, params, new BaseJsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String rawJsonResponse, Object response) {
                if(statusCode == 200){
                    try {
                        JSONArray ubicaciones = new JSONArray(rawJsonResponse);
                        mMap.clear();
                        LatLng[] ruta = new LatLng[ubicaciones.length()];

                        for (int i = 0; i < ubicaciones.length(); i++) {
                            JSONObject ubicacion = ubicaciones.getJSONObject(i);
                            double latitud = ubicacion.getDouble("latitud");
                            double longitud = ubicacion.getDouble("longitud");
                            String nombre = ubicacion.getString("nombre");

                            LatLng posicion = new LatLng(latitud, longitud);
                            ruta[i] = posicion;

                            int iconRes;
                            switch (tipoTransporte) {
                                case "Metropolitano":
                                    iconRes = R.drawable.ic_metropolitano;
                                    break;
                                case "Corredor":
                                    iconRes = R.drawable.ic_corredor;
                                    break;
                                default: // Tren Eléctrico
                                    iconRes = R.drawable.ic_tren;
                                    break;
                            }

                            mMap.addMarker(new MarkerOptions()
                                    .position(posicion)
                                    .title(nombre)
                                    .icon(BitmapDescriptorFactory.fromResource(iconRes)));
                        }

                        int colorRuta;
                        switch (tipoTransporte) {
                            case "Metropolitano":
                                colorRuta = Color.rgb(4, 67, 118);
                                break;
                            case "Corredor":
                                colorRuta = Color.rgb(114, 15, 183);
                                break;
                            default: // Tren Eléctrico
                                colorRuta = Color.rgb(0, 218, 72);
                                break;
                        }

                        dibujarRuta(ruta, colorRuta);

                        if (ubicaciones.length() > 0) {
                            JSONObject primerUbicacion = ubicaciones.getJSONObject(0);
                            double lat = primerUbicacion.getDouble("latitud");
                            double lng = primerUbicacion.getDouble("longitud");
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Toast.makeText(getContext(), "ERROR: "+statusCode, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
                Toast.makeText(getContext(), "ERROR: "+statusCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) throws Throwable {
                return null;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}