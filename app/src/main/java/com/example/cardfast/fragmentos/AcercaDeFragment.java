package com.example.cardfast.fragmentos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.cardfast.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AcercaDeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AcercaDeFragment extends Fragment implements View.OnClickListener {

    Button btnContactar;
    ImageButton btnFacebook, btnInstagram, btnYoutube;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AcercaDeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AcercaDeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AcercaDeFragment newInstance(String param1, String param2) {
        AcercaDeFragment fragment = new AcercaDeFragment();
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
        View view = inflater.inflate(R.layout.fragment_acerca_de, container, false);
        btnContactar = view.findViewById(R.id.btn_contactar);
        btnFacebook = view.findViewById(R.id.btn_facebook);
        btnInstagram = view.findViewById(R.id.btn_instagram);
        btnYoutube = view.findViewById(R.id.btn_youtube);

        btnFacebook.setOnClickListener(this);
        btnInstagram.setOnClickListener(this);
        btnYoutube.setOnClickListener(this);

        btnContactar.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_contactar)
            contactar();
        else if (v.getId() == R.id.btn_facebook)
            abrirRedSocial("https://www.facebook.com/UPN");
        else if (v.getId() == R.id.btn_instagram)
            abrirRedSocial("https://www.instagram.com/upn/");
        else if (v.getId() == R.id.btn_youtube)
            abrirRedSocial("https://www.youtube.com/user/UPNTVcanaloficial");
    }

    private void contactar() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:contacto@tuapp.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Soporte Card Fast");
        intent.putExtra(Intent.EXTRA_TEXT, "Hola, necesito ayuda con...");
        startActivity(Intent.createChooser(intent, "Enviar correo"));
    }

    private void abrirRedSocial(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}