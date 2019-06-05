package com.diurno.dam2.lyricalnotes;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            iniciarUID(user.getUid());
        }
        else {
            loginNuevo();
        }
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
                if (user.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No puedes dejar campos vacíos",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog();
                Task<AuthResult> authResultTask = auth.signInWithEmailAndPassword(user, pass);
                authResultTask.addOnCompleteListener(MainActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    String UID = firebaseUser.getUid();
                                    final UsersDatabase udb = new UsersDatabase(MainActivity.this);
                                    iniciarUID(UID);
                                } else {
                                    Log.w(MainActivity.class.getSimpleName(), "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this,
                                            "Correo electrónico o contraseña incorrectos.", Toast.LENGTH_LONG).show();
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
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String UID = data.getStringExtra("uid");
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
