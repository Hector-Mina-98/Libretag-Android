package com.application.heccoder.libretag;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    File[] archivos;
    String path;

    LinearLayout vacio, linear_archivos;
    FrameLayout linear_file_options;

    View nuevo_img, img_ren_file, img_del_file, img_share;

    TextView tv_file_options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        validarPermisos();

        abrirDesdeOtrasApps(getIntent());

        vacio = findViewById(R.id.main_vacio);
        linear_archivos = findViewById(R.id.main_linear_recientes);
        nuevo_img = findViewById(R.id.main_nuevo_img);

        linear_file_options = findViewById(R.id.linear_file_options);
        tv_file_options = findViewById(R.id.main_tv_file_options);

        img_del_file = findViewById(R.id.main_img_del_file);
        img_ren_file = findViewById(R.id.main_img_ren_file);
        img_share = findViewById(R.id.main_img_share);

        nuevo_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearArchivo("NUEVO_ARCHIVO");
            }
        });

        linear_file_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linear_file_options.setVisibility(View.GONE);
            }
        });

        img_del_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File archivo = new File(Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos" +
                        File.separator + tv_file_options.getText().toString());
                archivo.delete();
                Toast.makeText(getApplication(), "Eliminado: " + tv_file_options.getText().toString(), Toast.LENGTH_SHORT).show();
                linear_file_options.setVisibility(View.GONE);
                tv_file_options.setText("---");
                readfromFolder();
            }
        });

        img_ren_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearArchivo("RENOMBRAR_ARCHIVO");
                readfromFolder();
            }
        });

        img_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compartir();
            }
        });

        readfromFolder();
    }

    public void crearArchivo(String accion) {
        if (validarPermisos()) {
            Intent intent = new Intent(this, CrearArchivo.class);
            intent.putExtra("ACCION_NOMBRE",accion);
            intent.putExtra("NOMBRE_ACTUAL",tv_file_options.getText().toString());
            startActivity(intent);

        } else {
            Toast.makeText(this, "Debe conceder los permisos para un correcto funcionamiento", Toast.LENGTH_SHORT).show();
        }
    }


    public void readfromFolder () {
        path = Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos";

        try {
            linear_archivos.removeAllViews();

            File carpeta = new File(path);

            if (!carpeta.exists()) carpeta.mkdirs();

            archivos = carpeta.listFiles();
            //Toast.makeText(getApplication(), "Archivos: " + archivos.length, Toast.LENGTH_SHORT).show();

            if (archivos.length > 0) {

                for (int i = 0; i < archivos.length; i++) {
                    View item_archivo = LayoutInflater.from(this).inflate(R.layout.contenedor_archivo, linear_archivos, false);
                    ((TextView) item_archivo.findViewById(R.id.tv_nombre_archivo)).setText("" + archivos[i].getName());

                    ((TextView) item_archivo.findViewById(R.id.tv_descripcion_archivo))
                            .setText( String.format("%.4f", (archivos[i].length()/(1024.0f*1024.0f))) + " MB");
                    linear_archivos.addView(item_archivo);


                    item_archivo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openLibreta(view);
                        }
                    });

                    item_archivo.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            linear_file_options.setVisibility(View.VISIBLE);
                            tv_file_options.setText(((TextView)view.findViewById(R.id.tv_nombre_archivo)).getText().toString());

                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            //Vibrate for 50 milliseconds
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(50,VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                //deprecated in API 26
                                v.vibrate(50);
                            }
                            return true;    //TRUE para que NO ejecute el onClick normal, FALSE para que SI lo haga
                        }
                    });
                }

            } else {
                linear_archivos.addView(vacio);
                vacio.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            Toast.makeText(getApplication(), "Carpeta no encontrada", Toast.LENGTH_SHORT).show();
        }

    }


    public void openLibreta (View item_archivo) {
        Workspace.guardado = true;

        //TODO encerrar esta instruccion en un hilo diferente
        GeneraTexto.borrarArchivosDeCarpeta(
                Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current");

        Intent intent = new Intent(this, Workspace.class);
        intent.putExtra("ARCHIVO",((TextView)item_archivo.findViewById(R.id.tv_nombre_archivo)).getText().toString());
        startActivity(intent);

    }

    @Override
    protected void onPause() {
        //readfromFolder();
        super.onPause();
    }

    @Override
    protected void onStop() {
        //readfromFolder();
        super.onStop();
    }

    @Override
    protected void onResume() {
        readfromFolder();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (linear_file_options.getVisibility() == View.VISIBLE) linear_file_options.setVisibility(View.GONE);
        else super.onBackPressed();
    }



    // ===================== VALIDACION DE PERMISOS =============================================
    private boolean validarPermisos() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true; //retorna true cuando Android es menor a 6.0

        //CAMERA -> importar permissions Manifest.CAMERA (el que NO dice group)
        if ( (checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) ) return true;

        //si se da cuenta de que deberia volver a solicitar los permisos...
        if ( (shouldShowRequestPermissionRationale(CAMERA)) ||
                (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) ||
                (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) ) cargarDialogoRecomendacion();
        else requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA,READ_EXTERNAL_STORAGE},200);


        return false;
    }

    private void cargarDialogoRecomendacion() {

        AlertDialog.Builder dialogo = new AlertDialog.Builder(MainActivity.this);
        dialogo.setTitle("Permisos desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la aplicación");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA,READ_EXTERNAL_STORAGE},200);
            }
        });
        dialogo.show();

    }


    // ===================== COMPARTIR =============================================

    private void compartir() {
        String myFilePath = Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos" +
                File.separator + tv_file_options.getText().toString();

        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        File fileWithinMyDir = new File(myFilePath);

        if(fileWithinMyDir.exists()) {
            intentShareFile.setType("application/pdf");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + myFilePath));

            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Sharing File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

            startActivity(Intent.createChooser(intentShareFile, "Share File"));

            linear_file_options.setVisibility(View.GONE);
            tv_file_options.setText("---");
            readfromFolder();
        }
    }



    // ===================== ABRIR DESDE OTRAS APPS =============================================

    //TODO este proceso deberia ejecutarse en otro hilo
    private void abrirDesdeOtrasApps (Intent intent) {

        String action = intent.getAction();

        String name = "";

        if ( action != null ) {
            if (action.compareTo(Intent.ACTION_VIEW) == 0) {
                String scheme = intent.getScheme();
                ContentResolver resolver = getContentResolver();

                try {
                    if (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0) {
                        Uri uri = intent.getData();
                        name = getContentName(resolver, uri);

                        Log.d("MainActivity-int-fil" , "Content intent detected: "
                                + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
                        if (!archivoYaImportado(uri.getPath())) {
                            //Toast.makeText(this, "Importando: " + uri.getPath(), Toast.LENGTH_SHORT).show();
                            InputStream input = resolver.openInputStream(uri);
                            name = renombrarNombreYaUsado(name);
                            String importfilepath = Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos/" + name;
                            InputStreamToFile(input, importfilepath);
                        }
                    }
                    else if (scheme.compareTo(ContentResolver.SCHEME_FILE) == 0) {
                        Uri uri = intent.getData();
                        name = uri.getLastPathSegment();

                        Log.d("MainActivity-int-fil" , "File intent detected: "
                                + action + " : " + intent.getDataString() + " : " + intent.getType() + " : " + name);
                        if (!archivoYaImportado(uri.getPath())) {
                            //Toast.makeText(this, "Importando: " + uri.getPath(), Toast.LENGTH_SHORT).show();
                            InputStream input = resolver.openInputStream(uri);
                            name = renombrarNombreYaUsado(name);
                            String importfilepath = Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos/" + name;
                            InputStreamToFile(input, importfilepath);
                        }
                    }
                    else if (scheme.compareTo("http") == 0) {
                        // TODO Import from HTTP!
                    }
                    else if (scheme.compareTo("ftp") == 0) {
                        // TODO Import from FTP!
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("MainActivity", e.getMessage());
                }


                /////////////////////////////////////////////////////////////////////////////////////////////7
                if (!name.equals("")) {
                    Workspace.guardado = true;
                    //TODO encerrar esta instruccion en un hilo diferente
                    GeneraTexto.borrarArchivosDeCarpeta(
                            Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current");

                    Intent i = new Intent(this, Workspace.class);
                    i.putExtra("ARCHIVO",name);
                    startActivity(i);
                    finish();
                }

            }
        }

    }

    private boolean archivoYaImportado (String uriPath) {

        String pathFolder = Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos";

        File folder = new File(pathFolder);

        if (!folder.exists()) folder.mkdirs();

        File[] files = folder.listFiles();

        if (files.length > 0) {

            for (File file : files) {
                if (uriPath.equals(pathFolder + "/" + file.getName())) return true;
            }

        }

        return false;
    }

    //TODO cambiar la forma de renombrar " - copia" por "(1)", "(2)"...
    private String renombrarNombreYaUsado (String name) {

        String pathFolder = Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos";

        File folder = new File(pathFolder);

        if (!folder.exists()) folder.mkdirs();

        File[] files = folder.listFiles();

        if (files.length > 0) {
            for (File file : files) {
                if (name.equalsIgnoreCase(file.getName())) {
                    //Tener cuidado con esto, ya que solo funciona si la extension es de 4 caracteres
                    return name.substring(0,name.length()-5) + " - copia.hltg";
                }
            }
        }

        return name;
    }


    private String getContentName(ContentResolver resolver, Uri uri){
        Cursor cursor = resolver.query(uri, null, null, null, null);
        cursor.moveToFirst();
        int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
        if (nameIndex >= 0) {
            return cursor.getString(nameIndex);
        } else {
            return null;
        }
    }

    private void InputStreamToFile(InputStream in, String file) {
        try {
            OutputStream out = new FileOutputStream(new File(file));

            int size;
            byte[] buffer = new byte[1024];

            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
            }

            out.close();
        }
        catch (Exception e) {
            Log.e("MainActivity", "InputStreamToFile exception: " + e.getMessage());
        }
    }

}
