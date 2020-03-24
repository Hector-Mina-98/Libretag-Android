package com.application.heccoder.libretag;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ConstraintCanvas extends ConstraintLayout {

    Cuadricula cuadricula;
    ConstraintLayout panelCuadricula, panelCuadricula2;

    /////////////////////////////////////////////////////////////////////////////////////////
    View imgaux;
    ArrayList<Figura> figuras = new ArrayList<>();

    //////////////////////////////////////////////////////////////////////////////////////////
    LayoutInflater inflater;

    FrameLayout pplLayout;
    FocoFigura focoFigura;
    boolean enfocado;

    int tipoFiguraActual;
    int idActual;

    int idFiguras;
    //////////////////////////////////////////////////////////////////////////////////////////

    private int w_canvas, h_canvas;


    private ConstraintLayout panelFiguras;
    private PanelDibujo panelDibujo;

    public ConstraintLayout getPanelFiguras() {
        return panelFiguras;
    }
    public PanelDibujo getPanelDibujo() {
        return panelDibujo;
    }


    public ConstraintCanvas(Context context, FrameLayout pplLayout, int w_canvas, int h_canvas, int tipoCuadricula) {
        super(context);

        setLayoutParams(new LayoutParams(0,0));

        this.w_canvas = w_canvas;
        this.h_canvas = h_canvas;

        getLayoutParams().width = w_canvas;
        getLayoutParams().height = h_canvas;

        setBackgroundColor(Color.WHITE);
        setElevation(6.0f);

        // -------------------------- CUADRICULAS --------------------------------------------
        panelCuadricula = new ConstraintLayout(context);
        this.addView(panelCuadricula);
        panelCuadricula.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        cuadricula = new Cuadricula(context, w_canvas, h_canvas,Cuadricula.CUADRICULADO);
        panelCuadricula.addView(cuadricula);

        panelCuadricula2 = new ConstraintLayout(context);
        this.addView(panelCuadricula2);
        panelCuadricula2.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        cuadricula = new Cuadricula(context, w_canvas, h_canvas,Cuadricula.RAYADO);
        panelCuadricula2.addView(cuadricula);

        if (tipoCuadricula == Cuadricula.CUADRICULADO) panelCuadricula2.setVisibility(INVISIBLE);
        else if (tipoCuadricula == Cuadricula.RAYADO) panelCuadricula.setVisibility(INVISIBLE);
        else {
            panelCuadricula.setVisibility(INVISIBLE);
            panelCuadricula2.setVisibility(INVISIBLE);
        }

        // -------------------------- PANEL FIGURAS ------------------------------------------
        panelFiguras = new ConstraintLayout(context);
        this.addView(panelFiguras);
        panelFiguras.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

        panelDibujo = new PanelDibujo(context, w_canvas, h_canvas);
        this.addView(panelDibujo);
        panelDibujo.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        //panelDibujo.setBackgroundResource(R.drawable.fondo_botones_main);
        /////////////////////////////////////////////////////////////////////////////////////////
        inflater = LayoutInflater.from(context);

        /////////////////////////////////////////////////////////////////////////////////////////
        this.pplLayout = pplLayout;
        LinearLayout contenedorFoco = (LinearLayout) inflater.inflate(R.layout.contenedor_foco, pplLayout, false);
        //ContenedorFoco contenedorFoco = new ContenedorFoco(context);
        focoFigura = new FocoFigura(pplLayout, this, contenedorFoco);

        enfocado = false;

        tipoFiguraActual = -1;

        idFiguras = -1000000;

        idActual = -2000000;

        /////////////////////////////////////////////////////////////////////////////////////////


        countDownTimer = new CountDownTimer(10000,1) { //creo que ya no es necesario

            @Override
            public void onTick(long millisUntilFinished) {
                Log.d("COUNTDOWN: ", "" + millisUntilFinished);
                enfoqueAuto(false);
            }

            @Override
            public void onFinish() {
                Log.d("COUNTDOWN: ", "DONE");
            }
        };

        /////////////////////////////////////////////////////////////////////////////////////////
        tocandoCanvas = false;

    }



    public void nuevaFigura (int tipoFigura, String IDF) {

        View contenedor = inflater.inflate(R.layout.contenedor_figura, this, false);
        //panelFiguras.addView(contenedor);

        if (IDF.equals("NUEVO")) {
            idFiguras++;
            panelFiguras.addView(contenedor);

        } else {
            idFiguras = Integer.parseInt(IDF);
        }

        Figura figura = new Figura((LinearLayout) contenedor,tipoFigura,this,idFiguras);
        figura.crearVista();
        figuras.add(figura);



        //CALCULANDO POSICION CENTRADA EN LA PANTALLA
        this.getLocationOnScreen(locationCanvas);
        pplLayout.getLocationOnScreen(locationPplLatout);

        float ubiX = (-locationCanvas[0] + locationPplLatout[0] + 20.0f) / this.getScaleX();
        float ubiY = (-locationCanvas[1] + locationPplLatout[1] + 140.0f) / this.getScaleY();

        ubiX = Math.max(ubiX, 20.0f);
        ubiY = Math.max(ubiY, 20.0f);

        contenedor.setX(ubiX);
        contenedor.setY(ubiY);

        //figura.getVista().setLayoutParams(new LinearLayout.LayoutParams(1900, 200));
        //contenedor.setLayoutParams(new Constraints.LayoutParams(900, 200));
    }

    private int[] locationCanvas = new int[2];
    private int[] locationPplLatout = new int[2];



    public void enfoqueAuto (boolean clickOnCanvas) {

        ///////////////////////////////////////////////////////////////////////////////////////

        if (clickOnCanvas) {
            if(countDownTimer != null)    countDownTimer.cancel();
            focoFigura.desenfocar();

            //idActual = -2000000;

            View view = ((Activity)getContext()).getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)(getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            for (int i = 0; i < figuras.size(); i++) {

                if (figuras.get(i).getTipoVista() == Figura.EDIT_TEXT)   figuras.get(i).cambiarTipoTexto(Figura.TEXT_VIEW);

            }

            enfocado = false;

        } else if (enfocado) {
            focoFigura.enfocar(imgaux);
            enfocado = true;
        }

    }


    private int[] locationFigura = new int[2];

    boolean ubicarFigura (MotionEvent motionEvent) {

        boolean encontro = false;

        if (enfocado) Log.d("ubicarFigura: ", "enfocado");
        else Log.d("ubicarFigura: ", "NO enfocado");

        float xi, yi;

        float dimX, dimY;

        float x, y;

        float angulo;

        for (int i = figuras.size()-1; i >= 0 ; i--) {

            figuras.get(i).getContenedorFigura().getLocationOnScreen(locationFigura);
            angulo = (float) - Math.toRadians(figuras.get(i).getContenedorFigura().getRotation() % 360);
            dimX = ( (figuras.get(i).getContenedorFigura().getWidth() * figuras.get(i).getContenedorFigura().getScaleX()) * this.getScaleX());
            dimY = ( (figuras.get(i).getContenedorFigura().getHeight() * figuras.get(i).getContenedorFigura().getScaleY()) * this.getScaleY());

            xi = (motionEvent.getRawX() - locationFigura[0]);
            yi = (motionEvent.getRawY() - locationFigura[1]);

            x = (float)( (xi * Math.cos(angulo)) - (yi * Math.sin(angulo)) );
            y = (float)( (xi * Math.sin(angulo)) + (yi * Math.cos(angulo)) );

            /*
            Log.d("ROTA2","ANGULO F" + i + " = " + Math.toDegrees(angulo) + " --- X = " + x_r + " --- Y = " + y_r);
            Workspace.iv.setVisibility(View.VISIBLE);
            Workspace.iv.setX(locationFigura[0] + x_r);
            Workspace.iv.setY(locationFigura[1] + y_r - 50);
            */

            if (x > 0 && x < dimX)
                if (y > 0 && y < dimY) {

                    enfoque(figuras.get(i).getContenedorFigura(),figuras.get(i));
                    encontro = true;

                    //i = figuras.size();
                    i = -1;
                }

        }

        if (!encontro) enfoqueAuto(true);


        return  encontro;

    }


    CountDownTimer countDownTimer;
    public CountDownTimer getCountDownTimer() {
        return countDownTimer;
    }

    public void enfoque (View contenedor, Figura figura) {
        if (figura.getTipoVista() == Figura.TEXT_VIEW) figura.cambiarTipoTexto(Figura.EDIT_TEXT);

        imgaux = contenedor;
        enfocado = true;
        tipoFiguraActual = figura.getTipoVista();
        idActual = figura.getIdFigura();

        if (countDownTimer != null) {
            countDownTimer.cancel();
            //countDownTimer.start();
        }

        enfoqueAuto(false);

    }

    boolean tocandoCanvas;

    public void setTocandoCanvas(boolean tocandoCanvas) {
        this.tocandoCanvas = tocandoCanvas;
    }
    public boolean isTocandoCanvas() {
        return tocandoCanvas;
    }

    public ArrayList<Figura> getFiguras() {
        return figuras;
    }
    public int getIdActual() {
        return idActual;
    }

    public FocoFigura getFocoFigura() {
        return focoFigura;
    }

    public void setIdActual(int idActual) {
        this.idActual = idActual;
    }

    public ConstraintLayout getPanelCuadricula() {
        return panelCuadricula;
    }
    public ConstraintLayout getPanelCuadricula2() {
        return panelCuadricula2;
    }

    public View getImgaux() {
        return imgaux;
    }
}
