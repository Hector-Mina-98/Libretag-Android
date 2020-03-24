package com.application.heccoder.libretag;

import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class GeneraTexto {

    public static void genTXT(Proyecto proyecto, String nombreArchivo) {

        File carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/compresion");
        if (!carpeta.exists()) carpeta.mkdirs();

        String path2 = Environment.getExternalStorageDirectory() + File.separator + "Libretag/compresion"
                + File.separator + nombreArchivo + "_TEXTO.txt";

        try {
            File archivo = new File(path2);
            FileWriter writer = new FileWriter(archivo);

            writer.append("Hola Amigui");
            writer.append(" Como estas");
            writer.append("\n¡HAY ").append(String.valueOf(proyecto.getFiguras().size())).append(" FIGURAS!");

            writer.flush();
            writer.close();


            zipFolder(Environment.getExternalStorageDirectory() + File.separator + "Libretag/compresion",
                    Environment.getExternalStorageDirectory() + File.separator + "Libretag/out.zip");

            //unpackZip(Environment.getExternalStorageDirectory() + File.separator + "Libretag/"
              //      ,"out.zip", Environment.getExternalStorageDirectory() + File.separator + "Libretag/SALIDA");

        } catch (Exception e) {
            Log.e("TXT_ARCHIVO","no se creó un carajo");
        }

    }


    public static void dataCurrent(Proyecto proyecto, ConstraintCanvas constraintCanvas) { //proceso que se da al momento de guardar un archivo
        //crea una carpeta Libretag/data/current
        File carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/data/current");
        if (!carpeta.exists()) carpeta.mkdirs();

        //crea el archivo principal
        //crearPrincipal(proyecto);

        //leerPrincipal(Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current" + File.separator + "main.txt");
    }


    /* ESTE METODO ES LLAMADO AL MOMENTO DE INTERRUMPIR LA APP DE CUALQUIER FORMA
    GUARDA SOLO EL ARCHIVO TEMPORAL (ESTRUCTURA Y TEXTOS) EN DATA/CURRENT
    */
    public static void crearPrincipal(Proyecto proyecto, String path) {
        String pathCarpeta;
        File carpeta;
        if (path.equals("")) {
            carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/data/current");
            if (!carpeta.exists()) carpeta.mkdirs();
            pathCarpeta = Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current";
        } else {
            carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/data/temp");
            if (!carpeta.exists()) carpeta.mkdirs();
            pathCarpeta = Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/temp";
        }

        try {
            File archivo = new File(pathCarpeta + File.separator + "main.txt");
            FileWriter writer = new FileWriter(archivo);

            //caracteristicas del proyecto
            writer.append("<LibretagProject");
            writer.append("\n\tproject:width=").append('"').append(String.valueOf(proyecto.getW_canvas())).append('"');
            writer.append("\n\tproject:height=").append('"').append(String.valueOf(proyecto.getH_canvas())).append('"');
            writer.append("\n\tproject:grid=").append('"').append(String.valueOf(proyecto.getTipoCuadricula())).append('"');
            writer.append(">");
            //atributos de las figuras
            if (proyecto.getFiguras() != null) {
                for (Figura figura : proyecto.getFiguras()) {
                    writer.append("\n\n\t<").append((figura.getTipoVista()==Figura.IMAGE)?"IMAGE":"TEXT");
                    writer.append("\n\t\tfigure:id=").append('"').append(String.valueOf(figura.getIdFigura())).append('"');
                    writer.append("\n\t\tfigure:pos_x=").append('"').append(String.valueOf(figura.getPosX())).append('"');
                    writer.append("\n\t\tfigure:pos_y=").append('"').append(String.valueOf(figura.getPosY())).append('"');
                    writer.append("\n\t\tfigure:rotation=").append('"').append(String.valueOf(figura.getRot())).append('"');
                    if (figura.getTipoVista()==Figura.IMAGE) {
                        writer.append("\n\t\tfigure:width=").append('"').append(String.valueOf(figura.get_width())).append('"');
                        writer.append("\n\t\tfigure:height=").append('"').append(String.valueOf(figura.get_height())).append('"');
                        writer.append("\n\t\tfigure:scale_x=").append('"').append(String.valueOf(figura.getScaX())).append('"');
                        writer.append("\n\t\tfigure:scale_y=").append('"').append(String.valueOf(figura.getScaY())).append('"');
                        writer.append("\n\t\tfigure:image_alpha=").append('"').append(String.valueOf(figura.getImageAlpha())).append('"');
                    } else {
                        crearArchivoTxt(figura.getContenidoTexto(),figura.getIdFigura());
                        writer.append("\n\t\tfigure:text_size=").append('"').append(String.valueOf(figura.getText_size())).append('"');
                        writer.append("\n\t\tfigure:text_color=").append('"').append(String.valueOf(figura.getText_color())).append('"');
                        writer.append("\n\t\tfigure:text_background=").append('"').append(String.valueOf(figura.getText_background())).append('"');
                        writer.append("\n\t\tfigure:text_alignment=").append('"').append(String.valueOf(figura.getText_alignment())).append('"');
                        writer.append("\n\t\tfigure:text_style=").append('"').append(String.valueOf(figura.getText_style())).append('"');
                        writer.append("\n\t\tfigure:text_width=").append('"').append(String.valueOf(figura.getText_width())).append('"');
                    }
                    writer.append(" />");
                }
            }

            writer.append("\n\n</LibretagProject>");

            //cerrando principal
            writer.flush();
            writer.close();

            if (!path.equals("")) {
                //creando archivo comprimido en la carpeta de archivos libretag
                zipFolder(pathCarpeta, path);
                //borrando los archivos temporales
                borrarArchivosDeCarpeta(pathCarpeta);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CrearPrincipal",e.getMessage());
        }
    }

    public static void crearArchivoImg(byte[] byteArray, int idFigura) { //proceso que se da al momento de editar la imagen
        try {

            File carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/data/current");
            if (!carpeta.exists()) carpeta.mkdirs();


            String path2 = Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current"
                    + File.separator + String.valueOf(idFigura) + ".PNG";

            File archivo = new File(path2);
            FileOutputStream fos = new FileOutputStream(archivo);
            fos.write(byteArray);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void crearArchivoTxt(String text, int idFigura) {
        try {

            File carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/data/current");
            if (!carpeta.exists()) carpeta.mkdirs();


            String path2 = Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current"
                    + File.separator + String.valueOf(idFigura) + ".TXT";

            File archivo = new File(path2);
            FileWriter writer = new FileWriter(archivo);

            writer.append(text);

            Log.d("TXT_ARCHIVO", "---ESCRIBIENDO---");
            if (text != null) Log.d("TXT_ARCHIVO", text);

            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void actualizarArchivoOriginal (String nombreArchivo) {
        zipFolder(Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current",
                Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos/" + nombreArchivo );
    }








    public static Proyecto leerPrincipal(String path) {
        Proyecto proyecto = new Proyecto();
        ArrayList<Figura> figuras = new ArrayList<>();

        File archivo = new File(path);
        String line;
        boolean abierto;
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(archivo)));

            StringBuilder stringBuilder = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            line = stringBuilder.toString();

            abierto = false;
            stringBuilder.delete(0, stringBuilder.length());

            //leyendo los bloques
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) == '>') {
                    abierto = false;
                    interpretarBloque(stringBuilder, figuras, proyecto);
                    stringBuilder.delete(0, stringBuilder.length());
                }
                else if (abierto) stringBuilder.append(line.charAt(i));
                else if (line.charAt(i) == '<') abierto = true;
            }

            proyecto.setFiguras(figuras);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TXT_ARCHIVO","ERROR AL LEER");
            Log.e("TXT_ARCHIVO", e.getMessage());
            return null;
        }

        return proyecto;
    }


    public static void interpretarBloque(StringBuilder str, ArrayList<Figura> figuras, Proyecto proyecto) {
        Figura figura;

        String valor;

        if (str.toString().contains("/LibretagProject")){
            Log.d("TXT_ARCHIVO","--Lectura finalizada");

        } else if (str.toString().contains("LibretagProject")) {
            valor = encontrarValor(str.toString(),"project:width=");
            proyecto.setW_canvas(Integer.parseInt(valor));
            valor = encontrarValor(str.toString(),"project:height=");
            proyecto.setH_canvas(Integer.parseInt(valor));
            valor = encontrarValor(str.toString(),"project:grid=");
            proyecto.setTipoCuadricula(Integer.parseInt(valor));

        } else if (str.toString().contains("TEXT")) {
            valor = encontrarValor(str.toString(),"figure:id=");
            figura = new Figura(null,Figura.TEXT_VIEW,null,Integer.parseInt(valor));

            File carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/data/current");
            if (!carpeta.exists()) carpeta.mkdirs();
            File[] archivos = carpeta.listFiles();
            for (int i = 0; i < archivos.length; i++) {
                if (archivos[i].getName().equalsIgnoreCase(valor + ".TXT")) {
                    try {
                        String line;
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(archivos[i])));
                        StringBuilder stringBuilder = new StringBuilder();
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line).append("\n");
                        }
                        if (stringBuilder.length() > 0) figura.setContenidoTexto(stringBuilder.substring(0,stringBuilder.length()-1));
                        i = archivos.length;

                        Log.d("TXT_ARCHIVO", "LEYENDO");
                        if (figura.getContenidoTexto() != null) Log.d("TXT_ARCHIVO", figura.getContenidoTexto());

                        valor = encontrarValor(str.toString(),"figure:pos_x=");
                        figura.setPosX(Float.parseFloat(valor));
                        valor = encontrarValor(str.toString(),"figure:pos_y=");
                        figura.setPosY(Float.parseFloat(valor));
                        figura.setScaX(1.0f);
                        figura.setScaY(1.0f);
                        valor = encontrarValor(str.toString(),"figure:rotation=");
                        figura.setRot(Float.parseFloat(valor));
                        //Nuevos Atributos de texto
                        valor = encontrarValor(str.toString(),"figure:text_size=");
                        figura.setText_size(Integer.parseInt(valor));
                        valor = encontrarValor(str.toString(),"figure:text_color=");
                        figura.setText_color(Integer.parseInt(valor));
                        valor = encontrarValor(str.toString(),"figure:text_background=");
                        figura.setText_background(Integer.parseInt(valor));
                        valor = encontrarValor(str.toString(),"figure:text_alignment=");
                        figura.setText_alignment(Integer.parseInt(valor));
                        valor = encontrarValor(str.toString(),"figure:text_style=");
                        figura.setText_style(Integer.parseInt(valor));
                        valor = encontrarValor(str.toString(),"figure:text_width=");
                        figura.setText_width(Integer.parseInt(valor));
                        //Log.d("TEXT_WIDTH","valor = " + valor);

                        figuras.add(figura);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("TXT_ARCHIVO","ERROR AL ESTABLECER CONTENIDO DE TEXTO");
                        Log.e("TXT_ARCHIVO", e.getMessage());
                    }
                }
            }


        } else if (str.toString().contains("IMAGE")) {
            valor = encontrarValor(str.toString(),"figure:id=");
            figura = new Figura(null,Figura.IMAGE,null,Integer.parseInt(valor));

            File carpeta = new File(Environment.getExternalStorageDirectory(), "Libretag/data/current");
            if (!carpeta.exists()) carpeta.mkdirs();
            File[] archivos = carpeta.listFiles();
            for (int i = 0; i < archivos.length; i++) {
                if (archivos[i].getName().equalsIgnoreCase(valor + ".PNG")) {
                    try {
                        byte[] byteArray = new byte[(int)archivos[i].length()];
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(archivos[i]));
                        buf.read(byteArray, 0, byteArray.length);
                        buf.close();
                        figura.setByteArray(byteArray);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("TXT_ARCHIVO","ERROR AL ESTABLECER CONTENIDO DE TEXTO");
                        Log.e("TXT_ARCHIVO", e.getMessage());
                    }
                }
            }

            valor = encontrarValor(str.toString(),"figure:pos_x=");
            figura.setPosX(Float.parseFloat(valor));
            valor = encontrarValor(str.toString(),"figure:pos_y=");
            figura.setPosY(Float.parseFloat(valor));
            valor = encontrarValor(str.toString(),"figure:scale_x=");
            figura.setScaX(Float.parseFloat(valor));
            valor = encontrarValor(str.toString(),"figure:scale_y=");
            figura.setScaY(Float.parseFloat(valor));
            valor = encontrarValor(str.toString(),"figure:rotation=");
            figura.setRot(Float.parseFloat(valor));
            valor = encontrarValor(str.toString(),"figure:width=");
            figura.set_dim(Integer.parseInt(valor),0);
            valor = encontrarValor(str.toString(),"figure:height=");
            figura.set_dim(figura.get_width(),Integer.parseInt(valor));
            valor = encontrarValor(str.toString(),"figure:image_alpha=");
            figura.setImageAlpha(Integer.parseInt(valor));

            figuras.add(figura);
        }

    }


    private static String encontrarValor(String completo, String fragmento) {
        int[] rango = new int[2];

        if (fragmento.length() < completo.length() && completo.contains(fragmento)) {

            for (int i = 0; i < (completo.length()-fragmento.length()); i++) { //buscando inicio
                if (completo.substring(i,i + fragmento.length()).equals(fragmento)) {
                    rango[0] = i + fragmento.length();
                    i = completo.length();
                }
            }
            rango[0] += 1; //para igonrar las comillas dobles del principio
            for (int i = rango[0]; i < completo.length(); i++) { //buscando fin
                if(completo.charAt(i) == '"') {
                    rango[1] = i;
                    i = completo.length();
                }
            }

        }

        String valor;
        //Log.d("TEXT_WIDTH","R1 = " + rango[0] + "     R2 = " + rango[1]);
        //Log.d("TEXT_WIDTH",completo.substring(rango[0] - 5,rango[0] + 5));
        if (rango[0] != rango[1]) valor = completo.substring(rango[0],rango[1]);
        else valor = "0";

        return valor;
    }





    public static void borrarRecursosObsoletosCurrent () {
        File carpeta = new File(Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current");
        Proyecto proyecto = GeneraTexto.leerPrincipal(Environment.getExternalStorageDirectory()
                + File.separator + "Libretag/data/current" + File.separator + "main.txt");
        boolean utilizado;
        if (carpeta.exists()) {
            File[] archivos = carpeta.listFiles();
            for (int i = 0; i < archivos.length; i++) {
                utilizado = false;
                for (int j = 0; j < proyecto.getFiguras().size(); j++) {
                    if (!archivos[i].getName().equals("main.txt")) {
                        if ( String.valueOf(proyecto.getFiguras().get(j).getIdFigura())
                                .equals(archivos[i].getName().substring(0,archivos[i].getName().length()-4)) ) {
                            utilizado = true;
                            j = proyecto.getFiguras().size();
                        }
                    }
                }
                if (!utilizado && !archivos[i].getName().equals("main.txt")) archivos[i].delete();
            }
        }
    }




    public static void borrarArchivosDeCarpeta (String pathCarpeta) {
        File carpeta = new File(pathCarpeta);
        if (carpeta.exists()) {
            File[] archivos = carpeta.listFiles();
            for (int i = 0; i < archivos.length; i++) {
                archivos[i].delete();
            }
        }
    }


    private static void zipFolder(String inputFolderPath, String outZipPath) {
        try {
            FileOutputStream fos = new FileOutputStream(outZipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File(inputFolderPath);
            File[] files = srcFile.listFiles();
            Log.d("", "Zip directory: " + srcFile.getName());
            if (Workspace.barraProgresoTitulo != null) Workspace.barraProgresoTitulo.setMax(files.length + 1);
            for (int i = 0; i < files.length; i++) {
                Log.d("", "Adding file: " + files[i].getName());
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(files[i]);
                zos.putNextEntry(new ZipEntry(files[i].getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
                if (Workspace.hiloGuardado != null) {
                    Workspace.hiloGuardado.onProgressUpdate(i);
                }
            }
            zos.close();
        } catch (IOException ioe) {
            Log.e("TXT_ARCHIVO","ERROR AL COMPRIMIR");
            Log.e("TXT_ARCHIVO", ioe.getMessage());
        }
    }


    public static void unpackZip(String pathZip, String outPath) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(pathZip);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            File carpeta = new File(outPath);
            if (!carpeta.exists()) carpeta.mkdirs();
            outPath = outPath + "/";

            int i = new ZipFile(pathZip).size();
            if (Workspace.barraProgresoTitulo != null) Workspace.barraProgresoTitulo.setMax(i);
            i = 0;

            while ((ze = zis.getNextEntry()) != null) {

                filename = ze.getName();
                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(outPath + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(outPath + filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();

                if (Workspace.hiloPrimeraCarga != null) {
                    Workspace.hiloPrimeraCarga.onProgressUpdate(i);
                }
                i++;

            }

            zis.close();
        }
        catch(Exception ioe) {
            Log.e("TXT_ARCHIVO","ERROR AL DESCOMPRIMIR");
            Log.e("TXT_ARCHIVO", ioe.getMessage());
        }

    }

}
