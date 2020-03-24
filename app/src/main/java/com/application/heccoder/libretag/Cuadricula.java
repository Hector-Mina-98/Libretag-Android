package com.application.heccoder.libretag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Cuadricula extends View {

    private int canvas_width, canvas_height;

    private int tipoCuadricula;
    public static int CUADRICULADO = 0;
    public static int RAYADO = 1;
    public static int NINGUNO = -1;

    public Cuadricula(Context context, int canvas_width, int canvas_height, int tipoCuadricula) {
        super(context);
        this.canvas_width = canvas_width;
        this.canvas_height = canvas_height;

        this.tipoCuadricula = tipoCuadricula;
    }

    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {

        int cuadricula_width = (canvas_width - 100) - (canvas_width - 100)%30;
        //int cuadricula_width = canvas_width;
        int cuadricula_height = canvas_height;

        setX( (canvas_width - cuadricula_width)/2.0f );


        Paint miPincelcuad = new Paint();
        miPincelcuad.setColor(new Color().argb(20,0,0,0));      //argb(int alpha, int red, int green, int blue)
        miPincelcuad.setStrokeWidth(5);     //establece grosor del trazo
        miPincelcuad.setStyle(Paint.Style.STROKE);

        //canvas.drawLine(0, canvas_height/2, canvas_width, canvas_height/2, miPincelcuad); //horizontal ppl
        //canvas.drawLine(canvas_width/2, 0, canvas_width/2, canvas_height, miPincelcuad); //vertical ppl


        if (tipoCuadricula == CUADRICULADO) {
            miPincelcuad.setStrokeWidth(2);
            //lineas verticales
            for (int i = 0; i <= cuadricula_width; i += 30) {
                canvas.drawLine(i, 0, i, cuadricula_height, miPincelcuad);   //drawLine(float startX, float startY, float stopX, float stopY, Paint paint)
            }

            //lineas horizontales
            for (int i = 0; i <= cuadricula_height; i += 30) {
                canvas.drawLine(0, i, cuadricula_width, i, miPincelcuad);   //drawLine(float startX, float startY, float stopX, float stopY, Paint paint)
            }

        } else if (tipoCuadricula == RAYADO) {
            //lineas verticales
            miPincelcuad.setStrokeWidth(7);
            canvas.drawLine(0, 0, 0, cuadricula_height, miPincelcuad);
            miPincelcuad.setStrokeWidth(3);
            canvas.drawLine(cuadricula_width, 0, cuadricula_width, cuadricula_height, miPincelcuad);

            miPincelcuad.setStrokeWidth(2);
            //lineas horizontales
            for (int i = 0; i <= cuadricula_height; i += 30) {
                canvas.drawLine(0, i, cuadricula_width, i, miPincelcuad);   //drawLine(float startX, float startY, float stopX, float stopY, Paint paint)
            }
        }

    }
}
