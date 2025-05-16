package com.example.cardfast.fragmentos;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.example.cardfast.R;
import com.example.cardfast.clases.BaseAppCompatFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalificarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalificarFragment extends BaseAppCompatFragment {
    private RatingBar ratingBar;
    private EditText editComentario;
    private Button btnEnviar;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CalificarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalificarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalificarFragment newInstance(String param1, String param2) {
        CalificarFragment fragment = new CalificarFragment();
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
        View view = inflater.inflate(R.layout.fragment_calificar, container, false);

        ratingBar = view.findViewById(R.id.ratingBar);
        editComentario = view.findViewById(R.id.editComentario);
        btnEnviar = view.findViewById(R.id.btnEnviar);

        btnEnviar.setOnClickListener(v -> {
            float estrellas = ratingBar.getRating();
            String comentario = editComentario.getText().toString().trim();

            String mensaje = "⭐ Calificación: " + estrellas + " estrellas\n" +
                    "📝 Comentario: " + (comentario.isEmpty() ? "Sin comentario" : comentario);

            // Abrir WhatsApp
            String numero = "51967774800";  // Reemplazar con tu número
            String url = "https://wa.me/" + numero + "?text=" + Uri.encode(mensaje);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        return view;
    }
}