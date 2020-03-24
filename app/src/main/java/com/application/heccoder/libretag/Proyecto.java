package com.application.heccoder.libretag;

import java.io.Serializable;
import java.util.ArrayList;

public class Proyecto implements Serializable {

    public Proyecto () {
        w_canvas = 816;
        h_canvas = 1054;

        tipoCuadricula = Cuadricula.CUADRICULADO;
    }

    private int w_canvas, h_canvas;
    public int getW_canvas() {
        return w_canvas;
    }
    public void setW_canvas(int w_canvas) {
        this.w_canvas = w_canvas;
    }
    public int getH_canvas() {
        return h_canvas;
    }
    public void setH_canvas(int h_canvas) {
        this.h_canvas = h_canvas;
    }

    private ArrayList<Figura> figuras;
    public ArrayList<Figura> getFiguras() {
        return figuras;
    }
    public void setFiguras(ArrayList<Figura> figuras) {
        this.figuras = figuras;
    }

    private int tipoCuadricula;
    public int getTipoCuadricula() {
        return tipoCuadricula;
    }
    public void setTipoCuadricula(int tipoCuadricula) {
        this.tipoCuadricula = tipoCuadricula;
    }
}
