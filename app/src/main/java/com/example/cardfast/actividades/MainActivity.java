package com.example.cardfast;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.cardfast.clases.BaseAppCompatActivity;
import com.example.cardfast.fragmentos.AcercaDeFragment;
import com.example.cardfast.fragmentos.CalificarFragment;
import com.example.cardfast.fragmentos.CompartirFragment;
import com.example.cardfast.fragmentos.MisRecargasFragment;
import com.example.cardfast.fragmentos.PuntosDeRecargaFragment;
import com.example.cardfast.fragmentos.TarjetaFragment;
import com.example.cardfast.fragmentos.HomeFragment;
import com.example.cardfast.fragmentos.PerfilFragment;
import com.example.cardfast.sqlite.BaseDatos;
import com.example.cardfast.utils.Preferencias;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.navigation.NavigationView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BaseJsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends BaseAppCompatActivity {

    private static final String urlObtenerFoto = "http://bdtransporte.atwebpages.com/ws/obtenerFotoPerfil.php";
    DrawerLayout drawerLayout;
    MaterialToolbar materialToolbar;
    FrameLayout frameLayout;
    NavigationView navigationView;
    BaseDatos db;
    Preferencias preferencias;
    TextView navName, navEmail;
    ShapeableImageView navImage;
    public String fotoBase64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = new BaseDatos(this);
        preferencias = new Preferencias(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.draweLayout);
        materialToolbar = findViewById(R.id.materialToolbar);
        frameLayout = findViewById(R.id.frameLayout);
        navigationView = findViewById(R.id.navigationView);

        String correo = preferencias.getCorreoUsuario();
        String nombre = db.obtenerNombreUsuario(correo);

        View headerView = navigationView.getHeaderView(0);
        navName = headerView.findViewById(R.id.navName);
        navEmail = headerView.findViewById(R.id.navEmail);
        navImage = headerView.findViewById(R.id.navImage);
        
        navName.setText(nombre);
        navEmail.setText(correo);
        
        fotoBase64 = preferencias.getFotoPerfil();

        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            Bitmap decodedBitmap = base64ToBitmap(fotoBase64);
            if (decodedBitmap != null) {
                navImage.setImageBitmap(decodedBitmap);
            } else {
                navImage.setImageResource(R.drawable.person);
            }
        } else {
            obtenerFotoDesdeWS(correo);
        }

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                MainActivity.this, drawerLayout, materialToolbar, R.string.drawer_close, R.string.drawer_open);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.nav_home){
                    replaceFragment(new HomeFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.nav_profile){
                    replaceFragment(PerfilFragment.newInstance(nombre, correo));
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.nav_card){
                    replaceFragment(new TarjetaFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.nav_maps){
                    replaceFragment(new PuntosDeRecargaFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.nav_history){
                    replaceFragment(new MisRecargasFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.nav_share){
                    replaceFragment(new CompartirFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.nav_rate){
                    replaceFragment(new CalificarFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(item.getItemId() == R.id.nav_about){
                    replaceFragment(new AcercaDeFragment());
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarHeaderDrawer();
    }
    private void actualizarHeaderDrawer() {
        if (preferencias == null) {
            preferencias = new Preferencias(this);
        }

        String correo = preferencias.obtenerCorreo(this);
        String nombre = db.obtenerNombreUsuario(correo);
        String fotoBase64 = preferencias.getFotoPerfil();

        navName.setText(nombre);
        navEmail.setText(correo);

        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            Bitmap decodedBitmap = base64ToBitmap(fotoBase64);
            if (decodedBitmap != null) {
                navImage.setImageBitmap(decodedBitmap);
            } else {
                navImage.setImageResource(R.drawable.person);
            }
        } else {
            navImage.setImageResource(R.drawable.person);
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
                            Bitmap decodedBitmap = base64ToBitmap(fotoBase64);
                            if (decodedBitmap != null) {
                                navImage.setImageBitmap(decodedBitmap);
                            } else {
                                navImage.setImageResource(R.drawable.person);
                            }
                        }else {
                            navImage.setImageResource(R.drawable.person);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, String rawJsonData, Object errorResponse) {
            }

            @Override
            protected Object parseResponse(String rawJsonData, boolean isFailure) {
                return null;
            }
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }
    private Bitmap base64ToBitmap(String base64String) {
        try {
            byte[] imageBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}