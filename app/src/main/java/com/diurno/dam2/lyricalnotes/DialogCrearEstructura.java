package com.diurno.dam2.lyricalnotes;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DialogCrearEstructura extends DialogFragment {
    public static String TAG = "DialogCrearEstructura";

    private EditText nombreEstructura;
    private TextView txtVistaPrevia;
    private Button btnAnadir, btnBorrar, btnBorrarTodo;
    private Spinner spinner;
    private List<String> listaContenidos;
    private List<String> itemsEstructura;
    private String UID;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        if (getArguments() != null) {
            UID = getArguments().getString("uid");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.layout_crear_estructura, container, false);
        listaContenidos = new ArrayList<>();
        itemsEstructura = new ArrayList<>();
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle("Crear estructura");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_close_24);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCrearEstructura.this.dismiss();
            }
        });
        toolbar.inflateMenu(R.menu.crear_estructura);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.guardar_estructura) {
                    if (nombreEstructura.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), "No puedes dejar el nombre vacío", Toast.LENGTH_SHORT).show();
                        return false;
                    } else if (itemsEstructura.size() == 0) {
                        Toast.makeText(getContext(), "No puedes crear una estructura vacía", Toast.LENGTH_SHORT).show();
                        return false;
                    } else {
                        File file = new File(getContext().getFilesDir().getAbsolutePath() + "/" + UID + "/Estructuras", "structures.json");
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(file);
                        } catch (FileNotFoundException fnfe) {
                            fnfe.printStackTrace();
                        }
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        StringBuilder sb = new StringBuilder();
                        String resultado = null;
                        String line = null;
                        try {
                            while ((line = br.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            resultado = sb.toString();
                            JSONObject jsonObject = new JSONObject(resultado);
                            JSONArray structures = jsonObject.getJSONArray("structures");
                            JSONObject jsonEstructura = new JSONObject();
                            jsonEstructura.put("genero", nombreEstructura.getText().toString().trim());
                            String items = "";
                            for (String string : itemsEstructura) {
                                items += string + ",";
                            }
                            items = items.substring(0, items.length() - 1);
                            jsonEstructura.put("items", items);
                            structures.put(jsonEstructura);

                            Writer output = new BufferedWriter(new FileWriter(file));
                            output.write(jsonObject.toString());
                            output.close();
                            dismiss();
                            return true;
                        } catch (JSONException | IOException jse) {
                            jse.printStackTrace();
                        }

                    }
                }
                return false;
            }
        });
        setHasOptionsMenu(true);
        nombreEstructura = view.findViewById(R.id.etxtNombreEstructura);
        txtVistaPrevia = view.findViewById(R.id.txtContenidVistaPrevia);
        txtVistaPrevia.setMovementMethod(new ScrollingMovementMethod());
        btnAnadir = view.findViewById(R.id.agregar);
        btnAnadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listaContenidos.add(txtVistaPrevia.getText().toString());
                itemsEstructura.add(spinner.getSelectedItem().toString());
                String seleccionado = spinner.getSelectedItem().toString();
                txtVistaPrevia.append(seleccionado + "\n\n");
            }
        });
        btnBorrar = view.findViewById(R.id.borrar);
        btnBorrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listaContenidos.size() > 0) {
                    String ultimoTexto = listaContenidos.get(listaContenidos.size() - 1);
                    txtVistaPrevia.setText(ultimoTexto);
                    listaContenidos.remove(listaContenidos.size() - 1);
                    itemsEstructura.remove(itemsEstructura.size() - 1);
                }
            }
        });
        btnBorrarTodo = view.findViewById(R.id.borrar_todo);
        btnBorrarTodo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtVistaPrevia.setText("");
                itemsEstructura.clear();
                listaContenidos.clear();
            }
        });
        spinner = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.secciones, android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.crear_estructura, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.guardar_estructura) {
            Toast.makeText(getActivity(), "Boo", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    }

}
