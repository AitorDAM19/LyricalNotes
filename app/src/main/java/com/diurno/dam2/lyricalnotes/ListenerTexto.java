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
        String texto = etxtContenido.getText().toString().substring(start, end);
        System.out.println(texto);
        switch (item.getItemId()) {
            case R.id.buscar_rima:
                listaRimas.clear();
                System.out.println("Me has pulsado :)");
                String url = "https://api.datamuse.com/words?rel_rhy=" + texto + "&max=20";
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
    /*
     class BuscarRima extends AsyncTask<String, Void, List<String>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            addRimas(strings);
            super.onPostExecute(strings);
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            System.out.println("estamos en el asynctask");
            final List<String> lista = new ArrayList<>();
            String palabra = strings[0];
            String url = "https://api.datamuse.com/words?rel_rhy=" + palabra;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray array = response.getJSONArray("");
                        System.out.println("jsonarray size: " + array);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);
                            String rima = jsonObject.getString("word");
                            lista.add(rima);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            //MySingleton.getInstance().addToRequestQueue(jsonObjectRequest);
            return lista;
    }*/



