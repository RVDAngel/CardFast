package com.example.cardfast.utils;

import android.content.Context;
import android.content.res.Configuration;

import com.example.cardfast.utils.Preferencias;

import java.util.Locale;

public class LocaleHelper {
    public static Context actualizarIdioma(Context context) {
        Preferencias preferencias = new Preferencias(context);
        String idioma = preferencias.getIdioma("es");
        Locale nuevoLocale = new Locale(idioma);
        Locale.setDefault(nuevoLocale);

        Configuration config = new Configuration();
        config.setLocale(nuevoLocale);

        return context.createConfigurationContext(config);
    }
}
