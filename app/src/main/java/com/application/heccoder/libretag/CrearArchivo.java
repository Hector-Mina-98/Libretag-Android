package com.application.heccoder.libretag;

import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CrearArchivo extends AppCompatActivity {

    EditText et;
    Button ok;
    TextView titulo;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_archivo);

        et = findViewById(R.id.et_CA);
        ok = findViewById(R.id.button_CA_OK);

        titulo = findViewById(R.id.tv_titulo_CA);
        logo = findViewById(R.id.CA_logo);

        Bundle paquete = getIntent().getExtras();

        if (paquete.get("ACCION_NOMBRE").equals("NUEVO_ARCHIVO")) {
            titulo.setText("Nueva Libreta");
            logo.setImageResource(R.drawable.nota_simple);

        } else if (paquete.get("ACCION_NOMBRE").equals("RENOMBRAR_ARCHIVO")) {
            titulo.setText("Renombrar");
            logo.setImageResource(R.drawable.main_rename_file);
            et.setText(paquete.get("NOMBRE_ACTUAL").toString().
                    substring(0, paquete.get("NOMBRE_ACTUAL").toString().length() - 5 ));
        }

        ok.setEnabled(false);

        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ok.setEnabled( !et.getText().toString().equals("") );
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ok.setEnabled( !et.getText().toString().equals("") );
            }

            @Override
            public void afterTextChanged(Editable s) {
                ok.setEnabled( !et.getText().toString().equals("") );
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( readfromFolder() )
                    crearArchivo();
            }
        });
    }




    private void crearArchivo() {

        Proyecto proyecto = new Proyecto();
        proyecto.setW_canvas(-101101);

        try {
            File carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/archivos");
            boolean isCreada = carpeta.exists();

            if (!isCreada) {
                isCreada = carpeta.mkdirs();
            }

            if (getIntent().getExtras().get("ACCION_NOMBRE").equals("NUEVO_ARCHIVO")) {
                String nombreArchivo = "";

                if (isCreada) {
                    nombreArchivo = textoTemp + ".hltg";
                }

                String path = Environment.getExternalStorageDirectory() + File.separator
                        + "Libretag/archivos" + File.separator + nombreArchivo;

                /*File archivo = new File(path);
                FileOutputStream fos = new FileOutputStream(archivo);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(proyecto);
                oos.close();
                fos.close();*/
                GeneraTexto.crearPrincipal(proyecto,path);

                openLibreta(nombreArchivo);

            } else  if (getIntent().getExtras().get("ACCION_NOMBRE").equals("RENOMBRAR_ARCHIVO")) {
                String path = Environment.getExternalStorageDirectory() + File.separator
                        + "Libretag/archivos";

                File from = new File(path, getIntent().getExtras().get("NOMBRE_ACTUAL").toString() );
                File to = new File(path, textoTemp + ".hltg");

                if (from.exists()) from.renameTo(to);

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e("CrearArchivo","  " + e.getMessage());
        }

    }

    String textoTemp;

    public boolean readfromFolder () {

        String path = Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos";

        try {
            File carpeta = new File(path);

            if (!carpeta.exists()) carpeta.mkdirs();

            File [] archivos = carpeta.listFiles();

            textoTemp = et.getText().toString().trim();

            for (int i = 0; i < archivos.length; i ++) {

                if ( (textoTemp + ".hltg").toLowerCase().equals(archivos[i].getName().toLowerCase())) {
                    et.setError("Nombre ya existente, use otro");
                    et.requestFocus();
                    return false;

                }
            }


            for (int j = 0; j < textoTemp.length(); j ++) {
                if (textoTemp.charAt(j) == '/' || textoTemp.charAt(j) == ':' ||
                        textoTemp.charAt(j) == '*' || textoTemp.charAt(j) == '?' ||
                        textoTemp.charAt(j) == '<' || textoTemp.charAt(j) == '>' || textoTemp.charAt(j) == '|' ||
                        textoTemp.charAt(j) == '"' ||textoTemp.charAt(j) == 92/*BACKSLASH*/

                            /*textoTemp.charAt(j) == '{' || textoTemp.charAt(j) == '}' ||
                            textoTemp.charAt(j) == '[' || textoTemp.charAt(j) == ']' ||
                            textoTemp.charAt(j) == 39/*APOSTROFE || textoTemp.charAt(j) == '}' ||
                            textoTemp.charAt(j) == '´' || textoTemp.charAt(j) == '`'*/) {

                    et.setError("El nombre contiene caracteres inválidos");
                    et.requestFocus();
                    return false;
                }

            }

            return true;

        } catch (Exception e) {
            Toast.makeText(getApplication(), "Carpeta no encontrada", Toast.LENGTH_SHORT).show();
            Log.e("CrearArchivo","  " + e.getMessage());
            return false;
        }


    }


    private void openLibreta (String nombreArchivo) {
        Workspace.guardado = true;

        //TODO encerrar esta instruccion en un hilo diferente
        GeneraTexto.borrarArchivosDeCarpeta(
                Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current");

        Intent intent = new Intent(this, Workspace.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("ARCHIVO",nombreArchivo);
        startActivity(intent);
        finish();
    }
}
