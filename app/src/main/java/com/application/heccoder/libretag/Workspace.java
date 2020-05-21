package com.application.heccoder.libretag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import android.support.v7.app.AlertDialog;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

@SuppressLint("SetTextI18n")
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class Workspace extends AppCompatActivity {

    static boolean activarToasts = false;

    //////////////////////////////////////////////vistas//////////////////////////////////////////////////
    ConstraintLayout linear_activity;

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    Display display;  int width_disp, height_disp;
    Point size;

    Context contexto;

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    static ConstraintCanvas constraintCanvas;
    ZoomableLayoutContainer zoomableLayoutContainer;

    static ImageView iv;

    public static View contenedor_editor_texto;
    View ET_linear_contenido, ET_more_less, editorTexto;

    public static View contenedor_editor_imagen, EdImg_linear_contenido, EdImg_more_less;

    int pestana;


    int w_canvas, h_canvas;

    Proyecto proyecto;

    static View panel_titulo_workspace;
    static TextView titulo;
    static ProgressBar barraProgresoTitulo;

    View contenedor_editor_dibujo;
    SeekBar ED_seekbar_tamano, ED_seekbar_color_alpha;

    static int scaleBitmap = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);

        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width_disp = size.x;
        height_disp = size.y;

        //dimensiones_cambiadas = false;

        Objects.requireNonNull(getSupportActionBar()).hide();
        contexto = this;

        panel_titulo_workspace = findViewById(R.id.panel_titulo_workspace);
        titulo = findViewById(R.id.titulo_workspace);

        barraProgresoTitulo = findViewById(R.id.barraProgresoTitulo);
        barraProgresoTitulo.getProgressDrawable().setColorFilter(
                Color.WHITE, PorterDuff.Mode.SRC_IN
        );

        ((ProgressBar)findViewById(R.id.barraProgresoTemp)).getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.colorBarraProgresoTemp), PorterDuff.Mode.SRC_IN);

        linear_activity = findViewById(R.id.linear_activity);

        iv = findViewById(R.id.iv_activity);


        //cargarConfiguracionesDelProyecto();
        llamarHiloPrimeraCarga();
    }

    //onCreate2 se llama cuando se termina el hilo de la primera carga (encargado de preparar el archivo descomprimido en data/current)
    protected void onCreate2() {
        //----------------------- EDITOR IMAGEN -------------------------------------------
        contenedor_editor_imagen = findViewById(R.id.contenedor_editor_imagen);
        EdImg_linear_contenido = findViewById(R.id.EdImg_linear_contenido);
        EdImg_more_less = findViewById(R.id.EdImg_more_less);
        EdImg_seekbar_color_alpha = findViewById(R.id.EdImg_seekbar_col_alpha);
        EdImg_seekbar_color_alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colorEditorImagen(EdImg_seekbar_color_alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                colorEditorImagen(null);
            }
        });
        EdImg_seekbar_color_alpha.setMax(255);

        //----------------------- EDITOR TEXTO -------------------------------------------
        contenedor_editor_texto = findViewById(R.id.contenedor_editor_texto);
        ET_linear_contenido = findViewById(R.id.ET_linear_contenido);
        ET_more_less = findViewById(R.id.ET_more_less);
        ET_seekbar_tamano = findViewById(R.id.ET_seekbar_tamano);
        ET_seekbar_tamano.setMax(100);
        ET_seekbar_tamano.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                opcionesEditorTexto(ET_seekbar_tamano);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ((EditText)findViewById(R.id.ET_ind_tamano)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!((EditText)findViewById(R.id.ET_ind_tamano)).getText().toString().equals(""))
                    opcionesEditorTexto(findViewById(R.id.ET_ind_tamano));
            }
        });
        ET_seekbar_bg_alpha = findViewById(R.id.ET_seekbar_bg_alpha);
        ET_seekbar_bg_alpha.setMax(255);
        ET_seekbar_bg_alpha.setProgress(255);
        ET_seekbar_bg_alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                opcionesEditorTexto(ET_seekbar_bg_alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ET_seekbar_color_alpha = findViewById(R.id.ET_seekbar_color_alpha);
        ET_seekbar_color_alpha.setMax(255);
        ET_seekbar_color_alpha.setProgress(255);
        ET_seekbar_color_alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                opcionesEditorTexto(ET_seekbar_color_alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //------------------------ EDITOR DIBUJO ----------------------------------------
        contenedor_editor_dibujo = findViewById(R.id.contenedor_editor_dibujo);
        ED_estado_motion_event = findViewById(R.id.ED_estado_motion_event);
        ED_seekbar_tamano = findViewById(R.id.ED_seekbar_tamano);
        ED_seekbar_tamano.setMax(30);
        ED_seekbar_tamano.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                opcionesEditorDibujo(ED_seekbar_tamano);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ED_seekbar_color_alpha = findViewById(R.id.ED_seekbar_color_alpha);
        ED_seekbar_color_alpha.setMax(255);
        ED_seekbar_color_alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colorEditorDibujo(ED_seekbar_color_alpha);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //------------ esto oculta el teclado ----------------------
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (w_canvas > 0 && h_canvas > 0) {
            constraintCanvas = new ConstraintCanvas
                    (this, (FrameLayout) findViewById(R.id.Ppl_layout), w_canvas, h_canvas, proyecto.getTipoCuadricula());
            zoomableLayoutContainer =
                    new ZoomableLayoutContainer(this, constraintCanvas, linear_activity, width_disp, height_disp);
            linear_activity.addView(zoomableLayoutContainer);
        } else
            Toast.makeText(getApplication(), "Archivo dañado: DIMENSIONES NULAS", Toast.LENGTH_SHORT).show();

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        editorTexto = findViewById(R.id.editorTexto);

        ET_more_less.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ET_linear_contenido.getVisibility() == View.VISIBLE) aplicarCambiosEditorTexto(null);
                else abrirEditorTexto();
            }
        });

        /////////////////////////////////// CARGA DE IMAGENES ////////////////////////////////////////////////////////
        menuImagen();

        editorDibujoAbierto = false;

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        hiloCargaTerminado = false;
        findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.VISIBLE);
        try {
            cargarFigurasACanvas();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), "Archivo dañado: Error al cargar", Toast.LENGTH_SHORT).show();
        }

        //paqueteOnRestore = null;
    }



    boolean hiloCargaTerminado;

    static String nombreArchivo;

    public static HiloPrimeraCarga hiloPrimeraCarga;
    public void llamarHiloPrimeraCarga() {
        hiloCargaTerminado = false;
        findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.VISIBLE);
        try {

            barraProgresoTitulo.setProgress(0);
            hiloPrimeraCarga = new HiloPrimeraCarga();
            hiloPrimeraCarga.execute();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), "Error al cargar configuraciones del proyecto", Toast.LENGTH_SHORT).show();
        }
    }



    public void esconderTeclado (View view) {

        InputMethodManager imm = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public void cargarFigurasACanvas () {

        if (proyecto.getFiguras() != null) {
            HiloCarga hiloCarga = new HiloCarga();
            hiloCarga.execute();

        } else {
            findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.GONE);
            hiloCargaTerminado = true;
            barraProgresoTitulo.setProgress(0);
        }
        
    }


    public static HiloGuardado hiloGuardado;
    public void opciones (View vista) {
        GeneraTexto.borrarRecursosObsoletosCurrent();

        if (hiloCargaTerminado) {
            if (vista == findViewById(R.id.add_fig_text)) {
                constraintCanvas.nuevaFigura(Figura.TEXT_VIEW, "NUEVO");
                setGuardado(false);
            }
            else if (vista == findViewById(R.id.add_fig_image)) {
                constraintCanvas.nuevaFigura(Figura.IMAGE, "NUEVO");
                setGuardado(false);
            }
            else if (vista == findViewById(R.id.save_file)) {
                //finish();
                hiloCargaTerminado = false;
                findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.VISIBLE);
                guardarTodasLasFiguras();
                //GeneraTexto.actualizarArchivoOriginal(nombreArchivo);
                //setGuardado(true);
                hiloGuardado  = new HiloGuardado(); hiloGuardado.execute();
                //Toast.makeText(getApplication(), "Se guardó con éxito", Toast.LENGTH_SHORT).show();


            } else if (vista == findViewById(R.id.call_dim_canvas)) {
                guardarTodasLasFiguras();
                Intent intent = new Intent(this, DimCanvas.class);
                //setGuardado(false);
                intent.putExtra("ARCHIVO_DIMCANVAS", nombreArchivo);
                startActivity(intent);


            } else if (vista == findViewById(R.id.paint)) {

                opcionesEditorDibujo(null);

            } else if (vista == findViewById(R.id.change_grid)) {
                if (constraintCanvas.getPanelCuadricula().getVisibility() == View.VISIBLE) {
                    constraintCanvas.getPanelCuadricula().setVisibility(View.INVISIBLE);
                    constraintCanvas.getPanelCuadricula2().setVisibility(View.VISIBLE);

                } else if (constraintCanvas.getPanelCuadricula2().getVisibility() == View.VISIBLE) {
                    constraintCanvas.getPanelCuadricula().setVisibility(View.INVISIBLE);
                    constraintCanvas.getPanelCuadricula2().setVisibility(View.INVISIBLE);

                } else {
                    constraintCanvas.getPanelCuadricula().setVisibility(View.VISIBLE);
                }
                setGuardado(false);
            }
        }

    }


    //////////////////////////////////////////// EDITORES ///////////////////////////////////////////////////////////////////////

    // ================================== EDITOR DIBUJO ======================================================
    public static ImageView ED_estado_motion_event;

    public void opcionesEditorDibujo (View vista) {
        View masOpciones = findViewById(R.id.ED_scroll);

        if (vista == null) {
            LinearLayout linear_opc_ppl = findViewById(R.id.ED_linear_opc_ppl);
            int childCount = linear_opc_ppl.getChildCount();
            for (int i = 0; i < childCount; i++) {
                (linear_opc_ppl.getChildAt(i)).setBackgroundColor(Color.TRANSPARENT);
            }

            editorDibujoAbierto = true;
            //ED_estado_motion_event.setText("MOVE");
            ED_estado_motion_event.setImageResource(R.drawable.editor_dibujo_move);

            Workspace.panel_titulo_workspace.setVisibility(View.GONE);

            constraintCanvas.getPanelDibujo().setBackgroundResource(R.drawable.fondo_botones_main);
            constraintCanvas.enfoqueAuto(true);
            contenedor_editor_dibujo.setVisibility(View.VISIBLE);

            masOpciones.setVisibility(View.GONE);
            constraintCanvas.getPanelDibujo().setColor(Color.BLACK);
            colorEditorDibujo(findViewById(R.id.ED_color_negro));
            PanelDibujo.setBorrado(false);

            //seekbar tamano
            ((TextView)findViewById(R.id.ED_img_tamano)).setText("" + 5);
            ED_seekbar_tamano.setProgress(5);
            PanelDibujo.setTamanyoPunto(5);

            findViewById(R.id.ED_lapiz).setBackgroundResource(R.drawable.fondo_botones_main);

            ((ImageView)findViewById(R.id.ED_more_less)).setImageResource(R.drawable.editor_dibujo_mas_opciones);

        } else {
            switch (vista.getId()) {
                case R.id.ED_more_less:
                    if (masOpciones.getVisibility() == View.GONE) {
                        masOpciones.setVisibility(View.VISIBLE);
                        ((ImageView)findViewById(R.id.ED_more_less)).setImageResource(R.drawable.editor_dibujo_menos_opciones);
                    }
                    else {
                        masOpciones.setVisibility(View.GONE);
                        ((ImageView)findViewById(R.id.ED_more_less)).setImageResource(R.drawable.editor_dibujo_mas_opciones);
                    }
                    break;

                case R.id.ED_cerrar_editor:
                    cancelarEditorDibujo();
                    break;

                case R.id.ED_guardar:
                    editorDibujoAbierto = false;

                    Workspace.panel_titulo_workspace.setVisibility(View.VISIBLE);

                    constraintCanvas.getPanelDibujo().setBackgroundResource(0);
                    PanelDibujo.dibujo = false;
                    contenedor_editor_dibujo.setVisibility(View.GONE);

                    //====================================================
                    //creando nueva figura
                    constraintCanvas.nuevaFigura(Figura.IMAGE, "NUEVO");
                    Figura figuraDibujo = (constraintCanvas.getFiguras().get(constraintCanvas.getFiguras().size() -1) );

                    //extrayendo bitmap
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    Bitmap bitmap = Bitmap.createScaledBitmap(constraintCanvas.getPanelDibujo().getCanvasBitmap(),scaleBitmap,scaleBitmap,true);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                    figuraDibujo.setByteArray(bytes.toByteArray());
                    GeneraTexto.crearArchivoImg(bytes.toByteArray(),figuraDibujo.getIdFigura());
                    setGuardado(false);

                    //escalando la imagen al dibujo original
                    int w_bitmap = constraintCanvas.getPanelDibujo().getCanvasBitmap().getWidth();
                    int h_bitmap = constraintCanvas.getPanelDibujo().getCanvasBitmap().getHeight();
                    //Toast.makeText(getApplication(), "W: " + w_bitmap + "   H: " + h_bitmap, Toast.LENGTH_SHORT).show();
                    figuraDibujo.setScaX(w_bitmap*1.0f/200.0f);
                    figuraDibujo.setScaY(h_bitmap*1.0f/200.0f);
                    //(constraintCanvas.getFiguras().get(constraintCanvas.getFiguras().size() -1).getVista() ).setBackgroundResource(R.drawable.fondo_botones_main);

                    //ubicando la figura en el punto inicial del dibujo
                    figuraDibujo.setPosX(PanelDibujo.XsinTrim + 200.0f*figuraDibujo.getScaX()/2.0f - 200.0f/2.0f);
                    figuraDibujo.setPosY(PanelDibujo.YsinTrim + 200.0f*figuraDibujo.getScaY()/2.0f - 200.0f/2.0f);

                    //limpiando panel de dibujo
                    constraintCanvas.getPanelDibujo().NuevoDibujo();
                    guardarTodasLasFiguras();
                    break;

                case R.id.ED_borrador:
                    for (int i = 0; i < ((LinearLayout)findViewById(R.id.ED_linear_opc_ppl)).getChildCount(); i++) {
                        (((LinearLayout)findViewById(R.id.ED_linear_opc_ppl)).getChildAt(i)).setBackgroundColor(Color.TRANSPARENT);
                    }
                    findViewById(R.id.ED_borrador).setBackgroundResource(R.drawable.fondo_botones_main);
                    PanelDibujo.setBorrado(true);
                    break;
                case R.id.ED_lapiz:
                    for (int i = 0; i < ((LinearLayout)findViewById(R.id.ED_linear_opc_ppl)).getChildCount(); i++) {
                        (((LinearLayout)findViewById(R.id.ED_linear_opc_ppl)).getChildAt(i)).setBackgroundColor(Color.TRANSPARENT);
                    }
                    findViewById(R.id.ED_lapiz).setBackgroundResource(R.drawable.fondo_botones_main);
                    PanelDibujo.setBorrado(false);
                    break;

                case R.id.ED_estado_motion_event:
                    PanelDibujo.setDibujo();
                    if (PanelDibujo.dibujo) {
                        //ED_estado_motion_event.setText("DRAW");
                        ED_estado_motion_event.setImageResource(R.drawable.editor_dibujo_draw);
                    }
                    else {
                        //ED_estado_motion_event.setText("MOVE");
                        ED_estado_motion_event.setImageResource(R.drawable.editor_dibujo_move);
                    }
                    break;

                case R.id.ED_seekbar_tamano:
                    int tamano = Math.max(ED_seekbar_tamano.getProgress(), 1);
                    PanelDibujo.setTamanyoPunto(tamano);
                    ((TextView)findViewById(R.id.ED_img_tamano)).setText("" + tamano);
                    break;

                case R.id.ED_help:
                    //Toast.makeText(contexto, "AYUDA", Toast.LENGTH_SHORT).show();
                    Intent intentED_help = new Intent(this, AyudaPaint.class);
                    startActivity(intentED_help);
                    break;
            }
        }


    }

    public void colorEditorDibujo (View vista) {

        if (vista == ED_seekbar_color_alpha) {
            constraintCanvas.getPanelDibujo().setColor(ColorUtils.setAlphaComponent(
                    constraintCanvas.getPanelDibujo().getColor(), ED_seekbar_color_alpha.getProgress()
            ));

        } else {
            ED_seekbar_color_alpha.setProgress(ED_seekbar_color_alpha.getMax());
            LinearLayout linear_colores = findViewById(R.id.ED_linear_colores);
            int childCount = linear_colores.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (vista == linear_colores.getChildAt(i))
                    ((ImageView)linear_colores.getChildAt(i)).setImageResource(R.drawable.editor_dibujo_color_seleccionado);
                else ((ImageView)linear_colores.getChildAt(i)).setImageDrawable(null);
            }
            if (vista.getBackground() instanceof ColorDrawable) {
                constraintCanvas.getPanelDibujo().setColor(
                        ((ColorDrawable)vista.getBackground()).getColor()
                );
            }
        }
    }

    public void cancelarEditorDibujo () {
        editorDibujoAbierto = false;

        Workspace.panel_titulo_workspace.setVisibility(View.VISIBLE);

        constraintCanvas.getPanelDibujo().setBackgroundResource(0);
        PanelDibujo.dibujo = false;
        contenedor_editor_dibujo.setVisibility(View.GONE);

        //====================================================
        constraintCanvas.getPanelDibujo().NuevoDibujo();
    }

    public static boolean editorDibujoAbierto;

    // ================================== EDITOR TEXTO ======================================================

    TextView textoFiguraActual;
    SeekBar ET_seekbar_tamano, ET_seekbar_bg_alpha, ET_seekbar_color_alpha;

    public void abrirEditorTexto () {
        ((ImageView)ET_more_less).setImageResource(R.drawable.editor_texo_regresar);
        //Toast.makeText(contexto, "EDITOR TEXTO ABIERTO", Toast.LENGTH_SHORT).show();
        ArrayList<Figura> figuras = constraintCanvas.getFiguras();
        int idActual = constraintCanvas.getIdActual();
        for (int i = 0; i < figuras.size(); i++) {
            if (figuras.get(i).getIdFigura() == idActual &&
                    constraintCanvas.getFocoFigura().getContenedorFoco().getVisibility() == View.VISIBLE) {
                if (figuras.get(i).getTipoVista() == Figura.TEXT_VIEW){
                    textoFiguraActual = (TextView) figuras.get(i).getVista();
                    linear_activity.setEnabled(false);
                    ET_linear_contenido.setVisibility(View.VISIBLE);
                    findViewById(R.id.difuminadoTXT).setVisibility(View.VISIBLE);
                    pestana = 1;
                    //lee las caracteristicas del texto
                    ((EditText) editorTexto).setText(textoFiguraActual.getText());
                    ((EditText) editorTexto).setGravity(textoFiguraActual.getGravity());
                    if (textoFiguraActual.getTypeface() != null)
                        ((EditText) editorTexto).setTypeface(textoFiguraActual.getTypeface());
                    ET_seekbar_tamano.setProgress( (int)(textoFiguraActual.getTextSize()) );
                    //((EditText)findViewById(R.id.ET_ind_tamano)).setText( (int)(textoFiguraActual.getTextSize()) );
                    ((EditText) editorTexto).setTextColor(textoFiguraActual.getCurrentTextColor());
                    if (textoFiguraActual.getBackground() != null)
                        editorTexto.setBackgroundColor( ((ColorDrawable)textoFiguraActual.getBackground()).getColor() );
                    else editorTexto.setBackgroundColor(Color.TRANSPARENT);

                    //TODO en esta parte los seekbars de color no deberian ir al 100%, sino al valor real de opacidad
                    //ET_seekbar_bg_alpha.setProgress(255);
                    //ET_seekbar_color_alpha.setProgress(255);

                    //constraintCanvas.enfoqueAuto(true);
                }
            }
        }
    }

    public void opcionesEditorTexto (View vista) {
        switch (vista.getId()) {
            case R.id.ET_bold:
                if ( ((EditText) editorTexto).getTypeface() != null ) {
                    if ( !((EditText) editorTexto).getTypeface().isBold() ) {
                        if (((EditText) editorTexto).getTypeface().isItalic())
                            ((EditText) editorTexto).setTypeface(((EditText) editorTexto).getTypeface(), Typeface.BOLD_ITALIC);
                        else ((EditText) editorTexto).setTypeface(((EditText) editorTexto).getTypeface(), Typeface.BOLD);
                    } else {
                        if (((EditText) editorTexto).getTypeface().isItalic())
                            ((EditText) editorTexto).setTypeface(((EditText) editorTexto).getTypeface(), Typeface.ITALIC);
                        else ((EditText) editorTexto).setTypeface(null, Typeface.NORMAL);
                    }
                } else ((EditText) editorTexto).setTypeface(null, Typeface.BOLD);
                break;
            case R.id.ET_italic:
                if ( ((EditText) editorTexto).getTypeface() != null ) {
                    if ( !((EditText) editorTexto).getTypeface().isItalic() ) {
                        if (((EditText) editorTexto).getTypeface().isBold())
                            ((EditText) editorTexto).setTypeface(((EditText) editorTexto).getTypeface(), Typeface.BOLD_ITALIC);
                        else ((EditText) editorTexto).setTypeface(((EditText) editorTexto).getTypeface(), Typeface.ITALIC);
                    } else {
                        if (((EditText) editorTexto).getTypeface().isBold())
                            ((EditText) editorTexto).setTypeface(((EditText) editorTexto).getTypeface(), Typeface.BOLD);
                        else ((EditText) editorTexto).setTypeface(null, Typeface.NORMAL);
                    }
                } else ((EditText) editorTexto).setTypeface(null, Typeface.ITALIC);
                break;

            case R.id.ET_align_start:
                ((EditText) editorTexto).setGravity(Gravity.START);
                break;
            case R.id.ET_align_center:
                ((EditText) editorTexto).setGravity(Gravity.CENTER_HORIZONTAL);
                break;
            case R.id.ET_align_end:
                ((EditText) editorTexto).setGravity(Gravity.END);
                break;
            case R.id.ET_seekbar_tamano:
                int tamano = Math.max(ET_seekbar_tamano.getProgress(), 10);
                ((EditText)findViewById(R.id.ET_ind_tamano)).setText("" + tamano);
                //lo siguiente ubica el cursor al final
                ((EditText)findViewById(R.id.ET_ind_tamano)).
                        setSelection(((EditText)findViewById(R.id.ET_ind_tamano)).getText().length());
                break;
            case R.id.ET_ind_tamano:
                int tamano2 = Math.max(Integer.parseInt(((EditText)vista).getText().toString()), 10);
                tamano2 = Math.min(tamano2, 100);
                ET_seekbar_tamano.setProgress(tamano2);
                break;
            case R.id.ET_seekbar_bg_alpha:
                if (editorTexto.getBackground() != null && editorTexto.getBackground() instanceof ColorDrawable) {
                    int c = ((ColorDrawable)editorTexto.getBackground()).getColor();
                    editorTexto.setBackgroundColor(ColorUtils.setAlphaComponent(c, ((SeekBar)vista).getProgress()));
                }
                break;
            case R.id.ET_seekbar_color_alpha:
                ((EditText)editorTexto).setTextColor(ColorUtils.setAlphaComponent(
                        ((EditText)editorTexto).getCurrentTextColor(),((SeekBar)vista).getProgress()
                ));
                break;

            case R.id.ET_delete:
                borrarFigura();
                aplicarCambiosEditorTexto(null);
                break;
            case R.id.ET_link:
                break;
        }

    }

    public void colorEditorTexto (View vista) {
        int color = Color.BLACK;
        if (((ImageView)vista).getDrawable() instanceof ColorDrawable)
            color = ((ColorDrawable) ((ImageView)vista).getDrawable()).getColor();
        ((EditText)editorTexto).setTextColor(color);
    }
    public void bgEditorTexto (View vista) {
        int color = Color.TRANSPARENT;
        if (((ImageView)vista).getDrawable() instanceof ColorDrawable)
            color = ((ColorDrawable) ((ImageView)vista).getDrawable()).getColor();
        (editorTexto).setBackgroundColor(color);
    }

    public void aplicarCambiosEditorTexto (View vista) {
        ((ImageView)ET_more_less).setImageResource(R.drawable.editor_dibujo_mas_opciones);
        //Toast.makeText(contexto, "EDITOR TEXTO CERRADO", Toast.LENGTH_SHORT).show();
        ET_linear_contenido.setVisibility(View.GONE);
        findViewById(R.id.difuminadoTXT).setVisibility(View.GONE);
        linear_activity.setEnabled(true);
        pestana = 0;
        if (textoFiguraActual != null) {
            //se aplican los cambios
            textoFiguraActual.setText(((EditText) editorTexto).getText().toString());
            textoFiguraActual.setGravity(((EditText) editorTexto).getGravity());
            textoFiguraActual.setTypeface(((EditText) editorTexto).getTypeface());
            textoFiguraActual.setTextSize(TypedValue.COMPLEX_UNIT_PX, Math.max(ET_seekbar_tamano.getProgress(), 10));
            textoFiguraActual.setTextColor(((EditText) editorTexto).getCurrentTextColor());
            if (editorTexto.getBackground() != null)
                textoFiguraActual.setBackgroundColor( ((ColorDrawable)editorTexto.getBackground()).getColor() );
            else textoFiguraActual.setBackgroundColor(Color.TRANSPARENT);
            //se cierra el editor
            textoFiguraActual = null;
            constraintCanvas.enfoqueAuto(true);
            esconderTeclado(editorTexto);
            setGuardado(false);
        }
    }


    // ================================== EDITOR IMAGEN ======================================================

    public static ImageView imagenFiguraActual;
    SeekBar EdImg_seekbar_color_alpha;

    public void opcionesEditorImagen (View vista) {

        switch (vista.getId()) {
            case R.id.EdImg_more_less:
                if (EdImg_linear_contenido.getVisibility() == View.VISIBLE) {
                    EdImg_linear_contenido.setVisibility(View.GONE);
                    ((ImageView)EdImg_more_less).setImageResource(R.drawable.editor_dibujo_mas_opciones);
                } else {
                    EdImg_linear_contenido.setVisibility(View.VISIBLE);
                    ((ImageView)EdImg_more_less).setImageResource(R.drawable.editor_dibujo_menos_opciones);
                    if (imagenFiguraActual != null) EdImg_seekbar_color_alpha.setProgress(imagenFiguraActual.getImageAlpha());
                }
                break;
            case R.id.EdImg_choose_img:
                if (imagenFiguraActual != null) cargarImg();
                break;
            case R.id.EdImg_delete:
                borrarFigura();
                break;
        }

    }

    public void colorEditorImagen (View vista) {

        if (vista == EdImg_seekbar_color_alpha && imagenFiguraActual != null) {
            imagenFiguraActual.setImageAlpha(EdImg_seekbar_color_alpha.getProgress());

        } else if (vista != null) {
            EdImg_seekbar_color_alpha.setProgress(EdImg_seekbar_color_alpha.getMax());
            if ((vista.getBackground() instanceof ColorDrawable) && imagenFiguraActual != null) {
                Bitmap bitmap = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor( ((ColorDrawable)vista.getBackground()).getColor() );

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

                ArrayList<Figura> figuras = constraintCanvas.getFiguras();
                for (int i = 0; i < figuras.size(); i++) {
                    if (constraintCanvas.getIdActual() == figuras.get(i).getIdFigura()
                            && constraintCanvas.getFocoFigura().getContenedorFoco().getVisibility() == View.VISIBLE) {
                        figuras.get(i).setByteArray(bytes.toByteArray());
                        GeneraTexto.crearArchivoImg(bytes.toByteArray(), figuras.get(i).getIdFigura());
                        guardarTodasLasFiguras();
                        setGuardado(false);
                    }
                }

            }

        } else {
            ArrayList<Figura> figuras = constraintCanvas.getFiguras();
            for (int i = 0; i < figuras.size(); i++) {
                if (constraintCanvas.getIdActual() == figuras.get(i).getIdFigura()
                        && constraintCanvas.getFocoFigura().getContenedorFoco().getVisibility() == View.VISIBLE) {
                    if (imagenFiguraActual != null) figuras.get(i).setImageAlpha(imagenFiguraActual.getImageAlpha());
                    setGuardado(false);
                }
            }
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void borrarFigura () {
        if (constraintCanvas.getFiguras().size() == 0)
            Toast.makeText(getApplication(), "Seleccione una figura para poder borrarla", Toast.LENGTH_SHORT).show();

        for (int i = 0; i < constraintCanvas.getFiguras().size(); i++) {
            if (constraintCanvas.getFiguras().get(i).getIdFigura() == constraintCanvas.getIdActual()
                    && constraintCanvas.getFocoFigura().getContenedorFoco().getVisibility() == View.VISIBLE) {
                constraintCanvas.getPanelFiguras().removeView(constraintCanvas.getFiguras().get(i).getContenedorFigura());
                constraintCanvas.getFiguras().remove(i);
                constraintCanvas.enfoqueAuto(true);
                guardarTodasLasFiguras();
                 if (activarToasts) Toast.makeText(getApplication(), "BORRADO. FigurasFILE: " + proyecto.getFiguras().size(), Toast.LENGTH_SHORT).show();
                i = constraintCanvas.getFiguras().size();

                setGuardado(false);

            } else if (i == constraintCanvas.getFiguras().size()-1)
                Toast.makeText(getApplication(), "Seleccione una figura para poder borrarla", Toast.LENGTH_SHORT).show();
        }
    }






    public void guardarTodasLasFiguras () {
        ArrayList<Figura> figuras = constraintCanvas.getFiguras();
        for (int i = 0; i < figuras.size(); i++) {
            figuras.get(i).setPosX(figuras.get(i).getContenedorFigura().getX());
            figuras.get(i).setPosY(figuras.get(i).getContenedorFigura().getY());

            figuras.get(i).setRot(figuras.get(i).getContenedorFigura().getRotation());

            figuras.get(i).setScaX(figuras.get(i).getContenedorFigura().getScaleX());
            figuras.get(i).setScaY(figuras.get(i).getContenedorFigura().getScaleY());

            if (figuras.get(i).getTipoVista() == Figura.IMAGE){
                figuras.get(i).set_dim(figuras.get(i).getVista().getWidth(),figuras.get(i).getVista().getHeight());
                if (figuras.get(i).get_width() == 0 || figuras.get(i).get_height() == 0) figuras.get(i).set_dim(200,200);
            }

            if (figuras.get(i).getTipoVista() == Figura.TEXT_VIEW) {
                figuras.get(i).setContenidoTexto( "" + ((TextView) figuras.get(i).getVista()).getText().toString() );
                //Nuevos Atributos de texto
                figuras.get(i).setText_size( (int) ((TextView) figuras.get(i).getVista()).getTextSize() );
                figuras.get(i).setText_color( ((TextView) figuras.get(i).getVista()).getCurrentTextColor());
                int color = Color.TRANSPARENT;
                Drawable background = figuras.get(i).getVista().getBackground();
                if (background instanceof ColorDrawable) color = ((ColorDrawable) background).getColor();
                figuras.get(i).setText_background( color );
                figuras.get(i).setText_alignment(((TextView) figuras.get(i).getVista()).getGravity());
                if (((TextView)figuras.get(i).getVista()).getTypeface() != null)
                    figuras.get(i).setText_style(((TextView)figuras.get(i).getVista()).getTypeface().getStyle());
                figuras.get(i).setText_width(figuras.get(i).getVista().getWidth());
            }

        }

        proyecto.setFiguras(constraintCanvas.getFiguras());
        proyecto.setW_canvas(w_canvas);
        proyecto.setH_canvas(h_canvas);

        if (constraintCanvas.getPanelCuadricula().getVisibility() == View.VISIBLE) proyecto.setTipoCuadricula(Cuadricula.CUADRICULADO);
        else if (constraintCanvas.getPanelCuadricula2().getVisibility() == View.VISIBLE) proyecto.setTipoCuadricula(Cuadricula.RAYADO);
        else proyecto.setTipoCuadricula(Cuadricula.NINGUNO);

        try {
            GeneraTexto.crearPrincipal(proyecto,"");
            if (activarToasts)
                if(hiloCargaTerminado)
                    Toast.makeText(getApplication(), "Se guardó con éxito", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplication(), "ERROR AL GUARDAR", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (zoomableLayoutContainer != null){
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ||
                    newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

                display = getWindowManager().getDefaultDisplay();
                size = new Point();
                display.getSize(size);
                this.width_disp = size.x;
                this.height_disp = size.y;

                zoomableLayoutContainer.recenter(width_disp, height_disp);

                if (activarToasts) Toast.makeText(getApplicationContext(), "W = " + width_disp + " //// H = "
                        + height_disp, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState (Bundle paquete) {
        super.onSaveInstanceState(paquete);
        paquete.putInt("pestana",pestana);
        //Toast.makeText(getApplication(), "EDITOR ABIERTO", Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplication(), "GUARDANDO PAQUETE", Toast.LENGTH_SHORT).show();

        if (pestana == 1) {
            paquete.putInt("figuraTextoActual", constraintCanvas.getIdActual());
            //guardando las propiedades de texto del editor
            paquete.putString("contenidoEditorTexto", ((EditText)editorTexto).getText().toString());
            if ( ((EditText)editorTexto).getTypeface() != null ) paquete.putInt("estiloEditorTexto", ((EditText)editorTexto).getTypeface().getStyle());
            else paquete.putInt("estiloEditorTexto", Typeface.NORMAL);
            paquete.putInt("alineacionEditorTexto", ((EditText)editorTexto).getGravity());
            paquete.putInt("colorEditorTexto", ((EditText)editorTexto).getCurrentTextColor());
            paquete.putInt("fondoEditorTexto", (((ColorDrawable)editorTexto.getBackground()).getColor()));
            paquete.putInt("tamanoEditorTexto", ET_seekbar_tamano.getProgress());
            //Toast.makeText(getApplication(), "Texto ID: " + constraintCanvas.getIdActual(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRestoreInstanceState (Bundle paquete) {
        super.onRestoreInstanceState(paquete);
        paqueteOnRestore = paquete;
        //Toast.makeText(getApplication(), "onRestore", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        if (hiloCargaTerminado) {
            if (ET_linear_contenido != null) {
                if (ET_linear_contenido.getVisibility() == View.GONE) {
                    //Workspace.guardado = true;
                    if (!editorDibujoAbierto) {
                        if (!isGuardado()) {
                            lanzarConfirmacionSSG();
                        } else super.onBackPressed();
                    }
                    else cancelarEditorDibujo();
                } else {
                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                    );
                    aplicarCambiosEditorTexto(null);
                }

            } else if (editorDibujoAbierto) {
                cancelarEditorDibujo();

            } else {
                if (!isGuardado()) {
                    lanzarConfirmacionSSG();

                } else super.onBackPressed();
            }
        }

    }

    public static HiloGuardado hgSSG;
    public void lanzarConfirmacionSSG () {
        hgSSG = new HiloGuardado();
        hiloGuardado = hgSSG;   //Esto es porque en el proceso de compresion necesita "hiloGuardado" != null para el progressbar
        guardarTodasLasFiguras();
        Intent intent = new Intent(this, SalirSinGuardar.class);
        startActivity(intent);
    }


    public static boolean dimensiones_cambiadas = false;
    @Override
    protected void onStop() {
        if (!dimensiones_cambiadas && hiloCargaTerminado) guardarTodasLasFiguras();
        //Toast.makeText(getApplication(), "onStop", Toast.LENGTH_SHORT).show();
        dimensiones_cambiadas = false;
        super.onStop();
    }



    Bundle paqueteOnRestore;

    public void buscarTextoEnfocado () {
        if (paqueteOnRestore.getInt("pestana") == 1) {
            ((ImageView)ET_more_less).setImageResource(R.drawable.editor_texo_regresar);
            //Toast.makeText(getApplication(), "NUEVO: " + paqueteOnRestore.getInt("figuraTextoActual"), Toast.LENGTH_SHORT).show();
            ET_linear_contenido.setVisibility(View.VISIBLE);
            findViewById(R.id.difuminadoTXT).setVisibility(View.VISIBLE);
            contenedor_editor_texto.setVisibility(View.VISIBLE);
            linear_activity.setEnabled(false);
            pestana = 1;
            constraintCanvas.setIdActual(paqueteOnRestore.getInt("figuraTextoActual"));

            ArrayList<Figura> figuras = constraintCanvas.getFiguras();
            for (int i = 0; i < figuras.size(); i++) {
                if (figuras.get(i).getIdFigura() == paqueteOnRestore.getInt("figuraTextoActual")) {
                    textoFiguraActual = (TextView) figuras.get(i).getVista();
                    //Toast.makeText(getApplication(), "Texto encontrado", Toast.LENGTH_SHORT).show();

                    //recuperando propiedades del texto
                    ((EditText)editorTexto).setText(paqueteOnRestore.getString("contenidoEditorTexto"));
                    ((EditText)editorTexto).setGravity(paqueteOnRestore.getInt("alineacionEditorTexto"));
                    ((EditText)editorTexto).setTypeface(null,paqueteOnRestore.getInt("estiloEditorTexto"));
                    ((EditText)editorTexto).setTextColor(paqueteOnRestore.getInt("colorEditorTexto"));
                    editorTexto.setBackgroundColor(paqueteOnRestore.getInt("fondoEditorTexto"));
                    ET_seekbar_tamano.setProgress(paqueteOnRestore.getInt("tamanoEditorTexto"));
                }
            }

        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////// MENU IMAGEN ////////////////////////////////////////////////////////

    final int COD_SELEC = 10;
    final int COD_FOTO = 20;

    ImageView call_editor_image;

    private void menuImagen() {

        //------------------------------------ PARA ANDROID 6 -----------------------------------

        //se va a deshabilitar el boton de cargar imagen si no se han concedido los permisos
        call_editor_image = findViewById(R.id.EdImg_choose_img);
        call_editor_image.setEnabled(validarPermisos());

    }


    public void cargarImg () {

        final CharSequence[] opciones = {"Tomar foto", "Cargar imagen", "Cancelar"};
        final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(Workspace.this);
        alertOpciones.setTitle("Seleccione una opción");

        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar foto")) {
                    //Toast.makeText(getApplication(), "TOMAR FOTO", Toast.LENGTH_SHORT).show();
                    tomarfotoSDK15();

                } else if (opciones[i].equals("Cargar imagen")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/");
                    startActivityForResult(intent.createChooser(intent,"Seleccione la Aplicación"), COD_SELEC);

                } else dialogInterface.dismiss();
            }
        });
        alertOpciones.show();

    }



    private final String CARPETA_RAIZ = "Libretag/";
    private final String RUTA_IMAGEN = CARPETA_RAIZ + "Images";
    private String path;

    public void tomarfotoSDK15 () {
        File fileImagen = new File(Environment.getExternalStorageDirectory(), RUTA_IMAGEN);
        boolean isCreada = fileImagen.exists();

        String nombreImagen = "";

        if (!isCreada) {
            isCreada = fileImagen.mkdirs();
        }

        if (isCreada) {
            nombreImagen = System.currentTimeMillis()/100 + ".jpg";
        }

        path = Environment.getExternalStorageDirectory() + File.separator + RUTA_IMAGEN + File.separator + nombreImagen;

        File imagen = new File(path);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //------------------------ AQUI SE MODIFICA PARA ANDROID 7 ------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authorities = getApplicationContext().getPackageName() + ".provider";
            Uri imageUri = FileProvider.getUriForFile(this,authorities, imagen);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imagen));
        }

        startActivityForResult(intent, COD_FOTO);
    }







    private boolean validarPermisos() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true; //retorna true cuando Android es menor a 6.0

        //CAMERA -> importar permissions Manifest.CAMERA (el que NO dice group)
        if ( (checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) ) return true;

        //si se da cuenta de que deberia volver a solicitar los permisos...
        if ( (shouldShowRequestPermissionRationale(CAMERA)) ||
                (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) ) cargarDialogoRecomendacion();
        else requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);


        return false;
    }

    private void cargarDialogoRecomendacion() {

        AlertDialog.Builder dialogo = new AlertDialog.Builder(Workspace.this);
        dialogo.setTitle("Permisos desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la aplicación");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
            }
        });
        dialogo.show();

    }









    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap;

        try {

            switch (requestCode) {
                case COD_SELEC:
                    Uri path2 = data.getData();
                    imagenFiguraActual.setImageURI(path2);

                    break;

                case COD_FOTO:
                    MediaScannerConnection.scanFile(this, new String[]{path}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String s, Uri uri) {

                                }
                            });

                    if (BitmapFactory.decodeFile(path).getWidth() != 0) {
                        bitmap = BitmapFactory.decodeFile(path);
                        //bitmap = Bitmap.createScaledBitmap(bitmap,1000,1000,true);
                        imagenFiguraActual.setImageBitmap(bitmap);
                    }

                    break;
            }


            //PARA IMPORTAR IMAGEN CON TRANSPARENCIA===========================================================================================
            /*
            bitmap = ((BitmapDrawable)imagenFiguraActual.getDrawable()).getBitmap();
            bitmap = Bitmap.createScaledBitmap(bitmap,1000,1000,true);
            imagenFiguraActual.setImageBitmap(bitmap);
            */

            //PARA IMPORTAR IMAGEN SIN TRANSPARENCIA===========================================================================================

            bitmap = ((BitmapDrawable)imagenFiguraActual.getDrawable()).getBitmap();
            bitmap = Bitmap.createScaledBitmap(bitmap,scaleBitmap,scaleBitmap,true);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

            imagenFiguraActual.setImageBitmap(BitmapFactory.decodeByteArray(bytes.toByteArray(), 0, bytes.toByteArray().length));

            ArrayList<Figura> figuras = constraintCanvas.getFiguras();
            for (int i = 0; i < figuras.size(); i++) {
                if (constraintCanvas.getIdActual() == figuras.get(i).getIdFigura()
                        && constraintCanvas.getFocoFigura().getContenedorFoco().getVisibility() == View.VISIBLE) {
                    figuras.get(i).setByteArray(bytes.toByteArray());
                    GeneraTexto.crearArchivoImg(bytes.toByteArray(), figuras.get(i).getIdFigura());
                    guardarTodasLasFiguras();
                    setGuardado(false);
                }
            }

        } catch (Exception exception) {
            //iv2.setImageResource(R.drawable.ic_launcher_background);
            Toast.makeText(getApplication(), "Imagen no seleccionada", Toast.LENGTH_SHORT).show();
        }



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length == 2 && grantResults[0]==PackageManager.PERMISSION_GRANTED
                    && grantResults[1]==PackageManager.PERMISSION_GRANTED) {
                call_editor_image.setEnabled(true);
            } else {
                solicitarPermisosManual();
            }
        }
    }

    private void solicitarPermisosManual() {

        final CharSequence[] opciones = {"Sí", "No"};
        final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(Workspace.this);
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");

        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Sí")) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);

                } else {
                    Toast.makeText(getApplicationContext(),"Los permisos no fueron aceptados",Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();

    }













    ////////////////////===================== HILOS ================================////////////////////////////////////


    class HiloCarga extends AsyncTask<Void, Integer, Void> { //<DatosEntrada, Progreso, DatosSalida> (estos datos pueden estar en array)

        ArrayList<Figura> figurasFILE;

        @Override
        protected void onPreExecute() { //(usa el primer parametro de la clase)
            //esto se ejecuta en el hilo principal antes de realizar la tarea en segundo plano
            barraProgresoTitulo.setProgress(0);
            barraProgresoTitulo.setMax(proyecto.getFiguras().size());

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) { //(usa el tercer parametro de la clase)
            //esto es lo que se ejecuta en segundo plano

            if (proyecto.getFiguras() != null) {

                figurasFILE = proyecto.getFiguras();

                for (int i = 0; i < figurasFILE.size(); i++) {

                    constraintCanvas.nuevaFigura(figurasFILE.get(i).getTipoVista(),""+figurasFILE.get(i).getIdFigura());
                    //if (figurasFILE.get(i).getEstado().equals("editandoFigura"))
                    //imagenFiguraActual = (ImageView) constraintCanvas.getFiguras().get(i).getVista();

                }

                ArrayList<Figura> figuras = constraintCanvas.getFiguras();

                for (int i = 0; i < figuras.size(); i++) {
                    figuras.get(i).setPosX(figurasFILE.get(i).getPosX());
                    figuras.get(i).setPosY(figurasFILE.get(i).getPosY());


                    figuras.get(i).setRot(figurasFILE.get(i).getRot());

                    figuras.get(i).setScaX(figurasFILE.get(i).getScaX());
                    figuras.get(i).setScaY(figurasFILE.get(i).getScaY());

                    if (figuras.get(i).getTipoVista() == Figura.IMAGE) {

                        /**TODO esto solo es provisional, hay que cambiar la forma de guardar y leer archivos para
                         * que no se danen las figuras
                         */
                        if (figurasFILE.get(i).get_width() == 0 || figurasFILE.get(i).get_height() == 0) {
                            if (i == 0) Toast.makeText(getApplication(), "Reparando figuras", Toast.LENGTH_SHORT).show();
                            figuras.get(i).set_dim(200,200);
                        } else {
                            figuras.get(i).set_dim(figurasFILE.get(i).get_width(),figurasFILE.get(i).get_height());
                        }
                    }


                    if (figurasFILE.get(i).getTipoVista() == Figura.IMAGE) {
                        if (figurasFILE.get(i).getByteArray() != null) figuras.get(i).setByteArray(figurasFILE.get(i).getByteArray());

                        figuras.get(i).setImageAlpha( figurasFILE.get(i).getImageAlpha() );

                    } else if (figurasFILE.get(i).getTipoVista() == Figura.TEXT_VIEW) {
                        figuras.get(i).setContenidoTexto( figurasFILE.get(i).getContenidoTexto() );
                        //Nuevos Atributos de texto
                        figuras.get(i).setText_size( figurasFILE.get(i).getText_size() );
                        figuras.get(i).setText_color( figurasFILE.get(i).getText_color() );
                        figuras.get(i).setText_background( figurasFILE.get(i).getText_background() );
                        figuras.get(i).setText_alignment( figurasFILE.get(i).getText_alignment() );
                        figuras.get(i).setText_style( figurasFILE.get(i).getText_style() );
                        figuras.get(i).setText_width( figurasFILE.get(i).getText_width() );
                    }

                    publishProgress(i);

                }

                //zoomableLayoutContainer.bajarCalidad(true);


            }



            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { //(usa el segundo parametro de la clase
            //le informa al hilo principal el estado del progreso de este hilo (usando publishProgress() en este hilo)
            findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.VISIBLE);
            hiloCargaTerminado = false;

            //contenedor_imagenes.addView(imageViews.get(values[0]));
            constraintCanvas.getPanelFiguras().
                    addView(constraintCanvas.getFiguras().get(values[0]).getContenedorFigura());

            barraProgresoTitulo.setProgress((values[0] + 1));

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //se ejecuta esto antes de terminar este hilo
            super.onPostExecute(aVoid);
            if (activarToasts) Toast.makeText(getApplication(), "Figuras: " + constraintCanvas.getFiguras().size(), Toast.LENGTH_SHORT).show();
            findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.GONE);
            barraProgresoTitulo.setProgress(0);

            hiloCargaTerminado = true;

            if (paqueteOnRestore != null) {
                buscarTextoEnfocado();
            }
            paqueteOnRestore = null;
        }

        @Override
        protected void onCancelled() {
            //si se corta la ejecucion de este segundo hilo, se llama a esta funcion
            super.onCancelled();
        }
    }






    ////////////////////===================== OYENTE EDICION ================================////////////////////////////////////
    public static boolean guardado = true;

    public static void setGuardado(boolean guardado) {
        Workspace.guardado = guardado;
        if (guardado) titulo.setText(nombreArchivo);
        else titulo.setText("*" + nombreArchivo);
    }
    public static boolean isGuardado() {
        return guardado;
    }








    ////////////////////===================== HILO PRIMERA CARGA ================================////////////////////////////////////

    class HiloPrimeraCarga extends AsyncTask<Void, Integer, Void> { //<DatosEntrada, Progreso, DatosSalida> (estos datos pueden estar en array)

        @Override
        protected void onPreExecute() { //(usa el primer parametro de la clase)
            //esto se ejecuta en el hilo principal antes de realizar la tarea en segundo plano
            barraProgresoTitulo.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) { //(usa el tercer parametro de la clase)
            //esto es lo que se ejecuta en segundo plano

            nombreArchivo = (String) getIntent().getExtras().get("ARCHIVO");

            //titulo.setText(guardado ? nombreArchivo : "*" + nombreArchivo);
            publishProgress(-90);

            if (guardado) {
                //Toast.makeText(getApplication(), "LEYENDO ORIGINAL", Toast.LENGTH_SHORT).show();
                publishProgress(-91);
                String path = Environment.getExternalStorageDirectory() + File.separator + "Libretag/archivos" + File.separator + nombreArchivo;
                GeneraTexto.unpackZip(path,Environment.getExternalStorageDirectory() + File.separator + "Libretag/data/current");
            } else {
                //Toast.makeText(getApplication(), "LEYENDO COPIA", Toast.LENGTH_SHORT).show();
                publishProgress(-92);
            }

            try {

                proyecto = GeneraTexto.leerPrincipal(Environment.getExternalStorageDirectory()
                        + File.separator + "Libretag/data/current" + File.separator + "main.txt");

                if (proyecto == null) {
                    //Toast.makeText(getApplication(), "ERROR: PROYECTO NULO", Toast.LENGTH_SHORT).show();
                    publishProgress(-93);
                }

                if (proyecto.getW_canvas() == -101101) {
                    //Toast.makeText(getApplication(), "Nuevo proyecto creado", Toast.LENGTH_SHORT).show();
                    publishProgress(-94);
                    //proyecto = new Proyecto();
                    //w_canvas = proyecto.getW_canvas();
                    //h_canvas = proyecto.getH_canvas();


                } else {

                    if (proyecto.getW_canvas() != 0 && proyecto.getH_canvas() != 0) {
                        w_canvas = proyecto.getW_canvas();
                        h_canvas = proyecto.getH_canvas();
                    } else {
                        proyecto.setW_canvas(816);
                        proyecto.setH_canvas(1054);

                        w_canvas = proyecto.getW_canvas();
                        h_canvas = proyecto.getH_canvas();
                    }
                }


            } catch (Exception e) {
                //Toast.makeText(getApplication(), "Error al leer archivo: Objeto de otra clase", Toast.LENGTH_SHORT).show();
                publishProgress(-95);
                e.printStackTrace();
                finish();

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { //(usa el segundo parametro de la clase
            //le informa al hilo principal el estado del progreso de este hilo (usando publishProgress() en este hilo)

            switch (values[0]) {
                case -90:
                    titulo.setText(guardado ? nombreArchivo : "*" + nombreArchivo);
                    break;
                case -91:
                    //Toast.makeText(getApplication(), "LEYENDO ORIGINAL", Toast.LENGTH_SHORT).show();
                    break;
                case -92:
                    //Toast.makeText(getApplication(), "LEYENDO COPIA", Toast.LENGTH_SHORT).show();
                    break;
                case -93:
                    Toast.makeText(getApplication(), "ERROR: PROYECTO VACÍO", Toast.LENGTH_SHORT).show();
                    break;
                case -94:
                    Toast.makeText(getApplication(), "Nuevo proyecto creado", Toast.LENGTH_SHORT).show();
                    break;
                case -95:
                    if (activarToasts) Toast.makeText(getApplication(), "Error al leer archivo: Objeto de otra clase", Toast.LENGTH_SHORT).show();
                    break;
            }

            if (values[0] >= 0) barraProgresoTitulo.setProgress(values[0] + 1);

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //se ejecuta esto antes de terminar este hilo
            super.onPostExecute(aVoid);

            if (proyecto.getW_canvas() == -101101) {
                proyecto = new Proyecto();
                w_canvas = proyecto.getW_canvas();
                h_canvas = proyecto.getH_canvas();

                findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.VISIBLE);
                GeneraTexto.crearPrincipal(proyecto,"");
                new HiloGuardado().execute();
            }

            onCreate2();

            findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.GONE);

            hiloCargaTerminado = true;

            //para el giro de pantalla
            display = getWindowManager().getDefaultDisplay();
            size = new Point();
            display.getSize(size);
            width_disp = size.x;
            height_disp = size.y;
            zoomableLayoutContainer.recenter(width_disp, height_disp);

            hiloPrimeraCarga = null;

            barraProgresoTitulo.setProgress(0);
        }

        @Override
        protected void onCancelled() {
            //si se corta la ejecucion de este segundo hilo, se llama a esta funcion
            super.onCancelled();
        }
    }


    ////////////////////===================== HILO PRIMERA CARGA ================================////////////////////////////////////

    class HiloGuardado extends AsyncTask<Void, Integer, Void> { //<DatosEntrada, Progreso, DatosSalida> (estos datos pueden estar en array)

        @Override
        protected void onPreExecute() { //(usa el primer parametro de la clase)
            //esto se ejecuta en el hilo principal antes de realizar la tarea en segundo plano
            barraProgresoTitulo.setProgress(0);
            hiloCargaTerminado = false;
            findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.VISIBLE);

            if (constraintCanvas != null) constraintCanvas.enfoqueAuto(true);

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) { //(usa el tercer parametro de la clase)
            //esto es lo que se ejecuta en segundo plano
            GeneraTexto.actualizarArchivoOriginal(nombreArchivo);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) { //(usa el segundo parametro de la clase
            //le informa al hilo principal el estado del progreso de este hilo (usando publishProgress() en este hilo)
            super.onProgressUpdate(values);
            barraProgresoTitulo.setProgress(values[0] + 1);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //se ejecuta esto antes de terminar este hilo
            super.onPostExecute(aVoid);
            findViewById(R.id.frame_bloqueo_hilo).setVisibility(View.GONE);
            barraProgresoTitulo.setProgress(0);
            hiloCargaTerminado = true;
            setGuardado(true);
            Toast.makeText(getApplication(), "Guardado", Toast.LENGTH_SHORT).show();
            hiloGuardado = null;

            if (hgSSG != null) {
                hgSSG = null;
                finish();
            }
        }

        @Override
        protected void onCancelled() {
            //si se corta la ejecucion de este segundo hilo, se llama a esta funcion
            super.onCancelled();
        }
    }


}






















