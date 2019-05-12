package com.diurno.dam2.lyricalnotes;

public class Nota {
    private int idNota;
    private String titulo;
    private String letras;

    public Nota(int idNota, String titulo, String letras) {
        this.idNota = idNota;
        this.titulo = titulo;
        this.letras = letras;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getLetras() {
        return letras;
    }

    public void setLetras(String letras) {
        this.letras = letras;
    }

    public int getIdNota() {
        return idNota;
    }

    public void setIdNota(int idNota) {
        this.idNota = idNota;
    }
}
