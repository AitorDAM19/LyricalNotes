package com.diurno.dam2.lyricalnotes;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import java.util.ArrayList;

public class UsersDatabase extends SQLiteOpenHelper {

    public UsersDatabase(Context contexto) {
        super(contexto, "BDUSUARIO", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String orden="CREATE TABLE Usuario(idUsuario integer primary key, nombre text, password text)";
        String orden2 = "CREATE TABLE Nota(idNota integer primary key, titulo text, contenido text)";
        String orden3 = "CREATE TABLE Usuario_Nota(idUsuario integer , idNota integer, PRIMARY KEY (idUsuario, idNota), CONSTRAINT fk_idusu FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario), CONSTRAINT fk_idnota FOREIGN KEY (idNota) REFERENCES Nota(idNota))";
        db.execSQL(orden);
        db.execSQL(orden2);
        db.execSQL(orden3);
        Log.d("USUARIO", orden);
        Log.d("NOTA", orden2);
        Log.d("USUARIO_NOTA", orden3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public ArrayList<Usuario> listaUsuarios() {
        ArrayList<Usuario> lista = new ArrayList<Usuario>();
        SQLiteDatabase db = getReadableDatabase();
        String consulta = "SELECT idUsuario, nombre, password FROM Usuario ORDER by id";
        Log.d("USUARIO", consulta);
        Cursor cursor = db.rawQuery(consulta, null);
        Usuario user;
        while(cursor.moveToNext()) {
            user = new Usuario();
            user.setId(cursor.getInt(0));
            user.setNombre(cursor.getString(1));
            user.setPassword(cursor.getString(2));
            lista.add(user);
        }
        cursor.close();
        db.close();
        return lista;
    }

    public void guardarUsuario(String nombre, String password){
        SQLiteDatabase db = getWritableDatabase();
        int idUsuario = (int) cuantosUsuarios();
        idUsuario += 1;
        String orden="INSERT into Usuario values("+idUsuario+",'"+nombre+"','"+password+"')";
        db.execSQL(orden);
        Log.d("COCHE", orden);
        db.close();
    }

    public void guardarNota(int idUsuario, String titulo, String contenido) {
        SQLiteDatabase db = getWritableDatabase();
        int idNota = (int) cuantasNotas() + 1;
        String orden = "INSERT into Nota values ("+idNota+",'"+titulo+"','"+contenido+"')";
        String orden2 = "INSERT into Usuario_Nota values ("+idUsuario+","+idNota+")";
        db.execSQL(orden);
        db.execSQL(orden2);
        db.close();
    }

    public ArrayList<Nota> obtenerNotas(int idUsuario) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Nota> listaNotas = new ArrayList<>();
        String orden = "SELECT idNota, titulo, contenido FROM Nota WHERE idNota IN (SELECT idNota FROM Usuario_Nota WHERE idUsuario = "+idUsuario+")";
        Cursor cursor = db.rawQuery(orden, null);
            while (cursor.moveToNext()) {
                Nota nota = new Nota(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                listaNotas.add(nota);
            }
        db.close();
        return listaNotas;
    }

    public void actualizarNota(int idNota, String titulo, String contenido) {
        SQLiteDatabase db = getWritableDatabase();
        String orden = "UPDATE Nota SET titulo = ?, contenido = ? WHERE idNota = "+idNota+"";
        db.execSQL(orden, new String[] {titulo, contenido});
        db.close();
    }

    public void borrarNota(int idNota) {
        SQLiteDatabase db = getWritableDatabase();
        String orden = "DELETE FROM Nota WHERE idNota = "+idNota+"";
        String orden2 = "DELETE FROM Usuario_Nota where idNota = "+idNota+"";
        db.execSQL(orden);
        db.execSQL(orden2);
    }

    public void borrarNotas(int idUsuario) {
        SQLiteDatabase db = getWritableDatabase();
        String orden = "DELETE FROM Nota WHERE idNota IN (SELECT idNota FROM Usuario_Nota WHERE idUsuario = "+idUsuario+")";
        String orden2 = "DELETE FROM Usuario_Nota WHERE idUsuario = "+idUsuario+"";
        db.execSQL(orden);
        db.execSQL(orden2);
    }
    /*
    public void borrarUsuario(int codigo) {
        SQLiteDatabase db = getWritableDatabase();
        String orden = "DELETE FROM  WHERE _id= " + codigo;
        db.execSQL(orden);
        Log.d("COCHE", orden);
        db.close();
    }*/

    public boolean buscarUsuario(String nombre, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String orden = "SELECT nombre, password FROM Usuario WHERE nombre = '" + nombre + "' AND password = '" + password +"'";
        Cursor cursor = db.rawQuery(orden, null);
        if (cursor.getCount() == 1) {
            cursor.close();
            db.close();
            return true;
        }
        else {
            cursor.close();
            db.close();
            return false;
        }

    }

    public boolean existeUsuario(String nombre) {
        SQLiteDatabase db = getReadableDatabase();
        String orden = "SELECT nombre FROM Usuario WHERE nombre = '" + nombre + "'";
        Cursor cursor = db.rawQuery(orden, null);
        if (cursor.getCount() == 1) {
            cursor.close();
            db.close();
            return true;
        }
        else {
            cursor.close();
            db.close();
            return false;
        }
    }

    private long cuantosUsuarios() {
        SQLiteDatabase db = getReadableDatabase();
        return DatabaseUtils.queryNumEntries(db, "Usuario");
    }

    private long cuantasNotas() {
        SQLiteDatabase db = getReadableDatabase();
        String orden = "SELECT idNota FROM Nota ORDER BY idNota DESC LIMIT 1";
        Cursor cursor = db.rawQuery(orden, null);
        if (cursor != null && cursor.moveToFirst()) {
            int ultimoId = cursor.getInt(0);
            cursor.close();
            return  ultimoId;
        }
        return 0;
    }

    public int idUsuario(String nombreUsu) {
        SQLiteDatabase db = getReadableDatabase();
        String orden = "SELECT idUsuario FROM Usuario WHERE nombre = ?";
        Cursor cursor = db.rawQuery(orden, new String[] {nombreUsu});
        System.out.println(cursor == null);
        if( cursor != null && cursor.moveToFirst() ) {
            System.out.println(cursor.getCount());
            System.out.println(cursor.getColumnName(0));
            System.out.println(cursor.isLast());
            System.out.println(cursor.getColumnIndex("Usuario"));
            return cursor.getInt(0);
        }
        return 1;
    }

}