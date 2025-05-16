package com.example.cardfast.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cardfast.R;
import com.example.cardfast.clases.Tarjeta;

import java.util.List;

public class TarjetaAdapter extends RecyclerView.Adapter<TarjetaAdapter.TarjetaViewHolder> {

    private List<Tarjeta> listaTarjetas;
    private final OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onVerMasClick(int position);
    }

    public TarjetaAdapter(List<Tarjeta> listaTarjetas, OnItemClickListener listener) {
        this.listaTarjetas = listaTarjetas;
        this.listener = listener;
    }

    @Override
    public TarjetaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new TarjetaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(TarjetaViewHolder holder, int position) {
        Tarjeta tarjeta = listaTarjetas.get(position);
        holder.bind(tarjeta, position);
    }

    @Override
    public int getItemCount() {
        return listaTarjetas.size();
    }

    public void eliminarItem(int pos) {
        if (pos != -1) {
            listaTarjetas.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, listaTarjetas.size());
        }
    }

    public Tarjeta getItem(int pos) {
        return listaTarjetas.get(pos);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public void editarItem(int pos, Tarjeta nueva) {
        listaTarjetas.set(pos, nueva);
        notifyItemChanged(pos);
    }

    public void agregarItem(Tarjeta nueva) {
        listaTarjetas.add(nueva);
        notifyItemInserted(listaTarjetas.size() - 1);
    }

    public class TarjetaViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitulo;
        Button btnVerMas;

        public TarjetaViewHolder(View itemView) {
            super(itemView);
            txtTitulo = itemView.findViewById(R.id.txtTituloTarjeta);
            btnVerMas = itemView.findViewById(R.id.btnVerMas);
        }
        void bind(Tarjeta tarjeta, int position) {
            txtTitulo.setText(tarjeta.getTitulo());
            itemView.setOnClickListener(v -> {
                selectedPosition = getAdapterPosition();
                listener.onItemClick(getAdapterPosition());
            });
            btnVerMas.setOnClickListener(v -> {
                listener.onVerMasClick(getAdapterPosition());
            });
        }
    }
}