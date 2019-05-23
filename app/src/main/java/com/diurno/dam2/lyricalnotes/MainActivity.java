package com.diurno.dam2.lyricalnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button btnEntrar, btnRegistrar;
    private EditText etxtUsuario, etxtPass;
    private ViewPager viewPager;
    private FirebaseAuth auth;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            loginNuevo();
        }
        else {
            String email = user.getEmail();
            UsersDatabase udb = new UsersDatabase(MainActivity.this);
            iniciarUID(user.getUid());
        }
        /*viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return false;
            }

        });
        etxtUsuario = findViewById(R.id.etxtUsuario);
        etxtPass = findViewById(R.id.etxtPass);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsersDatabase udb = new UsersDatabase(MainActivity.this);
                String user = etxtUsuario.getText().toString();
                String pass = etxtPass.getText().toString();
                if (udb.buscarUsuario(user, pass)) {
                    iniciar(udb.idUsuario(user));
                    udb.close();
                }
                else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etxtUsuario.getText().toString();
                String pass = etxtPass.getText().toString();
                if (user.equals("") || pass.equals("")) {
                    Toast.makeText(MainActivity.this, "Tienes que introducir datos.",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    UsersDatabase bd = new UsersDatabase(MainActivity.this);
                    if (!bd.existeUsuario(user)) {
                        bd.guardarUsuario(user, pass);
                        bd.close();
                        Toast.makeText(MainActivity.this, "Usuario registrado", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(MainActivity.this, "Ya existe un usuario con ese nombre.", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });*/
    }

    public void iniciar(int idUsuario) {
        Intent intent = new Intent(this, VistaNotas.class);
        intent.putExtra("id", idUsuario);
        startActivity(intent);
        finish();
    }

    public void iniciarUID (String UID) {
        Intent intent = new Intent(this, VistaNotas.class);
        intent.putExtra("id", UID);
        startActivity(intent);
        finish();
    }

    public void loginNuevo() {
        Button btnLogin = findViewById(R.id.btnLogin);
        final EditText etxtEmail = findViewById(R.id.etxtEmail);
        final EditText etxtPass = findViewById(R.id.etxtPass);
        TextView txtRegistrarse = findViewById(R.id.txtRegistrarse);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String user = etxtEmail.getText().toString();
                String pass = etxtPass.getText().toString();
                /*if (udb.buscarUsuario(user, pass)) {
                    iniciar(udb.idUsuario(user));
                    udb.close();
                }
                else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos.", Toast.LENGTH_LONG).show();
                }*/
                showProgressDialog();
                auth.signInWithEmailAndPassword(user, pass).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String UID = firebaseUser.getUid();
                            final UsersDatabase udb = new UsersDatabase(MainActivity.this);
                            iniciarUID(UID);
                        } else {
                            Log.w(MainActivity.class.getSimpleName(), "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Correo electrónico o contraseña incorrectos.", Toast.LENGTH_LONG).show();
                        }

                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Error en el login.", Toast.LENGTH_LONG).show();
                        }
                        hideProgressDialog();
                    }
                });
            }
        });

        txtRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivityForResult(intent, 0);
                //finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //String email =  data.getStringExtra("email");
                //String pass = data.getStringExtra("pass");
                String UID = data.getStringExtra("uid");
                //UsersDatabase udb = new UsersDatabase(MainActivity.this);
                /*if (udb.buscarUsuario(email, pass)) {
                    iniciar(udb.idUsuario(email));
                    udb.close();
                }*/
                iniciarUID(UID);
            }
        }
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Cargando");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
