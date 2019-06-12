package com.diurno.dam2.lyricalnotes;

import android.view.View;

public interface RecyclerViewClickListener {
    void onRowClicked(int position);
    void onViewClicked(View v, int position);
    void onLongClick(View v, int position);
}
