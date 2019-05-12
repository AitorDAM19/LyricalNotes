package com.diurno.dam2.lyricalnotes;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_container, parent, false);
        return new NotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotaViewHolder notaViewHolder, int position) {
        Nota nota = listaNotas.get(position);
        final int idNota = nota.getIdNota();
        notaViewHolder.txtTitulo.setText(nota.getTitulo());
        notaViewHolder.txtContenido.setText(nota.getLetras());

        notaViewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CrearNota.class);
                intent.putExtra("idNota", idNota);
                intent.putExtra("Titulo", notaViewHolder.txtTitulo.getText().toString());
                intent.putExtra("Contenido", notaViewHolder.txtContenido.getText().toString());
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
    ConstraintLayout layout;

    NotaViewHolder (View itemView) {
        super(itemView);
        txtTitulo = itemView.findViewById(R.id.txtTitulo);
        txtContenido = itemView.findViewById(R.id.txtContenido);
        layout = itemView.findViewById(R.id.containerConstraint);
    }


}
