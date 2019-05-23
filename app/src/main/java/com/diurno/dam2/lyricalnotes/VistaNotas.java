package com.diurno.dam2.lyricalnotes;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class VistaNotas extends AppCompatActivity {
    private Drawable iconoBorrar;
    private RecyclerView recyclerView;
    private List<Nota> listaNotas;
    private int idUsuario;
    private String UID;
    private Stack<Nota> stackNotas;
    private int generoSeleccionado;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        stackNotas = new Stack<>();
        iconoBorrar = ContextCompat.getDrawable(VistaNotas.this, R.drawable.ic_delete_white_24);
        Bundle bundle = getIntent().getExtras();
        //idUsuario = bundle.getInt("id");
        UID = bundle.getString("id");
        recyclerView = findViewById(R.id.recyclerView);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        listaNotas = new ArrayList<>();
        Adapter adapter = new Adapter(VistaNotas.this, listaNotas);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniciar();
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();
                boolean isCanceled = dX == 0f && !isCurrentlyActive;

                if (isCanceled) {
                    Rect rect = new Rect((int) (itemView.getRight() + dX),  itemView.getTop(),  itemView.getRight(),  itemView.getBottom());
                    Paint paint = new Paint();
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                    c.drawRect(rect, paint);
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, false);
                    return;
                }
                ColorDrawable background = new ColorDrawable();
                background.setColor(Color.parseColor("#FFFFFF"));
                int dXint = (int) dX;
                background.setBounds(itemView.getRight() + dXint, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                background.draw(c);

                int iconoBorrarTop = itemView.getTop() + (itemHeight - iconoBorrar.getIntrinsicHeight()) / 2;
                int iconoBorrarMargin = (itemHeight - iconoBorrar.getIntrinsicHeight());
                int iconoBorrarLeft = itemView.getRight() - iconoBorrarMargin - iconoBorrar.getIntrinsicWidth();
                int iconoBorrarRight = itemView.getRight() - iconoBorrarMargin;
                int iconoBorrarBottom = iconoBorrarTop + iconoBorrar.getIntrinsicHeight();

                iconoBorrar.setBounds(iconoBorrarLeft, iconoBorrarTop, iconoBorrarRight, iconoBorrarBottom);
                iconoBorrar.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                int swipedPosition = viewHolder.getAdapterPosition();
                final UsersDatabase db = new UsersDatabase(VistaNotas.this);
                final Nota notaBorrada = listaNotas.get(swipedPosition);
                stackNotas.push(notaBorrada);
                final int posicionNota = listaNotas.indexOf(notaBorrada);
                listaNotas.remove(swipedPosition);
                Snackbar snackbar = Snackbar.make(findViewById(R.id.ConstraintLayout), "Nota borrada", 5000).setAction("Deshacer", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Nota nota = stackNotas.pop();
                        listaNotas.add(posicionNota, nota);
                        cargarNotas2();
                    }
                });
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onShown(Snackbar sb) {
                        super.onShown(sb);
                    }

                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            db.borrarNota(notaBorrada.getIdNota());
                            cargarNotas2();
                        }
                    }
                });
                snackbar.show();

                Adapter adapter = new Adapter(VistaNotas.this, listaNotas);
                recyclerView.setAdapter(adapter);
                recyclerView.smoothScrollBy(0, recyclerView.getHeight());
                //cargarNotas2();
            }
        };

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);
        cargarNotas2();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings2:
                listaNotas.clear();
                UsersDatabase db = new UsersDatabase(VistaNotas.this);
                db.borrarNotas(idUsuario);
                Adapter adapter = new Adapter(VistaNotas.this, listaNotas);
                recyclerView.setAdapter(adapter);
                recyclerView.smoothScrollBy(0, recyclerView.getHeight());
                return true;
            case R.id.logout:
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signOut();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        listaNotas.clear();
        cargarNotas2();
        Adapter adapter = new Adapter(VistaNotas.this, listaNotas);
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollBy(0, recyclerView.getHeight());
    }

    public void iniciar() {
        InputStream is =  getResources().openRawResource(R.raw.structures);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String resultado = null;
        String line = null;
        List<String> listaNombreGeneros = new ArrayList<>();
        final ArrayList<String[]> listaItems = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            resultado = sb.toString();
            JSONObject jsonObject = new JSONObject(resultado);
            JSONArray structures = jsonObject.getJSONArray("structures");
            for (int i = 0; i < structures.length(); i++) {
                JSONObject estructura = structures.getJSONObject(i);
                String genero = estructura.getString("genero");
                String itemsString = estructura.getString("items");
                //itemsString = itemsString.replaceAll(" ", "");
                String[] items2 = itemsString.split(",");
                listaNombreGeneros.add(genero);
                listaItems.add(items2);
                System.out.println(genero + " - " + itemsString);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        CharSequence[] generos = listaNombreGeneros.toArray(new CharSequence[listaNombreGeneros.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = getLayoutInflater();

        builder.setTitle("Selecciona el género")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setItems(generos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.out.println("Seleccionado: " + which);
                        Intent intent = new Intent(VistaNotas.this, CrearNota.class);
                        intent.putExtra("genre", listaItems.get(which));
                        //intent.putExtra("idUsu", idUsuario);
                        intent.putExtra("uid", UID);
                        startActivity(intent);
                    }
                }).create().show();
        /*Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();*/
    }

    /**
     * Carga todas las notas en la lista de notas.
     */
    private void cargarNotas2() {
        UsersDatabase db = new UsersDatabase(VistaNotas.this);
        listaNotas.clear();
        listaNotas.addAll(db.obtenerNotasUID(UID));
        Adapter adapter = new Adapter(VistaNotas.this, listaNotas);
        recyclerView.setAdapter(adapter);
        recyclerView.smoothScrollBy(0, recyclerView.getHeight());
        System.out.println("Nº de notas: " + listaNotas.size());
    }
}
