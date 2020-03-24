package com.application.heccoder.libretag;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DimCanvas extends AppCompatActivity {

    EditText et_DC_with, et_DC_height;
    Button ok;

    Proyecto proyecto;
    String nombreArchivo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dim_canvas);

        et_DC_with = findViewById(R.id.et_DC_width);
        et_DC_height = findViewById(R.id.et_DC_height);
        ok = findViewById(R.id.button_DC_OK);


        nombreArchivo = (String) getIntent().getExtras().get("ARCHIVO_DIMCANVAS");

        try {

            proyecto = GeneraTexto.leerPrincipal(
                    Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current/main.txt");

            et_DC_with.setText("" +proyecto.getW_canvas());
            et_DC_height.setText("" + proyecto.getH_canvas());

            //Toast.makeText(getApplication(), "ACTUAL: W = " + proyecto.getW_canvas() + "   H = " + proyecto.getH_canvas(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(getApplication(), "Arhivo no encontrado o dañado para leer dimensiones", Toast.LENGTH_SHORT).show();
        }

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redimensionar();
            }
        });
    }


    public void redimensionar () {
        for (int i = 0; i < Workspace.constraintCanvas.getFiguras().size(); i++) {
            if (Workspace.constraintCanvas.getFiguras().get(i).getTipoVista() == Figura.IMAGE) {
                if (((ImageView)Workspace.constraintCanvas.getFiguras().get(i).getVista()).getDrawable() instanceof BitmapDrawable) {
                    Bitmap bm = ((BitmapDrawable)((ImageView)Workspace.constraintCanvas.getFiguras().get(i).getVista()).getDrawable()).getBitmap();
                    bm = Bitmap.createScaledBitmap(bm,1,1,true);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                    ((ImageView)Workspace.constraintCanvas.getFiguras().get(i).getVista()).
                            setImageBitmap(BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.toByteArray().length));
                }
            }
        }



        proyecto.setW_canvas(Integer.parseInt(et_DC_with.getText().toString()));
        proyecto.setH_canvas(Integer.parseInt(et_DC_height.getText().toString()));

        try {
            GeneraTexto.crearPrincipal(proyecto,"");

            Toast.makeText(getApplication(), "DIMENSIONES CAMBIADAS", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), "Error al guardar las dimensiones en el archivo", Toast.LENGTH_SHORT).show();
        }

        Workspace.dimensiones_cambiadas = true;

        Intent intent = new Intent(this, Workspace.class);
        intent.putExtra("ARCHIVO", nombreArchivo);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Workspace.setGuardado(false);
        startActivity(intent);

    }
}
