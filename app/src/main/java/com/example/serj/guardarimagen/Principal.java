package com.example.serj.guardarimagen;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class Principal extends Activity {

    /**********************************************************************************************/
    /**************************************VARIABLES***********************************************/
    /**********************************************************************************************/

    private String[] parametros;                   // Variable donde se almacena la Url y el destino
                                                   // que se usa para descargar la imagen

    /**********************************************************************************************/
    /**************************************ON...***************************************************/
    /**********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Inicializa los componentes del layout principal y la variable de instancia
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_principal);
        initComponents();
    }

    /**********************************************************************************************/
    /***************************************ASYNCTASK**********************************************/
    /**********************************************************************************************/

    class Hilo extends AsyncTask<String[], Integer, String> {
        // Clase Anónima que permite trabajar con hilos en la UI fácilmente
        @Override
        protected String doInBackground(String[]... params) {
            // Método que realiza una acción de fondo para devolver el resultado al hilo UI
            // Recoge la Url y el destino de la imagen a descargar
            String url = parametros[0];
            String destino = parametros[1];
            try {
                // Descarga la imagen en segundo plano
                guardarImagen(url, destino);
            } catch (IOException e) { e.printStackTrace(); }
            // Devuelve la ruta de la imagen para que esta sea recogida en el onPostExecute
            return destino;
        }

        @Override
        protected void onPostExecute(String s) {
            // Método que realiza una acción en el hilo UI
            super.onPostExecute(s);
            if(new File(s).exists()){
                // Comprueba que el fichero existe y en caso afirmativo muestra la imagen en la UI
                ImageView iv = (ImageView)findViewById(R.id.imageView2);
                Bitmap foto = BitmapFactory.decodeFile(s);
                if(foto != null) {
                    iv.setImageBitmap(foto);
                }
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.error_imagen), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**********************************************************************************************/
    /*************************************GUARDAR IMAGEN*******************************************/
    /**********************************************************************************************/

    public void guardarImagen(View v) {
        // Recoge los parámetros de la UI y lanza un hilo
        initComponents();
        parametros = getParametros();
        if(parametros[0] != null && parametros[1] != null) {
            new Hilo().execute(parametros);
        }
    }

    /**********************************************************************************************/
    /***********************************MÉTODOS AUXILIARES*****************************************/
    /**********************************************************************************************/

    public void initComponents() {
        // Método para inicializar la variable dónde se almacenan los parámetros
        parametros = new String[2];
    }

    public String[] getParametros() {
        EditText etUrl = (EditText)findViewById(R.id.etUrl);
        EditText etNombre = (EditText)findViewById(R.id.etNombre);

        String url, destino=null, ruta, nombre, extension;

        if(!etUrl.getText().toString().isEmpty()) {
            // En el caso que el campo de texto dónde se introduce la Url no esté vacío
            // Recoge la Url de la imagen introducida por el usuario
            url = etUrl.getText().toString();
            // Recoge la ruta según el CheckBox seleccionado
            ruta = getRuta();
            if(!etNombre.getText().toString().isEmpty()) {
                // Si el campo de texto dónde se introduce el nombre no está vacío
                // Recoge el nombre del archivo especificado
                nombre = etNombre.getText().toString();
            } else {
                // En caso contrario recoge el nombre que tiene la imagen en la Url
                nombre = getNombre(url);
            }
            // Recoge la extensión de la imagen especificada en la Url
            extension = getExtension(url);
            if(!checkExtension(extension)) {
                // En el caso que no sea una extensión válida no se puede descargar la imagen
                extension = null;
            }
            if(extension != null){
                // Una vez comprobada cada parte de la ruta
                // Se le asigna la ruta dónde se guardará la imagen
                destino = ruta + nombre + extension;
            } else {
                // Si alguna parte no es correcta no se podrá guardar la imagen
                destino = null;
            }
        } else {
            // Si no se especifica una Url no se puede descargar la imagen
            url = null;
        }

        if (url == null) {
            // Si la Url no es correcta hay que informar al usuario
            Toast.makeText(this, getString(R.string.error_url), Toast.LENGTH_SHORT).show();
            return null;
        } else if (destino == null) {
            // Si el tipo de imagen no es correcta hay que informar al usuario
            Toast.makeText(this, getString(R.string.error_destino), Toast.LENGTH_SHORT).show();
            return null;
        } else {
            // En caso de que los parametros sean correctos se asignan a la variable de instancia
            // y se devuelven para poder lanzar el hilo con los parámetros adecuados
            parametros[0] = url;
            parametros[1] = destino;

            //Log.v("Parametros", "URL: "+parametros[0] + " Destino: "+parametros[1]);

            return parametros;
        }
    }

    public String getExtension(String url) {
        // Método para recoger la extensión de una imagen a través de una Url
        String extension = "";

        int i = url.lastIndexOf('.');
        if (i > 0) {
            extension = url.substring(i+1);
        }
        return "." + extension;
    }

    public String getNombre(String url) {
        // Método para recoger el nombre de una imagen a través de una Url
        int barra = url.lastIndexOf('/');
        int punto = url.lastIndexOf('.');
        if(punto > barra){
            return url.substring(barra + 1, punto);
        }else{
            return null;
        }
    }

    public String getRuta(){
        // Método que devuelve la ruta de la carpeta dónde se guardará la imagen
        // Según el CheckBox seleccionado se guardará en:
        //      · La carpeta DCIM de la memoria externa privada
        //      · La carpeta DCIM de la memoria externa pública
        RadioButton rbPrivada = (RadioButton)findViewById(R.id.rbPrivada);
        RadioButton rbPublica = (RadioButton)findViewById(R.id.rbPublica);
        if(rbPrivada.isChecked()) {
            return getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath()+"/";
        } else if (rbPublica.isChecked()) {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath()+"/";
        } else {
            return null;
        }
    }

    public boolean checkExtension(String extension) {
        // Método que comprueba que la extensión de la imagen esté entre las permitidas
        if(!extension.contains(getString(R.string.png))
                || !extension.contains(getString(R.string.jpg))
                || !extension.contains(getString(R.string.gif))){
            if(extension.length() < 5){
                return true;
            }
        }
        return false;
    }

    public void guardarImagen(String imageUrl, String destinationFile) throws IOException {
        // Método que descarga una imagen desde una Url
        // leyendo byte a byte
        // guardando en el destino especificado
        try {
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
        } catch (MalformedURLException e) {}
          catch (FileNotFoundException e) {}
    }
}