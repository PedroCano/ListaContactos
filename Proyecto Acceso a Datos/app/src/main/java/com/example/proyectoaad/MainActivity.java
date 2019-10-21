package com.example.proyectoaad;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static  final  int ID_PERMISO_LEER_CONTACTOS = 1;
    public static  final String MENSAJE= "";
    public static final String ARCHIVOS_HEREDADOS="";
    private List<Contacto> contactos;
    private TextView tvContactos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.initContacto();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icono1:
                abreGuardar();
                return true;
            case R.id.icono2:
                abreOpciones();
                return true;
            case R.id.icono3:
                abreLeer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ID_PERMISO_LEER_CONTACTOS){
            //PackageManager.PERMISSION_DENIED;
            //PackageManager.PERMISSION_GRANTED;
            if(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                accion();
            }else{
                finish();
            }
        }
    }

    private void initContacto(){
        Button btContacto = findViewById(R.id.btVer);
        btContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerListaContactos();
            }
        });
        tvContactos = findViewById(R.id.tvContactos);
        tvContactos.setMovementMethod(new ScrollingMovementMethod());

    }

    private void accion(){
        contactos = getListaContactos();
        TextView tvLista = findViewById(R.id.tvContactos);
        tvLista.setText(contactos.toString().substring(1,contactos.toString().length()-1));
    }

    private void obtenerListaContactos(){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            accion();
        }else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)){ //2º y sucesivas veces que se piden los permisos
                    Toast.makeText(this, R.string.razon, Toast.LENGTH_LONG).show();
                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        ID_PERMISO_LEER_CONTACTOS);

            }else{
                accion();
            }

        }
    }

    public List<Contacto> getListaContactos(){

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ? and " +
                ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
        String argumentos[] = new String[]{"1","1"};
        String orden = ContactsContract.Contacts.DISPLAY_NAME + " collate localized asc";
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceId = cursor.getColumnIndex(ContactsContract.Contacts._ID);
        int indiceNombre = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        List<Contacto> lista = new ArrayList<>();
        Contacto contacto;
        while(cursor.moveToNext()){
            contacto = new Contacto();
            contacto.setId(cursor.getLong(indiceId));
            contacto.setNombre(cursor.getString(indiceNombre));
            contacto.setTelefonos(getListaTelefonos(contacto.getId()));
            /*contacto.setId(1).setNombre("2");*/
            lista.add(contacto);
        }
        return lista;
    }
    public List<String> getListaTelefonos(long id){
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String proyeccion[] = null;
        String seleccion = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String argumentos[] = new String[]{id+""};
        String orden = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        int indiceNumero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        List<String> lista = new ArrayList<>();
        String numero;
        while(cursor.moveToNext()){
            numero = cursor.getString(indiceNumero);
            lista.add(numero);
        }
        return lista;
    }

    public void abreOpciones(){
         Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
         startActivity(intent);
    }

    public void abreGuardar(){
        Intent intent = new Intent(getApplicationContext(), Main3Activity.class);
        intent.putExtra(MENSAJE, getListaContactos().toString());
        startActivity(intent);
    }

    public void abreLeer(){
        Intent intent = new Intent(getApplicationContext(), Main4Activity.class);
        intent.putExtra(MENSAJE, getListaContactos().toString());
        startActivity(intent);
    }

    private static class Contacto{

        private long id;
        private String nombre;
        private List<String> telefonos = new ArrayList<>();

        public long getId() {
            return id;
        }

        public Contacto setId(long id) {
            this.id = id;
            return this;
        }

        public String getNombre() {
            return nombre;
        }

        public Contacto setNombre(String nombre) {
            this.nombre = nombre;
            return this;
        }

        public List<String> getTelefonos() {
            return telefonos;
        }

        public void setTelefonos(List<String> telefonos) {
            this.telefonos = telefonos;
        }

        @Override
        public String toString() {
            return "Id = " + id +"\n"+
                    "Nombre = " + nombre +"\n" +
                    "Telefonos=" + telefonos +"\n"+"\n";

        }
    }
}
