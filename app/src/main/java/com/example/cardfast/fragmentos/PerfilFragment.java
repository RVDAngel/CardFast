package com.example.cardfast.fragmentos;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.cardfast.R;
import com.example.cardfast.actividades.EditarPerfilActivity;
import com.example.cardfast.actividades.LoginActivity;
import com.example.cardfast.actividades.NotificacionSaldoActivity;
import com.example.cardfast.clases.BaseAppCompatFragment;
import com.example.cardfast.sqlite.BaseDatos;
import com.example.cardfast.utils.Preferencias;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PerfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PerfilFragment extends BaseAppCompatFragment implements View.OnClickListener {
    SwitchCompat switchNightMode, switchNotifications, switchPrivateAccount;
    Preferencias preferencias;
    BaseDatos db;
    LinearLayout layoutConfigurarSaldo;
    RelativeLayout logoutLayout, languageLayout;
    TextView txtNombreUsuario, txtCorreoUsuario;
    ShapeableImageView imgFotoPerfil;
    Button btnEditarPerfil;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_NOMBRE = "nombre_usuario";
    private static final String ARG_CORREO = "correo_usuario";

    // TODO: Rename and change types of parameters
    private String nombreUsuario;
    private String correoUsuario;
    private String fotoBase64;

    public PerfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param nombre Parameter 1.
     * @param correo Parameter 2.
     * @return A new instance of fragment PerfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PerfilFragment newInstance(String nombre, String correo) {
        PerfilFragment fragment = new PerfilFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NOMBRE, nombre);
        args.putString(ARG_CORREO, correo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            nombreUsuario = getArguments().getString(ARG_NOMBRE);
            correoUsuario = getArguments().getString(ARG_CORREO);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        
        switchNightMode = view.findViewById(R.id.switchNightMode);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        switchPrivateAccount = view.findViewById(R.id.switchPrivateAccount);
        txtNombreUsuario = view.findViewById(R.id.txtNombreUsuario);
        txtCorreoUsuario = view.findViewById(R.id.txtCorreoUsuario);
        imgFotoPerfil = view.findViewById(R.id.imgFotoPerfil);
        btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);

        txtNombreUsuario.setText(nombreUsuario);
        txtCorreoUsuario.setText(correoUsuario);

        logoutLayout = view.findViewById(R.id.logoutLayout);
        languageLayout = view.findViewById(R.id.languageLayout);
        layoutConfigurarSaldo = view.findViewById(R.id.layoutConfigurarSaldo);

        logoutLayout.setOnClickListener(this);
        languageLayout.setOnClickListener(this);
        layoutConfigurarSaldo.setOnClickListener(this);
        btnEditarPerfil.setOnClickListener(this);

        preferencias = new Preferencias(requireContext());

        cargarConfiguraciones();

        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencias.setModoNoche(isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencias.setNotificaciones(isChecked);
            layoutConfigurarSaldo.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        switchPrivateAccount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencias.setCuentaPrivada(isChecked);
        });

        return view;
    }

    private void cargarConfiguraciones() {
        boolean nightMode = preferencias.getModoNoche(isSystemInDarkMode());
        boolean notifications = preferencias.getNotificaciones(false);
        boolean privateAccount = preferencias.getCuentaPrivada(false);

        layoutConfigurarSaldo.setVisibility(notifications ? View.VISIBLE : View.GONE);
        switchNightMode.setChecked(nightMode);
        switchNotifications.setChecked(notifications);
        switchPrivateAccount.setChecked(privateAccount);

        AppCompatDelegate.setDefaultNightMode(
                nightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    private boolean isSystemInDarkMode() {
        int currentNightMode = getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.logoutLayout)
            mostrarDialogoConfirmacion();
        else if(v.getId() == R.id.layoutConfigurarSaldo)
            mostrarFragmentNotifSaldo();
        else if(v.getId() == R.id.languageLayout)
            mostrarDialogoIdiomas();
        else if(v.getId() == R.id.btnEditarPerfil)
            irPerfil();
    }

    @Override
    public void onResume() {
        super.onResume();
        actualizarDatosPerfil();
    }

    private void actualizarDatosPerfil() {
        if (preferencias == null) {
            preferencias = new Preferencias(requireContext());
        }
        if (db == null) {
            db = new BaseDatos(requireContext());
        }

        correoUsuario = preferencias.getCorreoUsuario();
        nombreUsuario = db.obtenerNombreUsuario(correoUsuario);
        fotoBase64 = preferencias.getFotoPerfil();

        txtNombreUsuario.setText(nombreUsuario != null ? nombreUsuario : "Usuario");
        txtCorreoUsuario.setText(correoUsuario);

        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            Bitmap bitmap = base64ToBitmap(fotoBase64);
            if (bitmap != null) {
                imgFotoPerfil.setImageBitmap(bitmap);
            } else {
                imgFotoPerfil.setImageResource(R.drawable.person);
            }
        }
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
    private void irPerfil() {
        Intent iEditar = new Intent(getActivity(), EditarPerfilActivity.class);
        startActivity(iEditar);
    }

    private void mostrarDialogoIdiomas() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(getString(R.string.seleccionar_idioma));
        View customLayout = getLayoutInflater().inflate(R.layout.dialog_language, null);
        builder.setView(customLayout);

        RadioGroup radioGroup = customLayout.findViewById(R.id.radioGroup);
        RadioButton rbEspañol = customLayout.findViewById(R.id.rbEspanol);
        RadioButton rbIngles = customLayout.findViewById(R.id.rbIngles);
        
        String idiomaGuardado = preferencias.getIdioma("es");
        if ("en".equals(idiomaGuardado)) {
            rbIngles.setChecked(true);
        } else {
            rbEspañol.setChecked(true);
        }

        builder.setPositiveButton(getString(R.string.aceptar), (dialog, which) -> {
            String idiomaSeleccionado = rbIngles.isChecked() ? "en" : "es";
            preferencias.setIdioma(idiomaSeleccionado);
            setLocale(idiomaSeleccionado);
            requireActivity().recreate();
        });

        builder.setNegativeButton(getString(R.string.cancelar), null);
        androidx.appcompat.app.AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        android.content.res.Configuration config = new android.content.res.Configuration();
        config.setLocale(locale);
        requireContext().getResources().updateConfiguration(config, requireContext().getResources().getDisplayMetrics());
    }

    private void mostrarFragmentNotifSaldo() {
        Intent intent = new Intent(requireContext(), NotificacionSaldoActivity.class);
        startActivity(intent);
    }

    private void mostrarDialogoConfirmacion() {
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.titulo_cerrar_sesion))
                .setMessage(getString(R.string.mensaje_cerrar_sesion))
                .setIcon(R.drawable.baseline_exit_to_app_24)
                .setPositiveButton(getString(R.string.opcion_si), (dialogInterface, which) -> cerrarSesionConAnimacion())
                .setNegativeButton(getString(R.string.opcion_no), null)
                .create();

        dialog.show();
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }

    private void cerrarSesionConAnimacion() {
        View rootView = requireActivity().getWindow().getDecorView().getRootView();
        rootView.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(() -> cerrarSesion())
                .start();
    }

    private void cerrarSesion() {
        preferencias.cerrarSesion();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}