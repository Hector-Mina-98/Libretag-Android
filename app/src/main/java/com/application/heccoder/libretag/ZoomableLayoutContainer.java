package com.application.heccoder.libretag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
public class ZoomableLayoutContainer extends ConstraintLayout {

    Context context;

    //////////////////////////////////////////// CHILD ////////////////////////////////////////////////////////////
    private ConstraintCanvas constraint_canvas;
    int canvas_width, canvas_height;
    int[] location_canvas = new int[2];
    float x_canvas, y_canvas;

    float global_difx, global_dify;

    //////////////////////////////////////////// ENTORNO /////////////////////////////////////////////////
    int width_disp, height_disp;
    Point size;

    boolean pre_touch;
    ConstraintLayout linear_activity;
    int[] location_activity = new int[2];
    private float margenVertical, margenHorizontal;

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    float xref,yref,   x,y,    a_canvas,b_canvas;
    float movecanvasx, movecanvasy;
    float moveimgx_ref, moveimgy_ref;

    //////////////////////////////////////////variables de gestos zoom/////////////////////////////////////////////
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    float mScaleFactorTemp;
    boolean scaleOngoing;
    boolean allowmove;
    private float minScale, maxScale;
    float a_canvas2, b_canvas2;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////// CONSTRUCTOR ////////////////////////////////////////////////////////
    public ZoomableLayoutContainer(Context context, View child, View parent, int width_disp, int height_disp) {
        super(context);
        this.context = context;

        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        //setBackgroundColor(Color.BLACK);

        //////////////////////////////////////////// CHILD ////////////////////////////////////////////////////////////
        this.constraint_canvas = (ConstraintCanvas) child;
        //this.constraint_canvas.setZoomableLayoutContainer(this);
        this.addView(constraint_canvas);
        canvas_width = constraint_canvas.getLayoutParams().width;
        canvas_height = constraint_canvas.getLayoutParams().height;

        //////////////////////////////////////////// ENTORNO /////////////////////////////////////////////////
        linear_activity = (ConstraintLayout) parent;

        this.width_disp = width_disp;
        this.height_disp = height_disp;

        //////////////////////////////////////////variables de gestos zoom/////////////////////////////////////////////
        allowmove = true;

        //margenVertical = 144.0f;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;
        margenVertical = 0.5f * densityDpi;
        margenHorizontal = 20.0f;

        oyenteZoom();

        recenter();

        oyente();

        global_difx = global_dify = 0;
        pre_touch = true;


        focoEscondido = false;
    }


    public void oyenteZoom () {
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            public void onScaleEnd(ScaleGestureDetector detector) {
                scaleOngoing = false;
            }
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                scaleOngoing = true;
                return true;
            }
            public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

                zoomCanvas(scaleGestureDetector,scaleOngoing);

