package com.example.proyectoaad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main4Activity extends AppCompatActivity {

    private static final int  PUBLICO= 1;
    private static final int PERMISO_LEER_ESCRIBIR = 10;
    private static final int NADA = -1;
    private static final String NOMBRE_VARIABLE = "ultimoFichero";
    private static final String TIPO_VARIABLE = "tipoFichero";

    TextView tvEleccion;
    Button btLeer;
    EditText etArchivoLectura;
    TextView tvContenidoLectura;
    private String nombreFichero;
    private int tipo;
    private String contenido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);
        initComponents();
    }

    private void initComponents() {
        tvEleccion = findViewById(R.id.tvEleccionArchivo);
        btLeer = findViewById(R.id.btLeer);
        etArchivoLectura = findViewById(R.id.etArchivoLectura);
        tvContenidoLectura = findViewById(R.id.tvContenidoLectura);

        asignarEventos();
        cambiarPreferences();

        tvContenidoLectura.setMovementMethod(new ScrollingMovementMethod());
        tvEleccion.setText(leerPreferences());
    }

    private void asignarEventos() {
        btLeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Main4Activity.this.leerArchivo();
                }catch (Exception e){
                    tvContenidoLectura.setText("No existe el archivo");
                }
            }
        });
    }

    private void checkPermissions(String permiso, int titulo, int mensaje, AfterPermissionCheck apc){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                explain(R.string.tituloLectura, R.string.mensajeExplicacionLectura, Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISO_LEER_ESCRIBIR);
            }
        } else {
            apc.doTheJob();
        }
    }

    private void explain(int title, int message, final String permission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.respSi, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ActivityCompat.requestPermissions(Main4Activity.this, new String[]{permission}, PERMISO_LEER_ESCRIBIR);
            }
        });
        builder.setNegativeButton(R.string.respNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    private boolean isValues() {
        nombreFichero = etArchivoLectura.getText().toString().trim();
        tipo = 0;
        return !(nombreFichero.isEmpty() || tipo == NADA);
    }

    private void leerArchivo() {
        Intent intent = getIntent();
        contenido = intent.getStringExtra(MainActivity.MENSAJE).trim();
        if(isValues() && !contenido.isEmpty()){
            if(tipo == PUBLICO){
                AfterPermissionCheck apc = new AfterPermissionCheck() {
                    @Override
                    public void doTheJob() {
                        readNotes();
                    }
                };
                checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.tituloLectura, R.string.mensajeExplicacionLectura, apc);
            }else{
                readNotes();
            }
        }
    }

    private void readNotes() {
        try {
            FileInputStream fileInputStream = getApplicationContext().openFileInput(nombreFichero);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String linea;

            while ((linea = bufferedReader.readLine())!= null){
                stringBuilder.append(linea).append("\n");
            }
            tvContenidoLectura.setText(stringBuilder.toString().substring(1,stringBuilder.toString().length()-1));

        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(IOException e){
        }
    }

    public void cambiarPreferences(){
        SharedPreferences sharedPref = Main4Activity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(NOMBRE_VARIABLE, nombreFichero);
        editor.putInt(TIPO_VARIABLE, tipo);
        editor.commit();
    }

    public String leerPreferences(){
        SharedPreferences sharedPref = Main4Activity.this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(NOMBRE_VARIABLE, "not found");
    }

}
