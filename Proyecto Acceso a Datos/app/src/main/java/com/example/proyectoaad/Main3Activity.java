package com.example.proyectoaad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main3Activity extends AppCompatActivity {

    private static final int NADA = -1;
    private static final int INTERNO = 0;
    private static final int  PUBLICO= 1;
    private static final int  PRIVADO = 2;
    private static final int PERMISO_LEER_ESCRIBIR = 10;
    private static final String NOMBRE_VARIABLE = "ultimoFichero";
    private static final String TIPO_VARIABLE = "tipoFichero";
    private static final String TAG = "xyzyx"+Main3Activity.class.getName();
    public static  final String ARCHIVOS= "";


    private RadioGroup rgOpcion;
    private Button btGuardar;
    private EditText etNombreArchivo;
    private TextView tvUltimoFichero;
    private String nombreFichero;
    private String contenido;
    private int tipo;


    private static File getPath(Context context, int tipo){
        File file = null;
        switch(tipo){
            case PRIVADO:
                file = context.getExternalFilesDir(null);
                break;
            case INTERNO:
                file = context.getFilesDir();
                break;
        }
        return file;
    }

    private static int getCheckedType(int item) {
        int tipo = NADA;

        switch (item){
            case R.id.rbPrivado:
                tipo = PRIVADO;
                break;
            case R.id.rbInterno:
                tipo = INTERNO;
                break;

        }

        return tipo;
    }

    private void selectRadio(int tipo) {

        switch (tipo){
            case PRIVADO:
                RadioButton rbPri = Main3Activity.this.findViewById(R.id.rbPrivado);
                rbPri.setChecked(true);
                break;
            case INTERNO:
                RadioButton rbPub = Main3Activity.this.findViewById(R.id.rbInterno);
                rbPub.setChecked(true);
                break;

        }

    }

    private void asignarEventos() {
        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Main3Activity.this.escribirArchivo();
                Intent intent = new Intent(Main3Activity.this, Main4Activity.class);
                intent.putExtra(ARCHIVOS, nombreFichero);
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

    private void escribirArchivo() {
        Intent intent = getIntent();
        contenido = intent.getStringExtra(MainActivity.MENSAJE).trim();
        if(isValues() && !contenido.isEmpty()){
            if(tipo == PUBLICO){
                checkPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.tituloLectura, R.string.mensajeExplicacionLectura, new AfterPermissionCheck() {
                    @Override
                    public void doTheJob() {
                        writeNotes();
                    }
                });
            }else{
                writeNotes();
            }
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
                ActivityCompat.requestPermissions(Main3Activity.this, new String[]{permission}, PERMISO_LEER_ESCRIBIR);
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

    //versión no estática de getPath
    private File getPath(int tipo){
        File file = null;
        switch(tipo){
            case PRIVADO:
                file = getExternalFilesDir(null);
                break;
            case INTERNO:
                file = getFilesDir();
                break;
        }
        return file;
    }

    private void initComponentes() {
        rgOpcion = findViewById(R.id.rgOpcion);
        btGuardar = findViewById(R.id.btGuardar);
        etNombreArchivo = findViewById(R.id.etNombreArchivo);
        tvUltimoFichero = findViewById(R.id.tvUltimoFichero);

        TextView tvFicheroAnterior = findViewById(R.id.tvFicheroUltimo);
        tvFicheroAnterior.setText(leerPreferences());

        if(prefUltimo()==true){
            tvFicheroAnterior.setVisibility(View.VISIBLE);
            tvUltimoFichero.setVisibility(View.VISIBLE);
        }else{
            tvFicheroAnterior.setVisibility(View.INVISIBLE);
            tvUltimoFichero.setVisibility(View.INVISIBLE);
        }

        int t = leerTipoPreferences();
        selectRadio(t);
        asignarEventos();

    }

    private boolean isValues() {
        nombreFichero = etNombreArchivo.getText().toString().trim()+prefExtension();
        tipo = Main3Activity.getCheckedType(rgOpcion.getCheckedRadioButtonId());
        return !(nombreFichero.isEmpty() || tipo == NADA);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        initComponentes();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) { //parametros: testigo, permisos que solicitas,
        // los permisos que me ha concedido y los que no en el mismo orden que el array anterior
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_LEER_ESCRIBIR){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                writeNotes();
            }
        }
    }

    private void writeNotes() {
        File f=new File( getPath(tipo),nombreFichero);
        Log.v(TAG, f.getAbsolutePath());//Es para ver one están los archivos
        try {
            FileWriter fw = new FileWriter(f);
            fw.write(contenido);
            fw.flush();
            fw.close();
            cambiarPreferences();
            EditText etNombre = findViewById(R.id.etNombreArchivo);
            etNombre.setText("");

            TextView tvFicheroAnterior = findViewById(R.id.tvFicheroUltimo);
            tvFicheroAnterior.setText(leerPreferences());


        }catch(IOException e){
        }
    }

    public void cambiarPreferences(){
        SharedPreferences sharedPref = Main3Activity.this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(NOMBRE_VARIABLE, nombreFichero);
        editor.putInt(TIPO_VARIABLE, tipo);
        editor.commit();
    }

    public String leerPreferences(){
        SharedPreferences sharedPref = Main3Activity.this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(NOMBRE_VARIABLE, "not found");
    }

    public int leerTipoPreferences(){
        SharedPreferences sharedPref = Main3Activity.this.getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(TIPO_VARIABLE, INTERNO);
    }

    public String prefExtension(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getString("reply","xxx");
    }

    public boolean prefUltimo(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean("sync",false);
    }


}