                return true;
                //return false;
            }
        });
    }

    public void zoomCanvas (ScaleGestureDetector scaleGestureDetector, boolean scaleOngoing) {
        this.scaleOngoing = scaleOngoing;
        // do scaling here
        mScaleFactor *= scaleGestureDetector.getScaleFactor();
        mScaleFactor = Math.max(minScale,
                Math.min(mScaleFactor, maxScale));

        constraint_canvas.setScaleX(mScaleFactor);
        constraint_canvas.setScaleY(mScaleFactor);


        if (!primerMultiTouch) {
            moveimgx_ref = ((xref-a_canvas-(a_canvas2))/a_canvas2);
            moveimgy_ref = ((yref-b_canvas-(b_canvas2))/b_canvas2);

            xref_scale = x;
            yref_scale = y;
            primerMultiTouch = true;
        }


        if (mScaleFactor != mScaleFactorTemp) {     //PORQUE SE DESPLAZA UN POCO HACIA ABAJO Y A LA DERECHA
            //UN POCO CUANDO COMIENZA A ESCALAR
            // (BUSCAR OTRA SOLUCION)

            constraint_canvas.setScaleX(mScaleFactor);
            constraint_canvas.setScaleY(mScaleFactor);
            a_canvas2 = mScaleFactor;
            b_canvas2 = mScaleFactor;



            if (primerMultiTouch) {
                ////////////////////////////////// CORRECCION EN Y ////////////////////////////////////////////////////////////////
                float puntoInteresY = moveimgy_ref*b_canvas2*1.0f;             //punto de interes teniendo como referencia a constraint_canvas (parte del canvas que quiero enfocar)
                linear_activity.getLocationOnScreen(location_activity);
                float ubicacionPuntoInteresY = yref_scale - location_activity[1];         //ubicacion en la pantalla del punto de interes (centro desde donde se hace escalamiento)

                float y_tope = Math.min(Math.max( (ubicacionPuntoInteresY - puntoInteresY), (height_disp/2.0f - canvas_height*b_canvas2)),
                        margenVertical);

                float movecanvasyTemp =  (  -(canvas_height/2.0f) + (b_canvas2*canvas_height/2) + y_tope);

                constraint_canvas.setY(movecanvasyTemp);


                ////////////////////////////////// CORRECCION EN X ////////////////////////////////////////////////////////////////
                // /*
                if ((width_disp - margenHorizontal*2) < (canvas_width*a_canvas2)) {
                    float puntoInteresX = moveimgx_ref*a_canvas2*1.0f;             //punto de interes teniendo como referencia a constraint_canvas
                    float ubicacionPuntoInteresX = xref_scale - location_activity[0];         //ubicacion en la pantalla del punto de interes

                    float x_tope = Math.min(Math.max( (ubicacionPuntoInteresX - puntoInteresX), (width_disp - canvas_width*a_canvas2 - margenHorizontal)),
                            margenHorizontal);

                    float movecanvasxTemp =  (  -(canvas_width/2.0f) + (a_canvas2*canvas_width/2) + x_tope);

                    constraint_canvas.setX(movecanvasxTemp);

                } else {
                    constraint_canvas.setX((width_disp-(canvas_width))/2);
                }
                // */
            }




            mScaleFactorTemp = mScaleFactor;

        }

    }

    public float diferenciaX(float a){
        constraint_canvas.setX(a);
        constraint_canvas.getLocationOnScreen(location_canvas);
        float a2 = location_canvas[0];
        if (pre_touch)   global_difx = a2-a;
        return a2-a;
    }
    public float diferenciaY(float b){
        constraint_canvas.setY(b);
        constraint_canvas.getLocationOnScreen(location_canvas);
        float b2 = location_canvas[1];
        if (pre_touch)   global_dify = b2-b;
        return b2-b;
    }


    @SuppressLint("ClickableViewAccessibility")
    public void oyente(){
        linear_activity.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mScaleGestureDetector.onTouchEvent(motionEvent);

                desplazamientoCanvas(motionEvent);

                //pruebaPosicion();

                return true;
            }
        });
    }

    private void pruebaPosicion() {

        Workspace.iv.setVisibility(VISIBLE);
        Workspace.iv.setX(width_disp/2);

        float pY = constraint_canvas.getY() + (canvas_height/2.0f) - (b_canvas2*canvas_height/2.0f);
        float pX = constraint_canvas.getX() + (canvas_width/2.0f) - (b_canvas2*canvas_width/2.0f);

        Workspace.iv.setY(pY);

        //Workspace.iv.setVisibility(VISIBLE);
        //Workspace.iv.setLayoutParams(new FrameLayout.LayoutParams(200,200));

        Log.d("POSICION", "SCALE = " + ((- pY + margenVertical) / constraint_canvas.getScaleY()) );

        constraint_canvas.getFiguras().get(0).getContenedorFigura().setY(
                 (- pY) / constraint_canvas.getScaleY()
        );

        constraint_canvas.getFiguras().get(0).getContenedorFigura().setX(
                 (- pX) / constraint_canvas.getScaleY()
        );

        //constraint_canvas.getFiguras().get(0).getContenedorFigura().setScaleX(1.0f/(b_canvas2));
        //constraint_canvas.getFiguras().get(0).getContenedorFigura().setScaleY(1.0f/(b_canvas2));

    }

    private boolean primerMultiTouch = false;
    private float xref_scale = 0;
    private float yref_scale = 0;


    CountDownTimer countDownTimer;

    MotionEvent me;
    boolean encontro;

    private void ubicar () {
        encontro = constraint_canvas.ubicarFigura(me);

        if (encontro) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            //Vibrate for 20 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(20,VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(20);
            }
        }
    }

    public void desplazamientoCanvas (MotionEvent motionEvent) {
        me = motionEvent;
        //constraint_canvas.enfoqueAuto(false);
        /*
        if (motionEvent.getPointerCount() > 1 && !primerMultiTouch) {
            moveimgx_ref = ((xref-a_canvas-(a_canvas2))/a_canvas2);
            moveimgy_ref = ((yref-b_canvas-(b_canvas2))/b_canvas2);

            xref_scale = motionEvent.getRawX();
            yref_scale = motionEvent.getRawY();
            primerMultiTouch = true;
        }
        */



        int action = motionEvent.getActionMasked();
        switch (action) {

            case MotionEvent.ACTION_DOWN:
                encontro = false;

                constraint_canvas.setTocandoCanvas(true);

                xref = motionEvent.getRawX();
                yref = motionEvent.getRawY();

                constraint_canvas.getLocationOnScreen(location_canvas);
                a_canvas = location_canvas[0];
                b_canvas = location_canvas[1];

                x_canvas = location_canvas[0];
                y_canvas = location_canvas[1];

                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
                countDownTimer = new CountDownTimer(500,1) {

                    @Override
                    public void onTick(long millisUntilFinished) {
                        Log.d("COUNTZOOM: ", "" + millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                        Log.d("COUNTZOOM: ", "DONE");
                        if (Workspace.editorDibujoAbierto) {
                            PanelDibujo.setDibujo();
                            //Workspace.ED_estado_motion_event.setText("DRAW");
                            Workspace.ED_estado_motion_event.setImageResource(R.drawable.editor_dibujo_draw);

                            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                            //Vibrate for 20 milliseconds
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(20,VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                //deprecated in API 26
                                v.vibrate(20);
                            }

                        } else {
                            ubicar();
                        }
                    }
                };

                countDownTimer.start();

                break;

            case MotionEvent.ACTION_MOVE:
                x = motionEvent.getRawX();
                y = motionEvent.getRawY();

                if(scaleOngoing){
                    allowmove = false;
                }
                if(allowmove){
                    constraint_canvas.getLocationOnScreen(location_canvas);

                    //int difx = diferenciaX(a_canvas);
                    //int dify = diferenciaY(b_canvas);

                    x = motionEvent.getRawX();
                    y = motionEvent.getRawY();

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////
                    if ((width_disp - margenHorizontal*2) < (canvas_width*a_canvas2)) {
                        float difx = diferenciaX(a_canvas);

                        movecanvasx = ((x - xref)+(a_canvas-difx));

                        if (  ((movecanvasx + (canvas_width/2.0f)) + (a_canvas2*canvas_width/2)) < (width_disp - margenHorizontal) )
                            movecanvasx = ( -(canvas_width/2.0f) - (a_canvas2*canvas_width/2) + (width_disp - margenHorizontal) );


                        if (  ((movecanvasx + (canvas_width/2.0f)) - (a_canvas2*canvas_width/2)) > margenHorizontal )
                            movecanvasx = ( -(canvas_width/2.0f) + (a_canvas2*canvas_width/2) + margenHorizontal );

                        constraint_canvas.setX(movecanvasx);
                    } else {
                        constraint_canvas.setX((width_disp-(canvas_width))/2);
                    }

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////
                    float dify = diferenciaY(b_canvas);

                    movecanvasy = ((y - yref)+(b_canvas-dify));

                    if (  ((movecanvasy + (canvas_height/2.0f)) + (b_canvas2*canvas_height/2)) < (height_disp/2.0f) )
                        movecanvasy = ( -(canvas_height/2.0f) - (b_canvas2*canvas_height/2) + (height_disp/2.0f) );

                    if (  ((movecanvasy + (canvas_height/2.0f)) - (b_canvas2*canvas_height/2)) > margenVertical )
                        movecanvasy = ( -(canvas_height/2.0f) + (b_canvas2*canvas_height/2) + margenVertical );

                    constraint_canvas.setY(movecanvasy);

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////


                }


                if((Math.abs(x-xref)>10) || (Math.abs(y-yref)>10) ){
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }

                    if (constraint_canvas.getFocoFigura().getContenedorFoco().getVisibility() == VISIBLE) {
                        constraint_canvas.getFocoFigura().getContenedorFoco().setVisibility(INVISIBLE);
                        focoEscondido = true;
                    }
                }


                break;

            case MotionEvent.ACTION_UP:
                if (focoEscondido) {
                    constraint_canvas.enfoqueAuto(false);
                    focoEscondido = false;
                }


                constraint_canvas.setTocandoCanvas(false);

                pre_touch = false;

                allowmove = true;

                x = motionEvent.getRawX();
                y = motionEvent.getRawY();

                primerMultiTouch = false;

                if((Math.abs(x-xref)<10) && (Math.abs(y-yref)<10) && !encontro){
                    constraint_canvas.enfoqueAuto(true);
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                    /*
                    if (Workspace.panel_titulo_workspace.getVisibility() == VISIBLE) {
                        Workspace.panel_titulo_workspace.setVisibility(GONE);
                    } else Workspace.panel_titulo_workspace.setVisibility(VISIBLE);
                    */
                }
                break;
        }
    }

    private boolean focoEscondido; //para cuando se oculta el foco al momento de mover el canvas mientras hay una figura enfocada


    public void recenter () {
        minScale = 0.4f;
        maxScale = 6.0f;

        minScale = ((width_disp-width_disp* minScale )*1.0f)/(canvas_width*1.0f);
        maxScale = maxScale * (width_disp*1.0f)/(canvas_width*1.0f);

        mScaleFactor = ((width_disp-(margenHorizontal)*2.0f)*1.0f)/(canvas_width*1.0f);
        mScaleFactorTemp = mScaleFactor;
        //mScaleFactor = minScale;
        a_canvas2 = mScaleFactor;
        b_canvas2 = mScaleFactor;
        constraint_canvas.setScaleX(a_canvas2);
        constraint_canvas.setScaleY(b_canvas2);

        constraint_canvas.setX((width_disp-constraint_canvas.getLayoutParams().width)/2);
        constraint_canvas.setY(( -(canvas_height/2.0f) + (b_canvas2*canvas_height/2) + margenVertical ));

    }

    public void recenter (int width_disp, int height_disp) {
        minScale = 0.4f;
        maxScale = 6.0f;

        float factorPrevio = constraint_canvas.getScaleY();
        constraint_canvas.getLocationOnScreen(location_canvas);
        linear_activity.getLocationOnScreen(location_activity);

        //-------------------------------ACTUALIZANDO DATOS DE LA NUEVA ORIENTACION------------------------------
        this.width_disp = width_disp;
        this.height_disp = height_disp;

        minScale = ((width_disp-width_disp* minScale )*1.0f)/(canvas_width*1.0f);
        maxScale = maxScale * (width_disp*1.0f)/(canvas_width*1.0f);

        mScaleFactor = ((width_disp-(margenHorizontal)*2.0f)*1.0f)/(canvas_width*1.0f);
        mScaleFactorTemp = mScaleFactor;

        a_canvas2 = mScaleFactor;
        b_canvas2 = mScaleFactor;
        constraint_canvas.setScaleX(a_canvas2);
        constraint_canvas.setScaleY(b_canvas2);

        //se obtiene el valor de la posicion del canvas en la orientacion anterior
        float pY = (-margenVertical + constraint_canvas.getY() + (canvas_height/2.0f) - (factorPrevio*canvas_height/2.0f) ) / factorPrevio;
        float pX = (-margenHorizontal + constraint_canvas.getX() + (canvas_width/2.0f) - (factorPrevio*canvas_width/2.0f) ) / factorPrevio;

        //se ubica el canvas de la nueva orientacion a partir del valor calculado anteior
        constraint_canvas.setX((width_disp-constraint_canvas.getLayoutParams().width)/2);

        pY = ( - (canvas_height/2.0f) + (b_canvas2*canvas_height/2.0f) + ( pY )*b_canvas2 + margenVertical);
        constraint_canvas.setY(pY);

        //se corrige la posicion si se pasa de las margenes
        pY = constraint_canvas.getY() + (canvas_height/2.0f) - (b_canvas2*canvas_height/2.0f);

        if (Math.max(pY + canvas_height*b_canvas2, height_disp/2.0f) == height_disp/2.0f) { //margen inferior
            //Toast.makeText(context, "Se pasó del límite inferior", Toast.LENGTH_SHORT).show();
            pY = constraint_canvas.getY() - (pY + canvas_height*b_canvas2 - height_disp/2.0f);
            constraint_canvas.setY(pY);
        }
        pY = constraint_canvas.getY() + (canvas_height/2.0f) - (b_canvas2*canvas_height/2.0f);
        if (Math.min(pY, margenVertical) == margenVertical) { //margen superior
            //Toast.makeText(context, "Se pasó del límite superior", Toast.LENGTH_SHORT).show();
            pY = ( -(canvas_height/2.0f) + (b_canvas2*canvas_height/2) + margenVertical );
            constraint_canvas.setY(pY);
        }

        //Se debe reenfocar la figura porque al voltear el foco podria aparecer en otra parte
        constraint_canvas.enfoqueAuto(false);
    }

    public void bajarCalidad (boolean todas) {
        ArrayList<Figura> figuras = constraint_canvas.getFiguras();

        if (todas) {

            for (int i = 0; i < figuras.size(); i++) {
                if (figuras.get(i).getTipoVista() == Figura.IMAGE) {

                    ((ImageView)figuras.get(i).getVista()).setImageBitmap(
                            Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(figuras.get(i).getByteArray(),
                                    0, figuras.get(i).getByteArray().length
                            ), 200, 200, false )
                    );

                    //((ImageView)figuras.get(i).getVista()).setImageResource(R.drawable.ic_launcher_background);
                }
            }

        } else {
            ((ImageView)figuras.get(figuras.size()-1).getVista()).setImageResource(R.drawable.ic_launcher_background);
        }

    }

}
