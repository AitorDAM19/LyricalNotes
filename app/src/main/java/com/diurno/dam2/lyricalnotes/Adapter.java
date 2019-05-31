package com.diurno.dam2.lyricalnotes;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<NotaViewHolder>  {
    private List<Nota> listaNotas;
    private Context context;
    public Adapter(Context context, List<Nota> listaNotas) {
        this.context = context;
        this.listaNotas= listaNotas;
    }

    @NonNull
    @Override
    public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_notas, parent, false);
        return new NotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotaViewHolder notaViewHolder, int position) {
        Nota nota = listaNotas.get(position);
        String tituloNota = nota.getTitulo();
        String contenidoNota = nota.getLetras();
        //System.out.println("Titulo de la nota cuando se le asigna a la tarjeta: " + tituloNota);
        //System.out.println("Contenido de la nota cuando se le asigna a la tarjeta: " + contenidoNota);
        final int idNota = nota.getIdNota();
        notaViewHolder.txtTitulo.setText(tituloNota);
        notaViewHolder.txtContenido.setText(contenidoNota);

        notaViewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = notaViewHolder.txtTitulo.getText().toString();
                String contenido = notaViewHolder.txtContenido.getText().toString();
                //System.out.println("Título de la nota al pulsar cardView: " + titulo);
                //System.out.println("Contenido de la nota al pulsar cardView: " + contenido);
                Intent intent = new Intent(context, CrearNota.class);
                intent.putExtra("idNota", idNota);
                intent.putExtra("Titulo", titulo);
                intent.putExtra("Contenido", contenido);
                context.startActivity(intent);
            }
        });



    }


    @Override
    public int getItemCount() {
        return listaNotas.size();
    }
}

class NotaViewHolder extends RecyclerView.ViewHolder {
    TextView txtTitulo;
    TextView txtContenido;
    CardView layout;

    NotaViewHolder (View itemView) {
        super(itemView);
        txtTitulo = itemView.findViewById(R.id.titulo);
        txtContenido = itemView.findViewById(R.id.contenido);
        layout = itemView.findViewById(R.id.card_view);
    }


}
