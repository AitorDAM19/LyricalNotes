package com.diurno.dam2.lyricalnotes;

import java.io.Serializable;

public class Audio implements Serializable {
    private String data;
    private String title;

    public Audio(String data, String title) {
        this.data = data;
        this.title = title;
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
}
