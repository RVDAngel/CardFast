package com.example.cardfast.fragmentos;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.cardfast.R;
import com.example.cardfast.clases.BaseAppCompatFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CompartirFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CompartirFragment extends BaseAppCompatFragment {

    Button btnCompartir;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CompartirFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CompartirFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CompartirFragment newInstance(String param1, String param2) {
        CompartirFragment fragment = new CompartirFragment();
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
        View view = inflater.inflate(R.layout.fragment_compartir, container, false);

        btnCompartir = view.findViewById(R.id.btnCompartir);
        btnCompartir.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String mensaje = "¡Mira esta app para recargar tus tarjetas de transporte! 🚍\nhttps://play.google.com/store/apps/details?id=" + requireContext().getPackageName();
            intent.putExtra(Intent.EXTRA_TEXT, mensaje);
            startActivity(Intent.createChooser(intent, "Compartir vía"));
        });

        return view;
    }
}