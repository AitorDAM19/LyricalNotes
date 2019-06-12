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
    private static int dbVersion = 1;
    public UsersDatabase(Context contexto) {
        super(contexto, "BDUSUARIO", null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String orden="CREATE TABLE Usuario(idUsuario integer primary key, nombre text, password text)";
        String ordenUID = "CREATE TABLE UsuarioUID(idUsuario text primary key)";
        String orden2 = "CREATE TABLE Nota(idNota integer primary key, titulo text, contenido text)";
        String orden3 = "CREATE TABLE Usuario_Nota(idUsuario integer , idNota integer, PRIMARY KEY (idUsuario, idNota), CONSTRAINT fk_idusu FOREIGN KEY (idUsuario) REFERENCES Usuario(idUsuario), CONSTRAINT fk_idnota FOREIGN KEY (idNota) REFERENCES Nota(idNota))";
        String orden4 = "CREATE TABLE Usuario_Nota_UID(idUsuario text , idNota integer, PRIMARY KEY (idUsuario, idNota), CONSTRAINT fk_idusu FOREIGN KEY (idUsuario) REFERENCES UsuarioUID(idUsuario), CONSTRAINT fk_idnota FOREIGN KEY (idNota) REFERENCES Nota(idNota))";

        db.execSQL(orden);
        db.execSQL(orden2);
        db.execSQL(orden3);
        db.execSQL(ordenUID);
        db.execSQL(orden4);
        Log.d("USUARIO", orden);
        Log.d("NOTA", orden2);
        Log.d("USUARIO_NOTA", orden3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            dbVersion++;
        }
    }

    public void guardarUsuarioUID(String UID){
        SQLiteDatabase db = getWritableDatabase();
        String orden="INSERT into UsuarioUID values('"+UID+"')";
        db.execSQL(orden);
        Log.d("COCHE", orden);
        db.close();
    }


    public void guardarNotaUID(String idUsuario, String titulo, String contenido) {
        SQLiteDatabase db = getWritableDatabase();
        int idNota = (int) cuantasNotas() + 1;
        String orden = "INSERT into Nota values ("+idNota+",'"+titulo+"','"+contenido+"')";
        String orden2 = "INSERT into Usuario_Nota_UID values ('"+idUsuario+"',"+idNota+")";
        db.execSQL(orden);
        db.execSQL(orden2);
        db.close();
    }

    public ArrayList<Nota> obtenerNotasUID(String idUsuario) {
        System.out.println("UID: " + idUsuario);
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Nota> listaNotas = new ArrayList<>();
        String orden = "SELECT idNota, titulo, contenido FROM Nota WHERE idNota IN (SELECT idNota FROM Usuario_Nota_UID WHERE idUsuario = '"+idUsuario+"')";
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


    public void borrarNotaUID(int idNota) {
        SQLiteDatabase db = getWritableDatabase();
        String orden = "DELETE FROM Nota WHERE idNota = "+idNota+"";
        String orden2 = "DELETE FROM Usuario_Nota_UID where idNota = "+idNota+"";
        db.execSQL(orden);
        db.execSQL(orden2);
    }


    public boolean buscarUsuarioUID(String UID) {
        SQLiteDatabase db = getReadableDatabase();
        String orden = "SELECT idUsuario FROM UsuarioUID WHERE nombre = '" + UID + "'";
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

    public boolean existeUsuarioUID(String UID) {
        SQLiteDatabase db = getReadableDatabase();
        String orden = "SELECT idUsuario FROM UsuarioUID WHERE idUsuario = '" + UID + "'";
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

}