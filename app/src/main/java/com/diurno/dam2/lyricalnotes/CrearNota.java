package com.diurno.dam2.lyricalnotes;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;

public class CrearNota extends AppCompatActivity {
    private int idNota;
    private int idUsuario;
    private String UID;
    private EditText etxtTitulo;
    private EditText etxtContenido;
    private boolean editar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_crear_nota);
        etxtTitulo = findViewById(R.id.etxtTitulo);
        etxtContenido = findViewById(R.id.etxtContenido);
        etxtContenido.setCustomSelectionActionModeCallback(new ListenerTexto(etxtContenido, CrearNota.this));
        editar  = false;
        Bundle bundle = getIntent().getExtras();

        //idUsuario = bundle.getInt("idUsu");
        UID = bundle.getString("uid");
        idNota = bundle.getInt("idNota");
        idUsuario = bundle.getInt("idUsu");
        etxtTitulo.setText(bundle.getString("Titulo"));
        etxtContenido.setText(bundle.getString("Contenido"));
        if (!etxtTitulo.getText().toString().equals("")) {
            editar = true;
        }
        String[] genre = bundle.getStringArray("genre");
        if (genre != null) {
            System.out.println(genre.length);
            String cadenaNegrita = null;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < genre.length; i++) {
                //String cadenaBold = "<b>" + genre[i] + "</b>";
                sb.append("<b>" + genre[i] + "</b>");
                sb.append("<br/>");
                sb.append("<br/>");
                //etxtContenido.append(Html.fromHtml(cadenaBold) + "\n\n");
            }
            cadenaNegrita = sb.toString();
            System.out.println(cadenaNegrita);
            etxtContenido.setText(Html.fromHtml(cadenaNegrita));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Guarda la nota
     */
    public void guardar() {
        UsersDatabase db = new UsersDatabase(CrearNota.this);
        String titulo =  etxtTitulo.getText().toString();
        String contenido = etxtContenido.getText().toString();
        //db.guardarNota(idUsuario,titulo, contenido);
        db.guardarNotaUID(UID, titulo, contenido);
        Toast.makeText(this, "Nota guardada.", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Edita la nota
     */
    public void editar() {
        UsersDatabase db = new UsersDatabase(CrearNota.this);
        db.actualizarNota(idNota, etxtTitulo.getText().toString(), etxtContenido.getText().toString());
        Toast.makeText(this, "Nota editada.", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Borra la nota seleccionada
     */
    public void borrarNota() {
        UsersDatabase db = new UsersDatabase(CrearNota.this);
        db.borrarNota(idNota);
        Toast.makeText(this, "Nota borrada.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                if (editar) {
                    editar();
                }
                else {
                    if (etxtTitulo.getText().toString().equals("") && etxtContenido.getText().toString().equals("")) {
                        Toast.makeText(CrearNota.this, "No puedes crear una nota sin título ni contenido.", Toast.LENGTH_LONG).show();
                    } else {
                        guardar();
                    }
                }
                return true;
            case R.id.borrarNota:
                    borrarNota();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
