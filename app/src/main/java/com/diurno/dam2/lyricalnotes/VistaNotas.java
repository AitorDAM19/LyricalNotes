package com.diurno.dam2.lyricalnotes;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.os.IBinder;
import android.provider.Settings;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class VistaNotas extends AppCompatActivity implements View.OnClickListener, RecyclerViewClickListener {
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.diurno.dam2.lyricalnotes.PlayNewAudio";
    public static final String Broadcast_STOP_AUDIO = "com.diurno.dam2.lyricalnotes.StopAudio";
    private static final int RECORD_AUDIO_PERMISSION_REQUEST = 2;
    private RecyclerView recyclerView;
    private List<Object> listaObjetos;
    private List<File> audioFiles;
    private String UID;
    private Stack<Nota> stackNotas;
    private int lastPlayedIndex;
    private ImageView lastPlayedImageView;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private FloatingActionButton fab;
    private MediaPlayerService player;
    private boolean serviceBound = false;
    private static Adapter adapter;
    private MediaRecorder myAudioRecorder;
    private boolean isSnackbarAudioShowed = false;
    private File audioABorrar;
    private boolean layoutNotas = true, layoutAudios = false, grabandoAudio = false;
    private boolean reproduciendoAudio = false;
    private Snackbar snackbarNota;
    private Snackbar snackbarAudio;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        stackNotas = new Stack<>();
        Bundle bundle = getIntent().getExtras();
        UID = bundle.getString("id");
        recyclerView = findViewById(R.id.recyclerView);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        listaObjetos = new ArrayList<>();
        audioFiles = new ArrayList<>();
        adapter = new Adapter(VistaNotas.this, listaObjetos, this);
        recyclerView.setAdapter(adapter);
        cargarNotas2();

        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(this);
        crearDirectorioEstructuras();
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                if (layoutNotas) {
                    swipeNota(viewHolder);
                } else if (layoutAudios) {
                    swipeAudio(viewHolder);
                }
            }
        };

        new ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(recyclerView);
        register_finishedAudio();
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
            case R.id.notas:
                if (isSnackbarAudioShowed) {
                    borrarAudio(audioABorrar);
                }
                fab.setImageResource(R.drawable.editar_24);
                layoutAudios = false;
                layoutNotas = true;
                cargarNotas2();
                return true;
            case R.id.grabaciones:
                if (isSnackbarAudioShowed) {
                    borrarAudio(audioABorrar);
                }
                checkPermission();
                return true;
            case R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("¿Seguro que quieres salir?");

                builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        Intent intent = new Intent(VistaNotas.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (layoutNotas) {
            cargarNotas2();
        } else if (layoutAudios) {
            cargarAudios();
        }

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
        unregisterReceiver(audioFinished);
    }

    public void iniciar() {
        File file = new File(getFilesDir().getAbsolutePath() +
                "/" + UID + "/Estructuras", "structures.json");
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
                String[] items2 = itemsString.split(",");
                listaNombreGeneros.add(genero);
                listaItems.add(items2);
                System.out.println(genero + " - " + itemsString);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        listaNombreGeneros.add("Crear estructura");
        final CharSequence[] generos = listaNombreGeneros.toArray(new CharSequence[listaNombreGeneros.size()]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Selecciona el género")
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setItems(generos, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == generos.length - 1) {
                            Bundle bundle = new Bundle();
                            bundle.putString("uid", UID);
                            DialogCrearEstructura dialogCrearEstructura = new DialogCrearEstructura();
                            dialogCrearEstructura.setArguments(bundle);
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            dialogCrearEstructura.show(ft, DialogCrearEstructura.TAG);
                        } else {
                            System.out.println("Seleccionado: " + which);
                            Intent intent = new Intent(VistaNotas.this, CrearNota.class);
                            intent.putExtra("genre", listaItems.get(which));
                            intent.putExtra("uid", UID);
                            startActivity(intent);
                        }
                    }
                }).create().show();
    }

    /**
     * Carga todas las notas en la lista de notas.
     */
    private void cargarNotas2() {
        UsersDatabase db = new UsersDatabase(VistaNotas.this);
        listaObjetos.clear();
        listaObjetos.addAll(db.obtenerNotasUID(UID));
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        adapter.notifyDataSetChanged();
    }

    public void setUpLayoutGrabaciones() {
        if (snackbarNota != null) {
            snackbarNota.dismiss();
        }
        fab.setImageResource(R.drawable.microfono_24);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        layoutAudios = true;
        layoutNotas = false;
        createAudioDir();
        cargarAudios();

        inicializarMediaRecorder();

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void createAudioDir() {
        File folder = new File(getFilesDir().getPath(), UID + "/Grabaciones");
        System.out.println("Directorio de audios: " + folder.getAbsolutePath());
        if (folder.exists()) {
            System.out.println("YA EXISTE LA CARPETA");
        } else {
            boolean seCreo = folder.mkdirs();
            if (seCreo) {
                System.out.println("SE CREO LA CARPETA");
            } else {
                System.out.println("NO SE CREO LA CARPETA");
            }
        }
    }

    private void cargarAudios() {

        File file = new File(getFilesDir().getPath(), UID );
        File audios = new File(file, "Grabaciones");
        File[] archivos = audios.listFiles();
        listaObjetos.clear();
        audioFiles.clear();
        for (int i = 0; i < archivos.length; i++) {
            File audioFile = archivos[i];
            String nombreArchivo = audioFile.getName();
            System.out.println(nombreArchivo);
            String[] array = nombreArchivo.split("#");
            String titulo = array[0];
            String fecha = array[1].substring(0, array[1].length() - 4);
            DateFormat sdf = SimpleDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
            Date date = new Date(Long.parseLong(fecha));
            fecha = sdf.format(date);
            System.out.println("Nombre del archivo: " + audioFile.getName());
            //audioFile.getName().substring(0, audioFile.getName().length() - 4))
            listaObjetos.add(new Audio(archivos[i].getAbsolutePath(), titulo, fecha));
            audioFiles.add(audioFile);

        }
        adapter.notifyDataSetChanged();
        //
        System.out.println("NUMERO DE AUDIOS: " + listaObjetos.size());
        for (Object object : listaObjetos) {
            Audio audio = (Audio) object;
            System.out.println("Datos del audio: " + audio.getData());
        }
    }

    private void playAudio(int audioIndex) {
        //Check is service is active
        if (!serviceBound) {
            ArrayList<Audio> audioList = new ArrayList<>();
            for (Object o : listaObjetos) {
                Audio audio = (Audio) o;
                audioList.add(audio);
            }
            //Store Serializable audioList to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            System.out.println("Audio index desde playAudio: " + audioIndex);
            //Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);

            //Service is active
            //Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    private void stopAudio(int audioIndex) {
        if (serviceBound) {
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudioIndex(audioIndex);
            Intent broadcastIntent = new Intent(Broadcast_STOP_AUDIO);
            sendBroadcast(broadcastIntent);
            reproduciendoAudio = false;
            System.out.println("Termino el método stopAudio de VistaNotas");
        }
    }

    private void checkPermission() {

        if (ContextCompat.checkSelfPermission(VistaNotas.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(VistaNotas.this, Manifest.permission.RECORD_AUDIO)) {
                requestPermission();
            } else {
                requestPermission();
            }
        } else {
            setUpLayoutGrabaciones();
        }
    }

    private void requestPermission() {
        final String[] permissions = new String[]{ Manifest.permission.RECORD_AUDIO};
        ActivityCompat.requestPermissions(VistaNotas.this, permissions, RECORD_AUDIO_PERMISSION_REQUEST);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull  int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setUpLayoutGrabaciones();
                    System.out.println("Se ejecutó onRequestPermissionsResult");
                } else {
                    boolean showRationale = shouldShowRequestPermissionRationale( Manifest.permission.RECORD_AUDIO);
                    if (!showRationale) {
                        openSettingsDialog();
                    }
                    //requestPermission();
                }
            }
        }
    }

    private void openSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VistaNotas.this);
        builder.setTitle("Permisos requeridos");
        builder.setMessage("Esta app necesita acceder al micrófono para poder usar la funcion de Grabaciones.");
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
            inicializarMediaRecorder();
            if (reproduciendoAudio) {
                lastPlayedImageView.setImageResource(R.drawable.play_48);
                stopAudio(lastPlayedIndex);
            }
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
                    String tituloConFecha = titulo + "#" + System.currentTimeMillis();
                    myAudioRecorder.setOutputFile(getFilesDir().getPath() + File.separator
                            + UID + File.separator + "Grabaciones" + File.separator + tituloConFecha + ".3gp");
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
            myAudioRecorder.reset();
            myAudioRecorder.release();
            myAudioRecorder = null;
            fab.setImageResource(R.drawable.microfono_24);
            grabandoAudio = false;
            cargarAudios();

            ArrayList<Audio> audioList = new ArrayList<>();
            StorageUtil storage = new StorageUtil(getApplicationContext());
            for (Object o : listaObjetos) {
                Audio audio = (Audio) o;
                audioList.add(audio);
            }
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioList.size() - 1);
        } else {
            System.out.println("Esto no funciona que sad man");
        }
    }

    private BroadcastReceiver audioFinished = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Audio acabó?");
            lastPlayedImageView.setImageResource(R.drawable.play_48);
        }
    };

    private void register_finishedAudio() {
        IntentFilter filter = new IntentFilter(MediaPlayerService.Broadcast_FINISHED_AUDIO);
        registerReceiver(audioFinished, filter);
    }

    @Override
    public void onRowClicked(int position) {

    }

    @Override
    public void onViewClicked(View v, int position) {
        if (v.getId() == R.id.imgPlayStop) {
            System.out.println("Posicion del pulsado: " + position);
            ImageView imageView = (ImageView) v;
            if (!reproduciendoAudio) {
                imageView.setImageResource(R.drawable.stop_48);
                lastPlayedIndex = position;
                lastPlayedImageView = imageView;
                playAudio(position);
                //audioIndex = position;
                reproduciendoAudio = true;
            } else if (!listaObjetos.get(position).equals(listaObjetos.get(lastPlayedIndex))) {
                System.out.println("El que se ha pulsado no es el que se está reproduciendo.");
                stopAudio(lastPlayedIndex);
                //reproduciendoAudio = false;
                lastPlayedImageView.setImageResource(R.drawable.play_48);
                lastPlayedIndex = position;
                lastPlayedImageView = imageView;
                playAudio(position);
                imageView.setImageResource(R.drawable.stop_48);
            } else if (reproduciendoAudio) {
                System.out.println("El que se ha empezado a reproducir es el mismo");
                imageView.setImageResource(R.drawable.play_48);
                stopAudio(position);
                reproduciendoAudio = false;
            }

        }
    }

    @Override
    public void onLongClick(View v, int position) {
        if (layoutNotas) {
            Nota notaPulsada = (Nota) listaObjetos.get(position);
            Toast.makeText(VistaNotas.this, notaPulsada.getTitulo(), Toast.LENGTH_LONG).show();
        } else if (layoutAudios) {
            Audio audio = (Audio) listaObjetos.get(position);
            Toast.makeText(VistaNotas.this, audio.getTitle(), Toast.LENGTH_LONG).show();
        }
    }

    private void swipeNota(RecyclerView.ViewHolder viewHolder) {
        final int swipedPosition = viewHolder.getAdapterPosition();
        final UsersDatabase db = new UsersDatabase(VistaNotas.this);
        final Nota notaBorrada = (Nota) listaObjetos.get(swipedPosition);
        db.borrarNotaUID(notaBorrada.getIdNota());
        db.close();
        stackNotas.push(notaBorrada);
        final int posicionNota = listaObjetos.indexOf(notaBorrada);
        listaObjetos.remove(swipedPosition);
        adapter.notifyItemRemoved(swipedPosition);
        //snackbar = null;
        snackbarNota = Snackbar.make(findViewById(R.id.ConstraintLayout), "Nota borrada", 5000).
                setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Nota nota = stackNotas.pop();
                UsersDatabase db1 = new UsersDatabase(VistaNotas.this);
                db1.guardarNotaUID(UID, nota.getTitulo(), nota.getLetras());
                listaObjetos.add(posicionNota, nota);
                adapter.notifyItemInserted(swipedPosition);
            }
        });
        snackbarNota.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                fab.setY(fab.getY() + 110);
            }
        });
        snackbarNota.show();
        fab.setY(fab.getY() - 110);
    }

    private void swipeAudio(RecyclerView.ViewHolder viewHolder) {
        final int swipedPosition = viewHolder.getAdapterPosition();
        audioABorrar = null;
        audioABorrar = audioFiles.get(swipedPosition);
        final Audio audio1 = (Audio) listaObjetos.get(swipedPosition);
        listaObjetos.remove(audio1);
        adapter.notifyItemRemoved(swipedPosition);
        snackbarAudio = Snackbar.make(findViewById(R.id.ConstraintLayout), "Grabación borrada", 5000).setAction("Deshacer", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listaObjetos.add(swipedPosition, audio1);
                adapter.notifyItemInserted(swipedPosition);
            }
        });
        snackbarAudio.addCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar sb) {
                super.onShown(sb);
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    isSnackbarAudioShowed = false;
                    borrarAudio(audioABorrar);
                }
                fab.setY(fab.getY() + 110);
            }
        });
        snackbarAudio.show();
        isSnackbarAudioShowed = true;
        fab.setY(fab.getY() - 110);
    }

    public void borrarAudio(File file) {
        System.out.println("Nombre del audio a borrar: " + file.getName());
        boolean seBorro = file.delete();
        if (seBorro) {
            System.out.println("El archivo se borró correctamente");
        } else {
            System.out.println("No se borró");
        }
    }


    private void crearDirectorioEstructuras() {
        File file = new File(getFilesDir().getAbsolutePath() + "/" + UID, "Estructuras");
        System.out.println("Ruta a la carpeta Estructuras: " + file.getAbsolutePath());
        if (!file.exists()) {
            boolean seCreo = file.mkdirs();
            if (seCreo) {
                System.out.println("Se creo la carpeta Estructuras");
                InputStream is =  getResources().openRawResource(R.raw.structures);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String resultado = null;
                String line = null;
                try {
                    while ((line = br.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    resultado = sb.toString();
                    JSONObject jsonObject = new JSONObject(resultado);
                    File archivoJson = new File(file, "structures.json");
                    if (!archivoJson.exists()) {
                        boolean seCreoJson = archivoJson.createNewFile();
                        if (seCreoJson) {
                            System.out.println("SE CREO EL .JSON!!!");
                            Writer output = new BufferedWriter(new FileWriter(archivoJson));
                            output.write(jsonObject.toString());
                            output.close();
                        }
                    }

                } catch (IOException |JSONException ioe) {
                    ioe.printStackTrace();
                }
            } else {
                System.out.println("No se creo la carpeta estructuras");
            }
        } else {
            System.out.println("Ya existe la carpeta estructuras");
        }
    }

    private void inicializarMediaRecorder() {
        if (myAudioRecorder == null) {
            myAudioRecorder = new MediaRecorder();
            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        }
    }
}
