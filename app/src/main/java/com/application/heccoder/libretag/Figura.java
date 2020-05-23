package com.application.heccoder.libretag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class Figura implements Serializable {



    transient private View vista;
    transient private LinearLayout contenedorFigura;
    transient private LinearLayout contenedorVista;
    transient private ConstraintCanvas canvasPadre;


    static final int EDIT_TEXT = 0;
    static final int TEXT_VIEW = 1;
    static final int IMAGE = 2;
    ///////////////////////////////////////////////////////////////////////////////////////////////
    transient private LinearLayout.LayoutParams layoutParams;
    ///////////////////////////////////////////////////////////////////////////////////////////////

    Figura(LinearLayout contenedorFigura, int tipoVista, ConstraintCanvas canvasPadre, int idFigura) {
        this.idFigura = idFigura;
        this.canvasPadre = canvasPadre;
        this.tipoVista = tipoVista;
        this.contenedorFigura = contenedorFigura;
        if (this.contenedorFigura != null)this.contenedorVista = contenedorFigura.findViewById(R.id.contenedor_vista);

        ////////////////////////////////////////////////////////////////////////////////////
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        contenidoTexto = "";

        link = "";
        linkEnable = false;
    }



    LinearLayout getContenedorFigura() {
        return contenedorFigura;
    }



    void crearVista() {

        switch (tipoVista) {
            case EDIT_TEXT:
                vista = new EditText(contenedorFigura.getContext());
                ((EditText) vista).setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                ((EditText) vista).setHint("MANTENGA PRESIONADO AQUÍ PARA EDITAR");
                (vista).setBackgroundColor(Color.TRANSPARENT);
                ((EditText) vista).setTextSize(18);
                contenedorVista.addView(vista);
                (vista).requestFocus();

                //layoutParams.setMargins(20, 0, 20, 0);      //setMargins(left, top, right, bottom)
                //vista.setLayoutParams(layoutParams);
                ((EditText)vista).setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);     //deshabilita el modo de edicion de landscape

                InputMethodManager imm = (InputMethodManager)(canvasPadre.getContext()).getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                imm.showSoftInput(vista, InputMethodManager.SHOW_IMPLICIT);
                break;

            case TEXT_VIEW:
                vista = new TextView(contenedorFigura.getContext());
                ((TextView) vista).setHint("MANTENGA PRESIONADO AQUÍ PARA EDITAR");
                ((TextView) vista).setTextSize(TypedValue.COMPLEX_UNIT_PX, 30); //TypedValue.COMPLEX_UNIT_PX hace que el texto
                                                                                     //sea independiente del "font size" y el "display_size"
                ((TextView) vista).setTextColor(Color.parseColor("#000000"));

                typeface = Typeface.defaultFromStyle(Typeface.NORMAL);

                //((TextView) vista).setWidth(60);
                //lo siguiente elimina el hint cuando ya hay texto en la figura, para que el hint no afecte el ancho
                ((TextView) vista).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (((TextView) vista).getText().toString().length() == 0) {
                            ((TextView) vista).setHint("MANTENGA PRESIONADO AQUÍ PARA EDITAR");
                        } else ((TextView) vista).setHint("");
                    }
                });

                contenedorVista.addView(vista);
                //layoutParams.setMargins(20, 20, 20, 20);      //setMargins(left, top, right, bottom)
                //vista.setLayoutParams(layoutParams);
                break;

            case IMAGE:
                vista = new ImageView(contenedorFigura.getContext());
                ((ImageView) vista).setImageResource(R.drawable.img_default);
                //((ImageView) vista).setImageBitmap(BitmapFactory.
                //decodeResource(contenedorFigura.getResources(),R.drawable.ic_launcher_background));
                //InputStream inputStream = getClass().getResourceAsStream(Environment.getDownloadCacheDirectory()+ "/img.jpg");
                //((ImageView) vista).setImageDrawable(Drawable.createFromStream(inputStream, ""));
                contenedorVista.addView(vista);
                (vista).setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                //contenedorFigura.setScaleX(1.5387f);
                //contenedorFigura.setRotation(45);
                break;
            case 3:
                break;
            case 4:
                break;
        }

        oyenteFiguras(tipoVista);

    }




    @SuppressLint("ClickableViewAccessibility")
    private void oyenteFiguras (int tipoFigura) {

        switch (tipoFigura) {

            case Figura.IMAGE:

                break;

            case Figura.TEXT_VIEW:

                break;

            case Figura.EDIT_TEXT:
                ((EditText)vista).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        canvasPadre.enfoque(contenedorFigura, Figura.this);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        canvasPadre.enfoque(contenedorFigura,Figura.this);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        canvasPadre.enfoque(contenedorFigura,Figura.this);
                    }
                });

        }

    }




    void cambiarTipoTexto (int cambiarATipo) {

        String textoTemporal;

        switch (cambiarATipo) {

            case Figura.EDIT_TEXT:
                /*
                textoTemporal = ((TextView) vista).getText().toString();
                vista.setVisibility(View.GONE);
                vista = null;
                tipoVista = Figura.EDIT_TEXT;
                crearVista();
                ((EditText) vista).setText(textoTemporal);
                ((EditText) vista).setSelection(((EditText) vista).getText().length());
                */
                //TODO Aquí debería salir una ventana emergente con EditText
                break;

            case Figura.TEXT_VIEW:
                textoTemporal = ((EditText) vista).getText().toString();
                vista.setVisibility(View.GONE);
                vista = null;
                tipoVista = Figura.TEXT_VIEW;
                crearVista();
                ((TextView) vista).setText(textoTemporal);
                break;

        }

    }




    //============================================ CAMPOS QUE SE ALMACENAN =================================================
    private final int idFigura;
    private int tipoVista;

    int getIdFigura() {
        return idFigura;
    }
    int getTipoVista() {
        return tipoVista;
    }

    private float posX, posY;
    public float getPosX() {
        return posX;
    }
    public float getPosY() {
        return posY;
    }
    public void setPosX(float posX) {
        this.posX = posX;
        if (this.contenedorFigura != null) contenedorFigura.setX(posX);
    }
    public void setPosY(float posY) {
        this.posY = posY;
        if (this.contenedorFigura != null) contenedorFigura.setY(posY);
    }

    private float rot;
    public void setRot(float rot) {
        this.rot = rot;
        if (this.contenedorFigura != null) contenedorFigura.setRotation(rot);
    }
    public float getRot() {
        return rot;
    }

    private float scaX, scaY;
    public float getScaX() {
        return scaX;
    }
    public float getScaY() {
        return scaY;
    }
    public void setScaX(float scaX) {
        this.scaX = scaX;
        if (this.contenedorFigura != null) contenedorFigura.setScaleX(scaX);
    }
    public void setScaY(float scaY) {
        this.scaY = scaY;
        if (this.contenedorFigura != null) contenedorFigura.setScaleY(scaY);
    }

    public View getVista() {
        return vista;
    }
    private int width, height;
    public int get_width() {
        return width;
    }
    public int get_height() {
        return height;
    }
    public void set_dim(int width, int height) {
        this.width = width;
        this.height = height;
        if (this.vista != null) vista.setLayoutParams(new LinearLayout.LayoutParams(width, height));
    }

    private byte[] byteArray;
    public byte[] getByteArray() {
        return byteArray;
    }
    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
        if (tipoVista == IMAGE) {
            if (this.vista != null) ((ImageView) vista).setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));
        }
    }

    private int imageAlpha;
    public int getImageAlpha() {
        return imageAlpha;
    }
    public void setImageAlpha(int imageAlpha) {
        if ( imageAlpha == 0 ) imageAlpha = 255;
        this.imageAlpha = imageAlpha;
        if (tipoVista == IMAGE) {
            if (this.vista != null) ((ImageView) vista).setImageAlpha(imageAlpha);
        }
    }

    private String contenidoTexto;
    public String getContenidoTexto() {
        return contenidoTexto;
    }
    public void setContenidoTexto(String contenidoTexto) {
        this.contenidoTexto = contenidoTexto;
        if (this.vista != null) ((TextView) vista).setText(contenidoTexto);
    }

    private int text_size;
    public int getText_size() {
        return text_size;
    }
    public void setText_size(int text_size) {
        if (text_size == 0) text_size = 30;
        this.text_size = text_size;
        if (this.vista != null) ((TextView) vista).setTextSize(TypedValue.COMPLEX_UNIT_PX, text_size);
    }

    private int text_color;
    public int getText_color() {
        return text_color;
    }
    public void setText_color(int text_color) {
        if (text_color == 0) text_color = Color.parseColor("#000000");
        this.text_color = text_color;
        if (this.vista != null) ((TextView) vista).setTextColor(text_color);
    }

    private int text_background;
    public int getText_background() {
        return text_background;
    }
    public void setText_background(int text_background) {
        if (text_background == 0) text_background = Color.TRANSPARENT;
        this.text_background = text_background;
        if (this.vista != null) vista.setBackgroundColor(text_background);
    }

    private int text_alignment;
    public int getText_alignment() {
        return text_alignment;
    }

    public void setText_alignment(int text_alignment) {
        this.text_alignment = text_alignment;
        if (text_alignment != 0 && this.vista != null)  ((TextView)vista).setGravity(text_alignment);
    }

    private int text_style;
    private Typeface typeface;
    public int getText_style() {
        return text_style;
    }
    public void setText_style(int text_style) { //TODO queda pendiente la fuente del texto
        this.text_style = text_style;
        if (this.vista != null) ((TextView)this.vista).setTypeface(typeface, text_style);
        //NORMAL = 0 //BOLD = 1 //ITALIC = 2 //BOLD_ITALIC = 3
    }

    private int text_width;
    public int getText_width() {
        return text_width;
    }
    public void setText_width(int text_width) {
        if (text_width <= 0) text_width = 20;
        this.text_width = text_width;
        if (this.vista != null) ((TextView)this.vista).setWidth(text_width);
    }

    private String link;
    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }

    private boolean linkEnable;
    public boolean isLinkEnable() {
        return linkEnable;
    }
    public void setLinkEnable(boolean linkEnable) {
        this.linkEnable = linkEnable;
    }
}