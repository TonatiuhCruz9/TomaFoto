package com.example.tomafoto;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICTURE = 7;
    private static final int REQUEST_CODE_ASK_PERMISION = 3 ;
    ImageButton imgbCamara;
    ImageView imgFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgbCamara=findViewById(R.id.imgbCamara);
        imgFoto=findViewById(R.id.imgFoto);
        //mandar a llamar el metodo que hicimos para la camara cuando se haga un click
        imgbCamara.setOnClickListener(onClickCamara);
        imgFoto.setOnClickListener(imgFotoClick);
    }

    //para foto con menu
    View.OnClickListener imgFotoClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this,imgFoto);
            popupMenu.getMenuInflater().inflate(R.menu.popup_main,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(itemClickListener);
            popupMenu.show();
        }
    };

    //metodo de los click del menu para saber que itris metodos se lanzan
    PopupMenu.OnMenuItemClickListener itemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.popmGuarda:
                    guardarFoto();
                    break;
                case R.id.popmTomaFoto:

                    break;
            }
            return true;
        }
    };

    //recomendacion de google, no pedir demasiados permisos ya que daña la experiencia de usuario
    private void guardarFoto() {
        pedirPermisos();
        //pasar archivo a formato de imagen
        File imgFile=getOutputMediaFile();
        //casteo
        BitmapDrawable bitmapDrawable = (BitmapDrawable)imgFoto.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //el segundo es donde se va a guardar, calidad del 100%
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        //transformarlo a formato byte en un arreglo
        byte[] byteArray = stream.toByteArray();

        //ya para escribir, manejando posibles excepciones
        try {
            FileOutputStream fos = new FileOutputStream(imgFile);
            fos.write(byteArray);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mensaje al usuario para que vea donded se guardo
        Toast.makeText(this,"El archivo fue almacenado en: "+imgFile.getAbsoluteFile(),Toast.LENGTH_LONG).show();
    }

    //uri del enlace de tu propio equipo o externi¿o
    private  static Uri getOutMediaFileUri(){
        //para indicarle que va a usar un archivo
        return Uri.fromFile(getOutputMediaFile());
    }

    //metodo estatico que se puede usar sin importar el lugar, no debe declararse
    //por eso es estatico, esye metodo solo da formato del archivo mutlimedia, no gyarda
    private static File getOutputMediaFile() {
        //file es para metodos para modificar o guardar los archivos
        //crear objeto de tipo file y poder manipular el directorio donde esta el archivo, es decir, creacion de metadatos y
        //ubicacion de las fotos en la memoria SD u otro medio de almacenamiento
        //el segundo parameto es el nombre del directorio que creara
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "TomaFotoApp");

        //si es que no existe el directorio y lo creara
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                //mensaje para el programador
                Log.d("ErrorTMAP", "No se pudo crear el directorio.");
                return null;
            }
        }
        //Crear las propiedades del archivo de imagen
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        //el separetor es oara concatenar dependiendo el SO
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        return mediaFile;
    }

    private void pedirPermisos() {
        //verificando que el usuario en el manifiesto, dio permisos desde la instalacion de la app
        int permisoStorage = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permisoCamara=ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA);
        int permisoStorageRead=ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE);

        //if por si en la instalacion no se considieron los permisos, pedirlos nuevamente
        if(permisoStorage != PackageManager.PERMISSION_GRANTED && permisoCamara != PackageManager.PERMISSION_GRANTED && permisoStorageRead != PackageManager.PERMISSION_GRANTED){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions(new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISION);
            }
        }
    }

    View.OnClickListener onClickCamara = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InvocarCamara();
        }
    };

    private void InvocarCamara() {
        //sentencia para llamar a la aplicacion de la camara, sin esa app, esto no funcionaria
        Intent tomarImagenIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //validar la toma de foto
        if(tomarImagenIntent.resolveActivity(getPackageManager())!=null){
            //sus argumentos son el objeti creado y una constante final que nosotros definimos
            startActivityForResult(tomarImagenIntent, REQUEST_IMAGE_PICTURE);
        }
    }

    //metodo que pasa al recibir foto con el starActivity que usamos en invocar camara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //resuelt okay es una variable ya definida en java
        if(requestCode == REQUEST_IMAGE_PICTURE && resultCode == RESULT_OK){
            //bundle se usa para pasar datis de app de camara a la nuestra, es decir, la imagen
            Bundle extras = data.getExtras();
            Bitmap imagenbitmap = (Bitmap)extras.get("data");
            imgFoto.setImageBitmap(imagenbitmap);
        }
    }
}
