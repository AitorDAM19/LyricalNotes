package com.diurno.dam2.lyricalnotes;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

public class VistaNotas extends AppCompatActivity implements View.OnClickListener {
    private static final int WRITE_EXTERNAL_PERMISSION_REQUEST = 2;
    private String rutaAudios;
    private Drawable iconoBorrar;
    private RecyclerView recyclerView;
    private List<Nota> listaNotas;
    private List<Audio> listaAudios;
    private int idUsuario;
    private String UID;
    private Stack<Nota> stackNotas;
    private int generoSeleccionado;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private FloatingActionButton fab;
    private MediaPlayerService player;
    private boolean serviceBound = false;
    private static Adapter adapter;
    private MediaRecorder myAudioRecorder;
    private String outputFile;
    private boolean layoutNotas = true, layoutAudios = false, grabandoAudio = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        stackNotas = new Stack<>();
        iconoBorrar = ContextCompat.getDrawable(VistaNotas.this, R.drawable.ic_delete_white_24);
        Bundle bundle = getIntent().getExtras();
        UID = bundle.getString("id");
        rutaAudios = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + UID + File.separator;
        System.out.println("La ruta de los audios: " + rutaAudios);
        recyclerView = findViewById(R.id.recyclerView);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        listaNotas = new ArrayList<>();
        listaAudios = new ArrayList<>();
        adapter = new Adapter(VistaNotas.this, listaNotas);
        recyclerView.setAdapter(adapter);
        cargarNotas2();

        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(this);

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
                            db.borrarNotaUID(notaBorrada.getIdNota());
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
        //playAudio("https://upload.wikimedia.org/wikipedia/commons/6/6c/Grieg_Lyric_Pieces_Kobold.ogg");
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
            /*case R.id.action_settings2:
                listaNotas.clear();
                UsersDatabase db = new UsersDatabase(VistaNotas.this);
                db.borrarNotas(idUsuario);
                Adapter adapter = new Adapter(VistaNotas.this, listaNotas);
                recyclerView.setAdapter(adapter);
                recyclerView.smoothScrollBy(0, recyclerView.getHeight());
                return true;*/
            case R.id.grabaciones:
               checkPermission();
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
        cargarNotas2();
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
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
        //Adapter adapter = new Adapter(VistaNotas.this, listaNotas);
        //recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        //recyclerView.smoothScrollBy(0, recyclerView.getHeight());
        System.out.println("Nº de notas: " + listaNotas.size());
    }

    public void setUpLayoutGrabaciones() {
        fab.setImageResource(R.drawable.microfono_24);
        layoutAudios = true;
        layoutNotas = false;
        createAudioDir();
        cargarAudios();

        if (myAudioRecorder == null) {
            myAudioRecorder = new MediaRecorder();
            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        }

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

            Toast.makeText(VistaNotas.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void createAudioDir() {
        File folder = new File(Environment.getExternalStorageDirectory(), UID);
        System.out.println("Directorio de audios: " + folder.getAbsolutePath());
        if (folder.exists()) {
            System.out.println("YA EXISTE LA CARPETA");
        } else {
            boolean seCreo = folder.mkdir();
            if (seCreo) {
                System.out.println("SE CREO LA CARPETA");
            } else {
                System.out.println("NO SE CREO LA CARPETA");
            }
        }
    }

    private void cargarAudios() {
        ContentResolver contentResolver = getContentResolver();
        //System.out.println(getFilesDir().getPath() + File.separator + UID);
        //System.out.println("Ruta con mediastore: " + MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        //Uri uri = new Uri.Builder().path(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + UID + File.separator).build();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        System.out.println("URI: " + uri.getPath());
        String[] proj = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.TITLE};
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media.DATA + " LIKE '/storage/emulated/0/naXabfrTxebf0jEHNrjzyUfEbJq1/%'";
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " ASC";

        Cursor cursor = contentResolver.query(uri, proj, selection, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            System.out.println("HAY AUDIOS!");
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                listaAudios.add(new Audio(data, title));
            }
            cursor.close();
        }

        System.out.println("NUMERO DE AUDIOS: " + listaAudios.size());
        for (Audio audio : listaAudios) {
            System.out.println("Datos del audio: " + audio.getData());
        }
        if (listaAudios.size() != 0) {
            playAudio(listaAudios.get(0).getData());
        }
    }

    private void playAudio(String media) {
        //Check is service is active
        if (!serviceBound) {
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            //Service is active
            //Send media with BroadcastReceiver
        }
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(VistaNotas.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(VistaNotas.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(VistaNotas.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(VistaNotas.this, Manifest.permission.RECORD_AUDIO)) {
                requestPermission();
            } else {
                requestPermission();
            }
        } else {
            setUpLayoutGrabaciones();
        }
    }

    private void requestPermission() {
        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(VistaNotas.this, permissions, WRITE_EXTERNAL_PERMISSION_REQUEST);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull  int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_PERMISSION_REQUEST: {
                boolean isPermissionForAllGranted = true;
                if (grantResults.length > 0 && permission.length == grantResults.length) {
                    for (int i = 0; i < permission.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isPermissionForAllGranted = false;
                        }
                    }
                    System.out.println("Se ejecutó onRequestPermissionsResult");
                } else {
                    boolean showRationale = shouldShowRequestPermissionRationale( Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (! showRationale) {
                        openSettingsDialog();
                    }
                   //requestPermission();
                }
                if (isPermissionForAllGranted) {
                    setUpLayoutGrabaciones();
                }
            }
        }
    }

    private void openSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VistaNotas.this);
        builder.setTitle("Permisos requeridos");
        builder.setMessage("Esta app necesita el permiso de almacenamiento para poder usar la funcion de Grabaciones.");
        builder.setPositiveButton("IR A AJUSTES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    @Override
    public void onClick(View v) {
        if (layoutNotas) {
            iniciar();
        } else if (layoutAudios && !grabandoAudio) {
            System.out.println("Se entro al else if de layoutAudios");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Título de la grabación");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Escribe un título");
            builder.setView(input);

            builder.setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String titulo = input.getText().toString();
                    myAudioRecorder.setOutputFile(rutaAudios + titulo + ".3gp");
                    try {
                        myAudioRecorder.prepare();
                        myAudioRecorder.start();
                    } catch (IllegalStateException ise) {
                        System.out.println("IllegalStateException: " + ise.getMessage());
                    } catch (IOException ioe) {
                        System.out.println("IOException: " + ioe.getMessage());
                    }
                    fab.setImageResource(R.drawable.stop_24);
                    grabandoAudio = true;
                    Toast.makeText(VistaNotas.this, "Grabación comenzada", Toast.LENGTH_LONG).show();
                }
            }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    myAudioRecorder = null;
                }
            }).show();
        } else if (grabandoAudio) {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder = null;
            fab.setImageResource(R.drawable.microfono_24);
            grabandoAudio = false;
            cargarAudios();
        } else {
            System.out.println("Esto no funciona que sad man");
        }
    }
}
