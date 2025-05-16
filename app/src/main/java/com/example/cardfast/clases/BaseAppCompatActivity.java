package com.example.cardfast.clases;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.cardfast.utils.Preferencias;

import java.util.Locale;

public class BaseAppCompatActivity extends AppCompatActivity {

    Preferencias preferencias;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = aplicarIdioma(newBase);
        super.attachBaseContext(context);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        preferencias = new Preferencias(this);
        boolean nightMode = preferencias.getModoNoche(false);

        AppCompatDelegate.setDefaultNightMode(
                nightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
    }

    private Context aplicarIdioma(Context context) {
        preferencias = new Preferencias(context);
        String idioma = preferencias.getIdioma("es");
        Locale locale = new Locale(idioma);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }
}