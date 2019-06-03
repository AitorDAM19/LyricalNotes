package com.diurno.dam2.lyricalnotes;

import java.io.Serializable;

public class Audio implements Serializable {
    private String data;
    private String title;
    private String fecha;

    public Audio(String data, String title, String fecha) {
        this.data = data;
        this.title = title;
        this.fecha = fecha;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
