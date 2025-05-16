package com.example.cardfast.adaptadores;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cardfast.R;
import com.example.cardfast.clases.Recarga;

import java.util.List;

public class RecargaAdapter extends RecyclerView.Adapter<RecargaAdapter.ViewHolder> {

    private Context context;
    private List<Recarga> listaRecargas;

    public RecargaAdapter(Context context, List<Recarga> listaRecargas) {
        this.context = context;
        this.listaRecargas = listaRecargas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_recarga, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recarga recarga = listaRecargas.get(position);
        holder.txtTarjeta.setText(context.getString(R.string.txt_recarga_tarjeta, recarga.getNombreTarjeta()));
        holder.txtMonto.setText(context.getString(R.string.txt_recarga_monto, recarga.getMonto()));
        holder.txtMetodo.setText(context.getString(R.string.txt_recarga_metodo, recarga.getMetodoPago()));
        holder.txtFecha.setText(context.getString(R.string.txt_recarga_fecha, recarga.getFecha()));
    }

    @Override
    public int getItemCount() {
        return listaRecargas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTarjeta, txtMonto, txtMetodo, txtFecha;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTarjeta = itemView.findViewById(R.id.txtTarjeta);
            txtMonto = itemView.findViewById(R.id.txtMonto);
            txtMetodo = itemView.findViewById(R.id.txtMetodo);
            txtFecha = itemView.findViewById(R.id.txtFecha);
        }
    }
}
