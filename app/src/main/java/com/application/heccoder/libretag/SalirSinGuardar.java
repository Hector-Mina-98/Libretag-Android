package com.application.heccoder.libretag;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SalirSinGuardar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.salir_sin_guardar);

        clickEnGuardar = false;

        if (Workspace.hgSSG == null) finish();
    }


    public void actionButtons (View view) {

        switch (view.getId()) {

            case R.id.SSG_si:
                Workspace.hgSSG.execute();
                clickEnGuardar = true;
                finish();
                break;

            case R.id.SSG_no:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                break;

            case R.id.SSG_cancelar:
                finish();
                break;

        }

    }

    boolean clickEnGuardar;

    @Override
    protected void onStop() {
        if (!clickEnGuardar) {
            Workspace.hgSSG = null;
            Workspace.hiloGuardado = null;
        }
        super.onStop();
    }
}
