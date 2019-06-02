package com.diurno.dam2.lyricalnotes;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListenerTexto implements ActionMode.Callback {
    private EditText etxtContenido;
    private  List<String> listaRimas;
    private Context context;
    private int rimaSeleccionada;
    private int idiomaSeleccionado;

    public ListenerTexto(EditText etxtContenido, Context context) {
        this.etxtContenido = etxtContenido;
        listaRimas = new ArrayList<>();
        this.context = context;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.setTitle("Rimas");
        mode.getMenuInflater().inflate(R.menu.buscar_rima, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int start = etxtContenido.getSelectionStart();
        int end = etxtContenido.getSelectionEnd();
        final String texto = etxtContenido.getText().toString().substring(start, end);
        System.out.println(texto);
        switch (item.getItemId()) {
            case R.id.buscar_rima:
                listaRimas.clear();
                List<String> listaIdiomas = new ArrayList<>();
                listaIdiomas.add("Español");
                listaIdiomas.add("Inglés");
                CharSequence[] idiomas = listaIdiomas.toArray(new CharSequence[listaIdiomas.size()]);
                System.out.println("Me has pulsado :)");
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Selecciona un idioma")
                        .setPositiveButton("Buscar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String url = "";
                                if (idiomaSeleccionado == 0) {
                                    url = "https://api.datamuse.com/words?rel_rhy=" + texto + "&v=es&max=20";
                                } else if (idiomaSeleccionado == 1) {
                                    url = "https://api.datamuse.com/words?rel_rhy=" + texto + "&max=20";
                                }
                                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                                    @Override
                                    public void onResponse(JSONArray response) {
                                        try {
                                            System.out.println("jsonarray size: " + response.length());
                                            for (int i = 0; i < response.length(); i++) {
                                                JSONObject jsonObject = response.getJSONObject(i);
                                                String rima = jsonObject.getString("word");
                                                listaRimas.add(rima);
                                            }
                                            mostrarRimas();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println("Error en la petición: ");
                                        error.printStackTrace();
                                    }
                                });
                                MySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
                            }
                        })
                        .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setSingleChoiceItems(idiomas, rimaSeleccionada, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                idiomaSeleccionado = which;

                            }
                        })
                        .create().show();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    public void mostrarRimas() {
        CharSequence[] rimas = listaRimas.toArray(new CharSequence[listaRimas.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Selecciona una palabra")
                .setPositiveButton("Insertar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        etxtContenido.getText().insert(etxtContenido.getSelectionEnd(), " " + listaRimas.get(rimaSeleccionada));
                        //etxtContenido.append(listaRimas.get(rimaSeleccionada));
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setSingleChoiceItems(rimas, rimaSeleccionada, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println(which);
                        rimaSeleccionada = which;

                    }
                })
                .create().show();
    }
    }



