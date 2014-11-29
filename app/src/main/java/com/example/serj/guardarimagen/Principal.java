package com.example.serj.guardarimagen;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class Principal extends Activity {

    private String[] parametros;                                                    // URL + destino
    private EditText etUrl, etNombre;
    private RadioButton rbPrivada, rbPublica;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_principal);
        initComponents();
    }

    public void guardarImagen(View v) {
        // Lanza un hilo
        parametros = getParametros();
        if(parametros != null) {
            new Hilo().execute(parametros);
        }
    }

    class Hilo extends AsyncTask<String[], Integer, String> {
        // GUARDAR IMAGEN
        @Override
        protected String doInBackground(String[]... params) {
            String url = parametros[0];
            String destino = parametros[1];
            try {
                guardarImagen(url, destino);
            } catch (IOException e) { e.printStackTrace(); }
            return destino;    //devolver ruta imagen --> onPostExecute
        }

        @Override
        protected void onPostExecute(String s) {
            // PONER IMAGEN en ImageView
            super.onPostExecute(s);
            ImageView iv = (ImageView)findViewById(R.id.imageView2);
            iv.setImageBitmap(BitmapFactory.decodeFile(s));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    public void initComponents() {
        parametros = new String[2];
        // ver si esta en horizontal o vertical ...
        etUrl = (EditText)findViewById(R.id.etUrl);
        etNombre = (EditText)findViewById(R.id.etNombre);
        rbPrivada = (RadioButton)findViewById(R.id.rbPrivada);
        rbPublica = (RadioButton)findViewById(R.id.rbPublica);
    }

    public String[] getParametros() {

        String url, destino=null, ruta, nombre, extension;

        if(!etUrl.getText().toString().isEmpty()) {
            url = etUrl.getText().toString();
            ruta = getRuta();
            if(!etNombre.getText().toString().isEmpty()) {
                nombre = etNombre.getText().toString();
            } else {
                nombre = getNombre(url);
            }
            extension = getExtension(url);
            if(!checkExtension(extension)) {
                extension = null;
            }
            if(extension != null){
                destino = ruta + nombre + extension;
            } else {
                destino = null;
            }
        } else {
            url = null;
        }

        if (url == null) {
            Toast.makeText(this, "Debe especificar una URL válida", Toast.LENGTH_SHORT).show();
            return null;
        } else if (destino == null) {
            Toast.makeText(this, "Tipo de imagen no soportado", Toast.LENGTH_SHORT).show();
            return null;
        } else {
            parametros[0] = url;
            parametros[1] = destino;

            Log.v("Parametros", "URL: "+parametros[0] + " Destino: "+parametros[1]);

            return parametros;
        }
    }

    public String getExtension(String url) {
        String extension = "";

        int i = url.lastIndexOf('.');
        if (i > 0) {
            extension = url.substring(i+1);
        }
        return "." + extension;
    }

    public String getNombre(String url) {
        int barra = url.lastIndexOf('/');
        int punto = url.lastIndexOf('.');
        if(punto > barra){
            return url.substring(barra + 1, punto);
        }else{
            return null;
        }
    }

    public String getRuta(){
        if(rbPrivada.isChecked()) {
            return getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath()+"/";
        } else if (rbPublica.isChecked()) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()+"/";
        } else {
            return null;
        }
    }

    public boolean checkExtension(String extension) {
        if(!extension.contains(".png") || !extension.contains(".jpg") || !extension.contains(".gif")){
            if(extension.length() < 5){
                return true;
            }
        }
        return false;
    }

    public static void guardarImagen(String imageUrl, String destinationFile) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }
}