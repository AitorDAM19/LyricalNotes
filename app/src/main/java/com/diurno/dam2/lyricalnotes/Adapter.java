package com.diurno.dam2.lyricalnotes;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private List<Object> listaObjetos;
    private Context context;
    private final int NOTAS = 0, AUDIOS = 1;
    private RecyclerViewClickListener recyclerViewClickListener;
    public Adapter(Context context, List<Object> listaNotas, RecyclerViewClickListener recyclerViewClickListener) {
        this.context = context;
        this.listaObjetos = listaNotas;
        this.recyclerViewClickListener = recyclerViewClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case NOTAS:
                View view = layoutInflater.inflate(R.layout.layout_notas, parent, false);
                viewHolder = new NotaViewHolder(view);
                break;
            case AUDIOS:
                View view2 = layoutInflater.inflate(R.layout.layout_grabaciones, parent, false);
                viewHolder = new AudioViewHolder(view2, recyclerViewClickListener);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()) {
            case NOTAS:
                NotaViewHolder notaViewHolder = (NotaViewHolder) viewHolder;
                prepararNotaViewHolder(notaViewHolder, position);
                break;
            case AUDIOS:
                AudioViewHolder audioViewHolder = (AudioViewHolder) viewHolder;
                prepararAudioViewHolder(audioViewHolder, position);
                break;

        }




    }


    @Override
    public int getItemCount() {
        return listaObjetos.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (listaObjetos.get(position) instanceof Nota) {
            return NOTAS;
        } else if (listaObjetos.get(position) instanceof Audio) {
            return AUDIOS;
        }
        return  -1;
    }

    public void prepararNotaViewHolder(final NotaViewHolder notaViewHolder, int position) {
        Nota nota = (Nota) listaObjetos.get(position);
        String tituloNota = nota.getTitulo();
        String contenidoNota = nota.getLetras();
        final int idNota = nota.getIdNota();
        notaViewHolder.txtTitulo.setText(tituloNota);
        notaViewHolder.txtContenido.setText(contenidoNota);

        notaViewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = notaViewHolder.txtTitulo.getText().toString();
                String contenido = notaViewHolder.txtContenido.getText().toString();
                Intent intent = new Intent(context, CrearNota.class);
                intent.putExtra("idNota", idNota);
                intent.putExtra("Titulo", titulo);
                intent.putExtra("Contenido", contenido);
                context.startActivity(intent);
            }
        });
    }

    public void prepararAudioViewHolder(AudioViewHolder audioViewHolder, int position) {
        Audio audio = (Audio) listaObjetos.get(position);
        String title = audio.getTitle();
        audioViewHolder.textView.setText(title);
        audioViewHolder.fecha.setText(audio.getFecha());
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

    class AudioViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView fecha;
        CardView cardView;
        public AudioViewHolder(@NonNull View itemView, final RecyclerViewClickListener recyclerViewClickListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgPlayStop);
            textView = itemView.findViewById(R.id.txtTituloAudio);
            fecha = itemView.findViewById(R.id.txtFecha);
            cardView = itemView.findViewById(R.id.card_view_grabaciones);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewClickListener != null) {
                        recyclerViewClickListener.onViewClicked(v, getAdapterPosition());
                    }
                }
            });
        }
    }
}


