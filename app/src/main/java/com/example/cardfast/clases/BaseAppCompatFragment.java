package com.example.cardfast.clases;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cardfast.utils.Preferencias;

import java.util.Locale;

public class BaseAppCompatFragment extends Fragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(aplicarIdioma(context));
    }

    private Context aplicarIdioma(Context context) {
        Preferencias preferencias = new Preferencias(context);
        String idioma = preferencias.getIdioma("es");
        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}
