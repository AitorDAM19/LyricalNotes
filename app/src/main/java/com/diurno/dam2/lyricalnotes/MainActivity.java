package com.diurno.dam2.lyricalnotes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button btnEntrar, btnRegistrar;
    private EditText etxtUsuario, etxtPass;
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.pager);
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
        });
    }

    public void iniciar(int idUsuario) {
        Intent intent = new Intent(this, VistaNotas.class);
        intent.putExtra("id", idUsuario);
        startActivity(intent);
    }
}
