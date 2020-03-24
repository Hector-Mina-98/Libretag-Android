package com.application.heccoder.libretag;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;


public class PanelDibujo extends View {

    //Path que utilizaré para ir pintando las lineas
    private Path drawPath;
    //Paint de dibujar y Paint de Canvas
    private static Paint drawPaint;
    private Paint canvasPaint;
    //Color Inicial
    private static int paintColor = 0xFFFF0000;
    //canvas
    private Canvas drawCanvas;
    //canvas para guardar
    private Bitmap canvasBitmap;

    static float TamanyoPunto;
    private static boolean borrado=false;


    private int w_canvas, h_canvas;

    private Context context;

    public PanelDibujo(Context context, int w_canvas, int h_canvas) {
        super(context);
        this.context = context;
        this.w_canvas = w_canvas;
        this.h_canvas = h_canvas;
        setupDrawing();

        dibujo = false;

        XsinTrim = YsinTrim = 0;
    }


    private void setupDrawing(){
        //Configuración del area sobre la que pintar

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);     //metodo de esta clase
        drawPaint.setAntiAlias(true);   //esto le da suavidad a los bordes de las lineas

        //setTamanyoPunto(20);      //tamano del punto en dp

        drawPaint.setStrokeWidth(5);       //tamano del punto pero NO en dp

        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);      //esquinas redondeadas
        drawPaint.setStrokeCap(Paint.Cap.ROUND);        //cada punto es redondo
        canvasPaint = new Paint(Paint.DITHER_FLAG);     //tiene que ver algo con el color, consultar en la API.


        canvasBitmap = Bitmap.createBitmap(w_canvas, h_canvas, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

    }


    //Tamaño asignado a la vista, aqui tambien se crea el bitmap
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //super.onSizeChanged(w, h, oldw, oldh);
        //canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //drawCanvas = new Canvas(canvasBitmap);
    }



    //Pinta la vista. Será llamado desde el OnTouchEvent
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }






    CountDownTimer countDownTimer;
    float touchXref, touchYref;

    //Registra los touch de usuario
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        if (dibujo) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchXref = event.getX();
                    touchYref = event.getY();

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }

                    countDownTimer = new CountDownTimer(500,1) {

                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            if (Workspace.editorDibujoAbierto) {
                                PanelDibujo.setDibujo();
                                //Workspace.ED_estado_motion_event.setText("MOVE");
                                Workspace.ED_estado_motion_event.setImageResource(R.drawable.editor_dibujo_move);
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
                    };

                    countDownTimer.start();



                    drawPath.moveTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!borrado) {
                        if((Math.abs(touchX-touchXref)>5) || (Math.abs(touchY-touchYref)>5) ) drawPath.lineTo(touchX, touchY);
                    } else {
                        drawPath.lineTo(touchX, touchY);
                        drawCanvas.drawPath(drawPath, drawPaint);
                        drawPath.reset();
                        drawPath.moveTo(touchX,touchY);
                    }

                    if((Math.abs(touchX-touchXref)>5) || (Math.abs(touchY-touchYref)>5) ){
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                            countDownTimer = null;
                        }
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    drawPath.lineTo(touchX, touchY);
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();

                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
                    break;
                default:
                    return false;
            }
            //repintar
            invalidate();
        }

        return dibujo;

    }

    public static boolean dibujo;
    public static void setDibujo () {
        dibujo = !dibujo;
    }












    //Actualiza color
    public void setColor(int paintColor){
        invalidate();
        this.paintColor = paintColor;
        drawPaint.setColor(paintColor);
    }

    public int getColor () {
       return this.paintColor;
    }

    //Poner tamaño del punto
    public static void setTamanyoPunto(float nuevoTamanyo){


        //float pixel = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
        //        nuevoTamanyo, getResources().getDisplayMetrics());

        //TamanyoPunto=pixel;
        drawPaint.setStrokeWidth(nuevoTamanyo);
    }


    //set borrado true or false
    public static void setBorrado(boolean estaborrado){
        borrado=estaborrado;
        if(borrado) {

            //drawPaint.setColor(Color.WHITE);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        }
        else {
            //drawPaint.setColor(paintColor);
            drawPaint.setXfermode(null);
        }
    }

    public void NuevoDibujo(){
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        XsinTrim = YsinTrim = 0;
        invalidate();

    }


    public Bitmap getCanvasBitmap() {
        return trim(this.canvasBitmap);
    }


    public static int XsinTrim, YsinTrim;

    //este es el metodo mas lento
    public static Bitmap createTrimmedBitmap(Bitmap bmp) {
        int imgHeight = bmp.getHeight();
        int imgWidth  = bmp.getWidth();
        int smallX=0,largeX=imgWidth,smallY=0,largeY=imgHeight;
        int left=imgWidth,right=imgWidth,top=imgHeight,bottom=imgHeight;
        for(int i=0;i<imgWidth;i++)
        {
            for(int j=0;j<imgHeight;j++)
            {
                if(bmp.getPixel(i, j) != Color.TRANSPARENT){
                    if((i-smallX)<left){
                        left=(i-smallX);
                    }
                    if((largeX-i)<right)
                    {
                        right=(largeX-i);
                    }
                    if((j-smallY)<top)
                    {
                        top=(j-smallY);
                    }
                    if((largeY-j)<bottom)
                    {
                        bottom=(largeY-j);
                    }
                }
            }
        }
        bmp=Bitmap.createBitmap(bmp,left,top,imgWidth-left-right, imgHeight-top-bottom);

        return bmp;

    }


    //este es el metodo mas rapido
    static Bitmap trim(Bitmap source) {
        int firstX = 0, firstY = 0;
        int lastX = source.getWidth();
        int lastY = source.getHeight();
        int[] pixels = new int[source.getWidth() * source.getHeight()];
        source.getPixels(pixels, 0, source.getWidth(), 0, 0, source.getWidth(), source.getHeight());
        loop:
        for (int x = 0; x < source.getWidth(); x++) {
            for (int y = 0; y < source.getHeight(); y++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstX = x;
                    XsinTrim = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = firstX; x < source.getWidth(); x++) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    firstY = y;
                    YsinTrim = y;
                    break loop;
                }
            }
        }
        loop:
        for (int x = source.getWidth() - 1; x >= firstX; x--) {
            for (int y = source.getHeight() - 1; y >= firstY; y--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastX = x;
                    break loop;
                }
            }
        }
        loop:
        for (int y = source.getHeight() - 1; y >= firstY; y--) {
            for (int x = source.getWidth() - 1; x >= firstX; x--) {
                if (pixels[x + (y * source.getWidth())] != Color.TRANSPARENT) {
                    lastY = y;
                    break loop;
                }
            }
        }
        return Bitmap.createBitmap(source, firstX, firstY, lastX - firstX, lastY - firstY);
    }
}
