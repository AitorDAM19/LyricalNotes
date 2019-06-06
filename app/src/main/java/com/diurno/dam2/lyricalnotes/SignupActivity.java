package com.diurno.dam2.lyricalnotes;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity);
        auth = FirebaseAuth.getInstance();
        final EditText etxtEmailSignup, etxtPassSignup, etxtConfirmarPass;
        TextView txtHasAccount;
        Button btnSignup;
        etxtEmailSignup  = findViewById(R.id.etxtEmailSignup);
        etxtPassSignup = findViewById(R.id.etxtPassSignup);
        etxtConfirmarPass = findViewById(R.id.etxtConfirmPass);
        txtHasAccount = findViewById(R.id.txtHasAccount);
        btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email, pass, confirmpass;
                email = etxtEmailSignup.getText().toString();
                pass = etxtPassSignup.getText().toString();
                confirmpass = etxtConfirmarPass.getText().toString();
                if (email.isEmpty() || pass.isEmpty() || confirmpass.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "No puedes dejar campos vacíos", Toast.LENGTH_LONG).show();
                }
                else if (pass.length() < 6) {
                    etxtPassSignup.setError("Mínimo 6 caracteres");
                }
                else if (!pass.equals(confirmpass)) {
                    Toast.makeText(SignupActivity.this, "Las contraseñas tienen que coincidir", Toast.LENGTH_LONG).show();
                }
                else {
                    etxtPassSignup.setError(null);
                    showProgressDialog();
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser registeredUser = auth.getCurrentUser();
                                String UID = registeredUser.getUid();
                                System.out.println("UID desde signuo: " + UID);
                                UsersDatabase bd = new UsersDatabase(SignupActivity.this);
                                if (!bd.existeUsuarioUID(UID)) {
                                    bd.guardarUsuarioUID(UID);
                                    bd.close();
                                    Toast.makeText(SignupActivity.this, "Usuario registrado", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                    intent.putExtra("uid", UID);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            } else {
                                Log.w(SignupActivity.class.getSimpleName(), "signUpWithEmail:failure", task.getException());
                                Toast.makeText(SignupActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            hideProgressDialog();
                        }
                    });
                }
            }
        });

        txtHasAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
