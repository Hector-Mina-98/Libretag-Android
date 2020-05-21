package com.application.heccoder.libretag;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AyudaPaint extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda_paint);
    }

    public void okButton (View vista) {
        finish();
    }


    //Este es un nuevo cambio en el proyecto
}
