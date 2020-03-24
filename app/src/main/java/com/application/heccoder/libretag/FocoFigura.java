package com.application.heccoder.libretag;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class FocoFigura {

    private FrameLayout pplLayout;
    private ConstraintCanvas constraintCanvas;
    private View contenedorFoco;
    private LinearLayout contenedorRectangulo;

    //private Rectangulo rectangulo;
    private ImageView rectangulo;
    private ImageView scaLeTop, scaRiTop, scaLeBot, scaRiBot;
    private ImageView rotLef, rotRig, rotBot;
    private ImageView mover;

    private ArrayList<View> botones = new ArrayList<>();

    private int[] locationFigura = new int[2];
    private int[] locationPplLayout = new int[2];

    private View contenedorFigura;
    //---------------------------------------MOTION EVENT-------------------------------------------
    private int xref, yref;
    private double x, y, x2, y2;
    private float moveimgx, moveimgy;
    private int a_canvas, b_canvas;
    private float a_canvas2, b_canvas2;
    private int[] locationCanvas = new int[2];
    private int[] locationBoton = new int[2];
    private int[] locationRectangulo = new int[2];

    private int buttomPPL_size, buttomSEC_size;

    private float scaleX_ini, scaleY_ini;

    double xref_r, yref_r;
    double x_r, y_r, angulo;
    double x3, y3;

    double xref_r_ESC, yref_r_ESC;
    double x_r_ESC, y_r_ESC;

    private View grid;


    FocoFigura (FrameLayout pplLayout, ConstraintCanvas constraintCanvas, LinearLayout contenedorFoco) {
        this.pplLayout = pplLayout;
        this.constraintCanvas = constraintCanvas;
        this.contenedorFoco = contenedorFoco;
        this.contenedorRectangulo = contenedorFoco.findViewById(R.id.contenedor_rectangulo);

        this.grid = contenedorFoco.findViewById(R.id.grid);

        pplLayout.addView(contenedorFoco);
        contenedorFoco.setVisibility(View.GONE);
        //----------------------------MARGENES---------------------------------------
        //rectangulo = new Rectangulo(pplLayout.getContext());
        rectangulo = new ImageView(pplLayout.getContext());
        rectangulo.setImageResource(R.drawable.ic_launcher_background);
        contenedorRectangulo.addView(rectangulo,0);
        /*
        contenedorFoco.setX(200);
        contenedorFoco.setY(200);
        rectangulo.setAncho(300);
        rectangulo.setAlto(100);
        */
        rectangulo.setVisibility(View.INVISIBLE);


        //----------------------------BOTONES---------------------------------------
        mover = contenedorFoco.findViewById(R.id.foco_move);
        botones.add(mover);
        scaLeTop = contenedorFoco.findViewById(R.id.foco_scaLeTop);
        scaRiTop = contenedorFoco.findViewById(R.id.foco_scaRiTop);
        scaLeBot = contenedorFoco.findViewById(R.id.foco_scaLeBot);
        scaRiBot = contenedorFoco.findViewById(R.id.foco_scaRiBot);
        botones.add(scaLeTop);
        botones.add(scaRiTop);
        botones.add(scaLeBot);
        botones.add(scaRiBot);
        rotLef = contenedorFoco.findViewById(R.id.foco_rotLef);
        rotRig = contenedorFoco.findViewById(R.id.foco_rotRig);
        rotBot = contenedorFoco.findViewById(R.id.foco_rotBot);
        botones.add(rotLef);
        botones.add(rotRig);
        botones.add(rotBot);

        buttomPPL_size = mover.getLayoutParams().width;        //60
        buttomSEC_size = scaRiBot.getLayoutParams().height;     //40


        oyenteBotones();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void oyenteBotones () {

        this.contenedorRectangulo.setOnTouchListener(new OyenteGeneral());

        for(int b = 0; b < botones.size(); b++) {
            botones.get(b).setOnTouchListener(new OyenteGeneral());
        }

    }


    private void mover () {

        a_canvas2 = constraintCanvas.getScaleX();
        b_canvas2 = constraintCanvas.getScaleY();

        //ubicar la figura en el origen (para ponerlo en un punto FIJO de partida)
        x2 = x - xref;
        y2 = y - yref;

        //ubicar la figura en el centro del foco
        x2 += locationFigura[0] + contenedorFoco.getWidth()/2;
        y2 += locationFigura[1] + contenedorFoco.getHeight()/2;

        //ubicar el centro de la figura en el centro del foco
        x2 -= contenedorFigura.getWidth()*constraintCanvas.getScaleX()*contenedorFigura.getScaleX()/2;
        y2 -= contenedorFigura.getHeight()*constraintCanvas.getScaleY()*contenedorFigura.getScaleY()/2;

        //normalizar --> independiente del escalamiento
        x2 = (x2 + 1.0f*a_canvas2);
        y2 = (y2 + 1.0f*b_canvas2);

        moveimgx = (float) (((x2) -a_canvas-(1.0f*a_canvas2))/a_canvas2);
        moveimgy = (float) (((y2) -b_canvas-(1.0f*b_canvas2))/b_canvas2);

        contenedorFigura.setX(moveimgx  + (contenedorFigura.getScaleX()-1) * contenedorFigura.getWidth()/2);
        contenedorFigura.setY(moveimgy  + (contenedorFigura.getScaleY()-1) * contenedorFigura.getHeight()/2);

    }

    @SuppressLint("ClickableViewAccessibility")
    void enfocar (View contenedorFigura) {
        Workspace.contenedor_editor_imagen.setVisibility(View.GONE);
        Workspace.contenedor_editor_texto.setVisibility(View.GONE);

        //primero activa o desactiva botones dependiendo del tipo de figura
        for(int b = 0; b < botones.size(); b++) {
            botones.get(b).setVisibility(View.INVISIBLE);
        }
        mover.setVisibility(View.VISIBLE);
        ArrayList<Figura> figuras = constraintCanvas.getFiguras();
        int idActual = constraintCanvas.getIdActual();
        for (int i = 0; i < figuras.size(); i++) {
            if (figuras.get(i).getIdFigura() == idActual) { //Busca la figura enfocada
                figuraActual = figuras.get(i);
                if (figuras.get(i).getTipoVista() == Figura.IMAGE){ //Si es texto activa los botones laterales
                    Workspace.imagenFiguraActual = (ImageView) figuras.get(i).getVista();
                    Workspace.contenedor_editor_imagen.setVisibility(View.VISIBLE);
                    Workspace.EdImg_linear_contenido.setVisibility(View.GONE);
                    ((ImageView)Workspace.EdImg_more_less).setImageResource(R.drawable.editor_dibujo_mas_opciones);
                    scaLeTop.setVisibility(View.VISIBLE);
                    scaRiTop.setVisibility(View.VISIBLE);
                    scaLeBot.setVisibility(View.VISIBLE);
                    scaRiBot.setVisibility(View.VISIBLE);
                } else {
                    Workspace.contenedor_editor_texto.setVisibility(View.VISIBLE);
                    rotLef.setVisibility(View.VISIBLE);
                    rotRig.setVisibility(View.VISIBLE);
                }
            }
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////////7
        double angulo2 = Math.toRadians(contenedorFigura.getRotation() % 360);

        grid.setRotation( (float)Math.toDegrees(angulo2));

        contenedorFoco.setVisibility(View.VISIBLE);
        this.contenedorRectangulo.setOnTouchListener(new OyenteGeneral());

        this.contenedorFigura = contenedorFigura;

        pplLayout.getLocationInWindow(locationPplLayout);
        contenedorFigura.getLocationInWindow(locationFigura);

        int ancho, alto;

        ancho = (int)((contenedorFigura.getWidth() * constraintCanvas.getScaleX()) * contenedorFigura.getScaleX());
        alto = (int)((contenedorFigura.getHeight() * constraintCanvas.getScaleY()) * contenedorFigura.getScaleY());

        rectangulo.setLayoutParams(new LinearLayout.LayoutParams(ancho, alto));

        int tam_max = Math.max((int)(contenedorFigura.getWidth() * contenedorFigura.getScaleX() * constraintCanvas.getScaleX()) + buttomPPL_size*5,
                (int)(contenedorFigura.getHeight() * contenedorFigura.getScaleY() * constraintCanvas.getScaleY()) + buttomPPL_size*5);

        tam_max = tam_max * 2;

        contenedorFoco.setLayoutParams(new FrameLayout.LayoutParams(tam_max, tam_max));

        //contenedorFoco.setX(locationFigura[0] - locationPplLayout[0] - buttomPPL_size + buttomSEC_size/2);
        //contenedorFoco.setY(locationFigura[1] - locationPplLayout[1] - buttomPPL_size);


        //ubicar la figura en el centro del foco
        float focox = (locationFigura[0] - locationPplLayout[0] - tam_max/2);
        float focoy = (locationFigura[1] - locationPplLayout[1] - tam_max/2);

        //ubicar el centro de la figura en el centro del foco
        focox += contenedorFigura.getWidth()*contenedorFigura.getScaleX()*constraintCanvas.getScaleX()/2;
        focoy += contenedorFigura.getHeight()*contenedorFigura.getScaleY()*constraintCanvas.getScaleY()/2;

        ///////////////////////////////////////ROTACION DE COORDENADAS///////////////////////////////////////////////////////////////

        float dimX = (contenedorFigura.getWidth() * contenedorFigura.getScaleX() * constraintCanvas.getScaleX());
        float dimY = (contenedorFigura.getHeight() * contenedorFigura.getScaleY() * constraintCanvas.getScaleY());

        //x = (ancho_escalado * Math.cos(angulo)) - (alto_escalado * Math.sin(angulo));
        //y = (ancho_escalado * Math.sin(angulo)) + (alto_escalado * Math.cos(angulo));

        //se localizan cuatro esquinas del contenedor de la figura
        float xA = focox;
        float xB = focox + (float)( dimX * Math.cos(angulo2) );
        float xC = focox + ((float)( dimX * Math.cos(angulo2) ) + (float)( - dimY * Math.sin(angulo2) ));
        float xD = focox + (float)( - (dimY) * Math.sin(angulo2) );

        float yA = focoy;
        float yB = focoy + (float)( dimX * Math.sin(angulo2) );
        float yC = focoy + ((float)( dimX * Math.sin(angulo2) ) + (float)( dimY * Math.cos(angulo2) ));
        float yD = focoy + (float)( (dimY) * Math.cos(angulo2) );

        focox = (Math.min(Math.min(xA,xB), Math.min(xC,xD)));
        focoy = (Math.min(Math.min(yA,yB), Math.min(yC,yD)));

        Log.d("scarot","xC-xA: " + Math.abs(xC-xA) + " -------- xB-xD: " + Math.abs(xD-xB) + " ---------- DIMX:" + dimX );

        //if (Math.abs(xB-xD) > dimX) focox += (Math.abs(xB-xD) - dimX) /2;
        //else if (Math.abs(xC-xA) > dimX) focox += (Math.abs(xC-xA) - dimX) /2;

        if (Math.max(Math.abs(xC-xA),Math.abs(xB-xD)) != dimX) {
            if (Math.max(Math.abs(xC-xA),Math.abs(xB-xD)) == Math.abs(xC-xA)) focox += (Math.abs(xC-xA) - dimX) /2;
            else if (Math.max(Math.abs(xC-xA),Math.abs(xB-xD)) == Math.abs(xB-xD)) focox += (Math.abs(xB-xD) - dimX) /2;
        }

        //if (Math.abs(yC-yA) > dimY) focoy += (Math.abs(yC-yA) - dimY) /2;
        //else if (Math.abs(yB-yD) > dimY) focoy += (Math.abs(yB-yD) - dimY) /2;

        if (Math.max(Math.abs(yC-yA),Math.abs(yB-yD)) != dimY) {
            if (Math.max(Math.abs(yC-yA),Math.abs(yB-yD)) == Math.abs(yC-yA)) focoy += (Math.abs(yC-yA) - dimY) /2;
            else if (Math.max(Math.abs(yC-yA),Math.abs(yB-yD)) == Math.abs(yB-yD)) focoy += (Math.abs(yB-yD) - dimY) /2;
        }


        contenedorFoco.setX(focox);
        contenedorFoco.setY(focoy);

        //contenedorFoco.setX(focox);
        //contenedorFoco.setY(focoy);


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



    }

    @SuppressLint("ClickableViewAccessibility")
    void desenfocar () {
        contenedorFoco.setVisibility(View.GONE);
        this.contenedorRectangulo.setOnTouchListener(null);

        figuraActual = null;

        Workspace.contenedor_editor_texto.setVisibility(View.GONE);
        Workspace.contenedor_editor_imagen.setVisibility(View.GONE);
        Workspace.imagenFiguraActual = null;
    }






    private void escalar (View view, int accion) {

        float min = 0.05f;

        double angulo = Math.toRadians(contenedorFigura.getRotation() % 360);

        switch (accion) {
            case MotionEvent.ACTION_DOWN:
                xref_r_ESC = (xref * Math.cos(angulo)) + (yref * Math.sin(angulo));
                yref_r_ESC = -(xref * Math.sin(angulo)) + (yref * Math.cos(angulo));
                break;


            case MotionEvent.ACTION_MOVE:
                x_r_ESC = ( (x * Math.cos(angulo)) + (y * Math.sin(angulo)) );
                y_r_ESC = (-(x * Math.sin(angulo)) + (y * Math.cos(angulo)) );

                if (view == scaLeBot) {
                    contenedorFigura.setScaleX((float) Math.max(min, scaleX_ini + ((xref_r_ESC - x_r_ESC)/constraintCanvas.getScaleX())/(contenedorFigura.getWidth()/2.0f) ));
                    contenedorFigura.setScaleY((float) Math.max(min, scaleY_ini + ((y_r_ESC - yref_r_ESC)/constraintCanvas.getScaleY())/(contenedorFigura.getHeight()/2.0f) ));
                } else if (view == scaLeTop) {
                    contenedorFigura.setScaleX((float) Math.max(min, scaleX_ini + ((xref_r_ESC - x_r_ESC)/constraintCanvas.getScaleX())/(contenedorFigura.getWidth()/2.0f) ));
                    contenedorFigura.setScaleY((float) Math.max(min, scaleY_ini + ((yref_r_ESC - y_r_ESC)/constraintCanvas.getScaleY())/(contenedorFigura.getHeight()/2.0f) ));
                } else if (view == scaRiBot) {
                    contenedorFigura.setScaleX((float) Math.max(min, scaleX_ini + ((x_r_ESC - xref_r_ESC)/constraintCanvas.getScaleX())/(contenedorFigura.getWidth()/2.0f) ));
                    contenedorFigura.setScaleY((float) Math.max(min, scaleY_ini + ((y_r_ESC - yref_r_ESC)/constraintCanvas.getScaleY())/(contenedorFigura.getHeight()/2.0f) ));
                } else if (view == scaRiTop) {
                    contenedorFigura.setScaleX((float) Math.max(min, scaleX_ini + ((x_r_ESC - xref_r_ESC)/constraintCanvas.getScaleX())/(contenedorFigura.getWidth()/2.0f) ));
                    contenedorFigura.setScaleY((float) Math.max(min, scaleY_ini + ((yref_r_ESC - y_r_ESC)/constraintCanvas.getScaleY())/(contenedorFigura.getHeight()/2.0f) ));
                }

                enfocar(contenedorFigura);
                break;

        }
    }


    private void rotar(int accion) {

        //contenedorFigura.setRotation(45);
        angulo = - Math.toRadians(contenedorFigura.getRotation() % 360);

        switch (accion) {
            case MotionEvent.ACTION_DOWN:
                //pplLayout.getLocationOnScreen(locationPplLayout);

                xref_r = (xref * Math.cos(angulo)) + (yref * Math.sin(angulo));
                yref_r = -(xref * Math.sin(angulo)) + (yref * Math.cos(angulo));
                break;
            case MotionEvent.ACTION_MOVE:

                x3 = x - (locationFigura[0]+locationPplLayout[0] + (contenedorFoco.getWidth()/2) );
                y3 = - (y - (locationFigura[1]+locationPplLayout[1]) - (contenedorFoco.getHeight()/2) );

                x_r = (x3 * Math.cos(angulo)) + (y3 * Math.sin(angulo));
                y_r = -(x3 * Math.sin(angulo)) + (y3 * Math.cos(angulo));

                contenedorFigura.setRotation( (float)(contenedorFigura.getRotation() + x_r*0.1) );
                grid.setRotation( (float)(grid.getRotation() + x_r*0.1) );

                Log.d("ROTACION..........", "Angulo: " + Math.toDegrees(angulo) + "   X: " + x_r + "   Y: " + y_r);

                break;
        }

    }


    private int ancho_INI, alto_INI;
    private float xt, xt_ref;
    private float yt, yt_ref;

    private  Figura figuraActual;

    private void modificarAnchoTexto (View view, int accion) {

        float min = 20.0f;

        Log.d("ANCHO_TXT","------ Ancho = " + contenedorFigura.getWidth());

        double angulo = Math.toRadians(contenedorFigura.getRotation() % 360);

        switch (accion) {
            case MotionEvent.ACTION_DOWN:
                xref_r_ESC = (xref * Math.cos(angulo)) + (yref * Math.sin(angulo));
                yref_r_ESC = -(xref * Math.sin(angulo)) + (yref * Math.cos(angulo));
                ancho_INI = contenedorFigura.getWidth();
                alto_INI = contenedorFigura.getHeight();

                xt_ref = contenedorFigura.getX();
                yt_ref = contenedorFigura.getY();

                break;


            case MotionEvent.ACTION_MOVE:
                x_r_ESC = ( (x * Math.cos(angulo)) + (y * Math.sin(angulo)) );
                y_r_ESC = (-(x * Math.sin(angulo)) + (y * Math.cos(angulo)) );

                if (figuraActual != null) {
                    if (figuraActual.getTipoVista() == Figura.TEXT_VIEW) {
                        if (view == rotLef) {
                            ((TextView)figuraActual.getVista()).setWidth((int) Math.max
                                    (min, (ancho_INI + (xref_r_ESC - x_r_ESC)/constraintCanvas.getScaleX()) ));
                        } else if (view == rotRig) {
                            ((TextView)figuraActual.getVista()).setWidth((int) Math.max
                                    (min, (ancho_INI - (xref_r_ESC - x_r_ESC)/constraintCanvas.getScaleX())  ));
                        }
                    }


                    //================== MOVIMIENTO AUTOMATICO =========================

                    constraintCanvas.getLocationInWindow(locationCanvas);
                    a_canvas = locationCanvas[0];
                    b_canvas = locationCanvas[1];

                    a_canvas2 = constraintCanvas.getScaleX();
                    b_canvas2 = constraintCanvas.getScaleY();

                    xt = xt_ref + ancho_INI/2 - contenedorFigura.getWidth()/2;
                    yt = yt_ref + alto_INI/2 - contenedorFigura.getHeight()/2;

                    contenedorFigura.setX(xt);
                    contenedorFigura.setY(yt);

                    x2 = (a_canvas + xt*a_canvas2 );
                    y2 = (b_canvas + yt*b_canvas2 );



                    //================== ENFOQUE AUTOMATICO =========================
                    pplLayout.getLocationInWindow(locationPplLayout);

                    int ancho, alto;

                    ancho = (int)((contenedorFigura.getWidth() * constraintCanvas.getScaleX()) * contenedorFigura.getScaleX());
                    alto = (int)((contenedorFigura.getHeight() * constraintCanvas.getScaleY()) * contenedorFigura.getScaleY());

                    rectangulo.setLayoutParams(new LinearLayout.LayoutParams(ancho, alto));

                    int tam_max = Math.max((int)(contenedorFigura.getWidth() * contenedorFigura.getScaleX() * constraintCanvas.getScaleX()) + buttomPPL_size*5,
                            (int)(contenedorFigura.getHeight() * contenedorFigura.getScaleY() * constraintCanvas.getScaleY()) + buttomPPL_size*5);

                    tam_max = tam_max * 2;

                    contenedorFoco.setLayoutParams(new FrameLayout.LayoutParams(tam_max, tam_max));


                    //ubicar la figura en el centro del foco
                    float focox = (float) (x2 - locationPplLayout[0] - tam_max/2);
                    float focoy = (float) (y2 - locationPplLayout[1] - tam_max/2);

                    //ubicar el centro de la figura en el centro del foco
                    focox += contenedorFigura.getWidth()*contenedorFigura.getScaleX()*constraintCanvas.getScaleX()/2;
                    focoy += contenedorFigura.getHeight()*contenedorFigura.getScaleY()*constraintCanvas.getScaleY()/2;

                    contenedorFoco.setX(focox);
                    contenedorFoco.setY(focoy);




                }

                break;

        }

    }





    @SuppressLint("ClickableViewAccessibility")
    private class OyenteGeneral implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if(constraintCanvas.getCountDownTimer() != null)    constraintCanvas.getCountDownTimer().cancel();

            View view2 = ((Activity)constraintCanvas.getContext()).getCurrentFocus();
            if (view2 != null) {
                InputMethodManager imm = (InputMethodManager)(constraintCanvas.getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view2.getWindowToken(), 0);
            }

            //Log.d("multi000", "toques: " + motionEvent.getPointerCount());

            int action = motionEvent.getActionMasked();

            if (constraintCanvas.isTocandoCanvas() || motionEvent.getPointerCount() > 1) {
                constraintCanvas.enfoqueAuto(true);
                toque_multiple = true;
            }

            switch (action) {

                case MotionEvent.ACTION_DOWN:
                    if(motionEvent.getPointerCount() == 1) toque_multiple = false;

                    xref = (int) motionEvent.getRawX();
                    yref = (int) motionEvent.getRawY();

                    constraintCanvas.getLocationInWindow(locationCanvas);
                    a_canvas = locationCanvas[0];
                    b_canvas = locationCanvas[1];

                    view.getLocationInWindow(locationBoton);
                    contenedorFoco.getLocationInWindow(locationFigura);
                    pplLayout.getLocationOnScreen(locationPplLayout);

                    scaleX_ini = contenedorFigura.getScaleX();
                    scaleY_ini = contenedorFigura.getScaleY();

                    contenedorRectangulo.getLocationInWindow(locationRectangulo);

                    rotar(MotionEvent.ACTION_DOWN);
                    escalar(null, MotionEvent.ACTION_DOWN);
                    modificarAnchoTexto(null, MotionEvent.ACTION_DOWN);

                    break;
                case MotionEvent.ACTION_MOVE:
                    x = motionEvent.getRawX();
                    y = motionEvent.getRawY();

                    if (!constraintCanvas.isTocandoCanvas()) {
                        if (view == mover) {
                            //TODO aquí iría la acción del único botón de rotación
                            rotar(MotionEvent.ACTION_MOVE);

                        } else if (view == scaLeBot || view == scaLeTop || view == scaRiBot || view == scaRiTop) {
                            escalar(view, MotionEvent.ACTION_MOVE);

                        } else if (view == rotLef || view == rotRig) {
                            modificarAnchoTexto(view, MotionEvent.ACTION_MOVE);

                        } else if (view == contenedorRectangulo) {
                            mover();

                            x = x - (xref - locationRectangulo[0] - buttomPPL_size + buttomSEC_size/2);
                            y = y - (yref - locationRectangulo[1] - buttomPPL_size + buttomSEC_size/2);

                            pplLayout.getLocationInWindow(locationPplLayout);
                            contenedorFoco.setX((float) x - (locationBoton[0]-locationFigura[0]) - locationPplLayout[0] - buttomPPL_size + buttomSEC_size/2);
                            contenedorFoco.setY((float) y - (locationBoton[1]-locationFigura[1]) - locationPplLayout[1] - buttomPPL_size + buttomSEC_size/2);
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    if (toque_multiple) constraintCanvas.enfoqueAuto(true);

                    x = (int) motionEvent.getRawX();
                    y = (int) motionEvent.getRawY();
                    if((Math.abs(x-xref)<10) && (Math.abs(y-yref)<10) ){
                        constraintCanvas.enfoqueAuto(true);
                    }

                    Workspace.setGuardado(false);
                    break;

            }

            return true;
        }
    }

    private boolean toque_multiple;


    public View getContenedorFoco() {
        return contenedorFoco;
    }
}
