package com.diurno.dam2.lyricalnotes;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class CrearNota extends AppCompatActivity {
    private int idNota;
    private String UID;
    private EditText etxtTitulo;
    private EditText etxtContenido;
    private boolean editar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_crear_nota);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
        etxtTitulo = findViewById(R.id.etxtTitulo);
        etxtContenido = findViewById(R.id.etxtContenido);
        etxtContenido.setCustomSelectionActionModeCallback(
                new ListenerTexto(etxtContenido, CrearNota.this));
        editar  = false;
        Bundle bundle = getIntent().getExtras();

        //idUsuario = bundle.getInt("idUsu");
        UID = bundle.getString("uid");
        idNota = bundle.getInt("idNota");
        etxtTitulo.setText(bundle.getString("Titulo"));
        etxtContenido.setText(bundle.getString("Contenido"));
        if (!etxtTitulo.getText().toString().equals("")) {
            editar = true;
            if (actionBar != null) {
                actionBar.setTitle("Editar nota");
            }
        } else {
            if (actionBar != null) {
                actionBar.setTitle("Crear nota");
            }
        }
        String[] genre = bundle.getStringArray("genre");
        if (genre != null) {
            System.out.println(genre.length);
            String cadena = null;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < genre.length; i++) {
                sb.append(genre[i]);
                sb.append("<br/>");
                sb.append("<br/>");
            }
            cadena = sb.toString();
            System.out.println(cadena);
            etxtContenido.setText(Html.fromHtml(cadena));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (editar) {
            editar();
        } else {
            guardar();
        }
    }

    /**
     * Guarda la nota
     */
    public void guardar() {
        UsersDatabase db = new UsersDatabase(CrearNota.this);
        String titulo =  etxtTitulo.getText().toString();
        String contenido = etxtContenido.getText().toString();
        if (contenido.isEmpty() && titulo.isEmpty()) {
            Toast.makeText(this, "No puedes crear una nota vacía", Toast.LENGTH_SHORT).show();
            return;
        } else if (titulo.isEmpty()) {
            titulo = "Sin título";
        }
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
        db.borrarNotaUID(idNota);
        Toast.makeText(this, "Nota borrada.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.info:
                mostrarInfo();
                return true;
            case R.id.borrarNota:
                borrarNota();
                return true;
            case android.R.id.home:
                if (editar) {
                    editar();
                } else {
                    guardar();
                }
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void mostrarInfo() {
       AlertDialog builder = new AlertDialog.Builder(this)
                .setTitle("Información")
                .setIcon(R.drawable.ic_info_black_24)
               .setMessage("Prueba a buscar una rima seleccionando una palabra y luego pulsa en Buscar rima")
                .setNeutralButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
       builder.show();
    }
}
