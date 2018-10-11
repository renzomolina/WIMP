package actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.whereismypet.whereismypet.BuildConfig;
import com.whereismypet.whereismypet.R;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import clases_estaticas.GeneralMethods;
import de.hdodenhof.circleimageview.CircleImageView;
import misclases.VolleySingleton;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class RegistroActivity {
/*
    //Componentes
    private CardView btnRegistrar;
    private EditText etNombre, etApellido, etCorreo, etContraseña, etConfimarContraseña;
    private String Nombre,Apellido, Correo, Contraseña, ConfirmacionContraseña;
    //Url Webservices
    private static final String URL_REGISTRO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Registro.php";
    private static final String URL_SUBIRFOTO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Upload.php";
    private static final String URL_CARPETAFOTO ="http://www.secsanluis.com.ar/servicios/varios/wimp/W_Imagenes/";
    private static String URL_CORREO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_ValidarCorreo.php";
    //Imagen
    private Bitmap bitmapImagenPerfil;
    private CircleImageView btnImagenPerfilCircular;
    private File fileImagen;
    private String pathTomarFoto;
    private Uri uriSeleccionarFoto;
    private String pathImageWebService;


    private boolean withImage = false;
    private int codigoOpcion = 0;
    //Url carpeta imagenes
    private static final String CARPETA_PRINCIPAL = "WIMP/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    //Validacion
    private boolean CheckEditText=false;
    private boolean Validacion = false;
    private static final String REGEX_LETRAS = "^[a-zA-ZáÁéÉíÍóÓúÚñÑüÜ\\s]+$";
    private static final String REGEX_EMAIL ="^[a-zA-Z0-9\\._-]+@[a-zA-Z0-9-]{2,}[.][a-zA-Z]{2,4}$";
    //Permisos
    private static final int MIS_PERMISOS = 100;
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    //Consulta
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        InicializarEditText();
        ValidarLogin();
    }

    @Override
    public void onClick(View view) {
        if (view == btnImagenPerfilCircular) {
            if (solicitaPermisosVersionesSuperiores()) {
                mostrarDialogOpciones();
            }
        }
    }
    // CAMARA

    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo=new AlertDialog.Builder(this);
        dialogo.setTitle("Permisos Desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
                }
            }
        });
        dialogo.show();
    }

    private void mostrarDialogOpciones() {
        final CharSequence[] opciones={"Tomar Foto","Elegir de Galeria","Cancelar"};
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Elige una Opción");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar Foto")){
                    abrirCamara();
                }else{
                    if (opciones[i].equals("Elegir de Galeria")){
                        Intent intent=new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        startActivityForResult(intent.createChooser(intent,"Seleccione"),COD_SELECCIONA);
                    }else{
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        builder.show();
    }

    private void abrirCamara() {
        File miFile=new File(Environment.getExternalStorageDirectory(),DIRECTORIO_IMAGEN);
        boolean isCreada=miFile.exists();

        if(!isCreada){
            isCreada=miFile.mkdirs();
        }

        if(isCreada){
            Long consecutivo= System.currentTimeMillis()/1000;
            String nombre=consecutivo.toString()+".jpg";

            pathTomarFoto=Environment.getExternalStorageDirectory()+File.separator+DIRECTORIO_IMAGEN
                    +File.separator+nombre;//indicamos la ruta de almacenamiento

            fileImagen=new File(pathTomarFoto);

            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N)
            {
                String authorities=this.getPackageName()+".provider";
                Uri imageUri= FileProvider.getUriForFile(this,authorities,fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else
            {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }
            startActivityForResult(intent,COD_FOTO);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case COD_SELECCIONA:
                if(data != null) {
                    //uriSeleccionarFoto = data.getData();
                    //btnImagenPerfilCircular.setImageURI(uriSeleccionarFoto);
                    try {
                        bitmapImagenPerfil = MediaStore.Images.Media.getBitmap(getContentResolver(),data.getData());
                        btnImagenPerfilCircular.setImageBitmap(bitmapImagenPerfil);
                        codigoOpcion = COD_SELECCIONA;
                        withImage = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case COD_FOTO:
                MediaScannerConnection.scanFile(this, new String[]{pathTomarFoto}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("Path",""+path);
                            }
                        });
                codigoOpcion = COD_FOTO;
                bitmapImagenPerfil= BitmapFactory.decodeFile(pathTomarFoto);
                btnImagenPerfilCircular.setImageBitmap(bitmapImagenPerfil);
                withImage = true;
                break;
        }

        if(bitmapImagenPerfil == null){
            withImage = false;
            btnImagenPerfilCircular.setImageDrawable(getResources().getDrawable(R.drawable.com_facebook_profile_picture_blank_square));
        }
        /*else{
            if(codigoOpcion != 0)
                enviarImagenDB(codigoOpcion);
        }
    }

    private void enviarImagenDB(int tipoOpcion){
        String nombreFotoServidor = String.valueOf(Calendar.getInstance().getTime()).replace(":","-").replace(" ","") + String.valueOf(Calendar.getInstance().getTimeInMillis());
        switch (tipoOpcion){
            case COD_SELECCIONA:{
                GeneralMethods.AndroidUploadService(getPath(uriSeleccionarFoto),nombreFotoServidor,RegistroActivity.this);
            }break;
            case COD_FOTO:{
                GeneralMethods.AndroidUploadService(pathTomarFoto,nombreFotoServidor,RegistroActivity.this);
            }break;
            default:break;
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    //PERMISOS
    private boolean solicitaPermisosVersionesSuperiores() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        if((this.checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&&this.checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
            return true;
        }
        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)||(shouldShowRequestPermissionRationale(CAMERA)))){
            cargarDialogoRecomendacion();
        }else{
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==MIS_PERMISOS){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){//el dos representa los 2 permisos
                Toast.makeText(getApplicationContext(),"Permisos aceptados",Toast.LENGTH_SHORT);
                mostrarDialogOpciones();
            }
        }else{
            solicitarPermisosManual();
        }
    }

    private void solicitarPermisosManual() {
        final CharSequence[] opciones={"si","no"};
        final AlertDialog.Builder alertOpciones=new AlertDialog.Builder(getApplicationContext());//estamos en fragment
        alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("si")){
                    Intent intent=new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri=Uri.fromParts("package",getApplicationContext().getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Los permisos no fueron aceptados",Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();
    }

    //VALIDACIONES
    private void InicializarEditText(){
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        //EditText
        etNombre = findViewById(R.id.nombre);
        etApellido = findViewById(R.id.apellido);
        etCorreo = findViewById(R.id.email);
        etContraseña = findViewById(R.id.pass);
        etConfimarContraseña = findViewById(R.id.confirmpass);
        //Botones
        btnImagenPerfilCircular = findViewById(R.id.imgPerfilDB);
        btnImagenPerfilCircular.setOnClickListener(this);
        btnRegistrar = findViewById(R.id.CheckIn);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckEditTextIsEmptyOrNot();
                if (CheckEditText && Validacion) {
                    ValidarCorreo();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Lo siento, pero todos los campos deben estar completos...", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Inializo el bitmap con una img por default
        bitmapImagenPerfil = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.com_facebook_profile_picture_blank_square);
    }

    private void CheckEditTextIsEmptyOrNot(){
        InicializarEditText();
        Nombre = etNombre.getText().toString().trim();
        Apellido = etApellido.getText().toString().trim();
        Correo = etCorreo.getText().toString().trim();
        Contraseña = etContraseña.getText().toString().trim();
        ConfirmacionContraseña = etConfimarContraseña.getText().toString().trim();

        if (TextUtils.isEmpty(Nombre) || TextUtils.isEmpty(Apellido) || TextUtils.isEmpty(Correo)
                || TextUtils.isEmpty(Contraseña) || TextUtils.isEmpty(ConfirmacionContraseña)) {
            CheckEditText = false;
        } else {
            CheckEditText = true;
        }
    }

    private boolean ValidarLogin() {
        etCorreo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {

                Regex("email");
            }
        });
        etNombre.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                Regex("nombre");
                Regex("nombrevacio");
            }
        });
        etApellido.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                Regex("apellido");
                Regex("apellidovacio");
            }
        });
        etContraseña.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                Regex("contraseñavacio");
            }
        });
        etConfimarContraseña.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                Regex("confirmacontraseña");
                Regex("confirmacontraseñavacio");
            }
        });

        return Validacion;
    }

    private void Regex(String edit) {
        Drawable msgerror = getResources().getDrawable(R.drawable.icon_error);
        msgerror.setBounds(0, 0, msgerror.getIntrinsicWidth(), msgerror.getIntrinsicHeight());
        switch (edit){
            case"apellido":{
                Pattern p = Pattern.compile(REGEX_LETRAS);
                if (!p.matcher(etApellido.getText().toString()).matches()) {
                    etApellido.setError("Este campo permite solo letras", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                } else {
                    btnRegistrar.setEnabled(true);
                    etApellido.setError(null);
                    Validacion = true;
                }
            }break;
            case"nombre":{
                Pattern p = Pattern.compile(REGEX_LETRAS);
                if (!p.matcher(etNombre.getText().toString()).matches()) {
                    etNombre.setError("Este campo permite solo letras", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                } else {
                    btnRegistrar.setEnabled(true);
                    etNombre.setError(null);
                    Validacion = true;
                }
            }break;
            case"email": {
                if (!Patterns.EMAIL_ADDRESS.matcher(etCorreo.getText().toString()).matches()) {
                    etCorreo.setError("Correo Invalido", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                } else {
                    btnRegistrar.setEnabled(true);
                    etCorreo.setError(null);
                    Validacion = true;
                }
            }break;
            case"nombrevacio": {
                InicializarEditText();
                Nombre = etNombre.getText().toString().trim();

                if (TextUtils.isEmpty(Nombre)){
                    etNombre.setError("Campo Vacio", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                }
                else {
                    btnRegistrar.setEnabled(true);
                    Validacion = true;
                }
            }break;
            case"apellidovacio": {
                InicializarEditText();
                Apellido = etApellido.getText().toString().trim();

                if (TextUtils.isEmpty(Apellido))
                {
                    etApellido.setError("Campo Vacio", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                }
                else {
                    btnRegistrar.setEnabled(true);
                    Validacion = true;
                }
            }break;
            case"correovacio": {
                InicializarEditText();
                Correo = etCorreo.getText().toString().trim();

                if(TextUtils.isEmpty(Correo))
                {
                    etCorreo.setError("Campo Vacio", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                }
                else {
                    btnRegistrar.setEnabled(true);
                    Validacion = true;
                }

            }break;
            case"contraseñavacio": {
                InicializarEditText();

                Contraseña = etContraseña.getText().toString().trim();

                if (TextUtils.isEmpty(Contraseña)) {
                    etContraseña.setError("Campo Vacio", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                } else {
                    btnRegistrar.setEnabled(true);
                    Validacion = true;
                }
            }break;
            case"confirmacontraseñavacio": {
                InicializarEditText();
                ConfirmacionContraseña = etConfimarContraseña.getText().toString().trim();

                if(TextUtils.isEmpty(ConfirmacionContraseña))
                {
                    etConfimarContraseña.setError("Campo Vacio", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                }

                else {
                    btnRegistrar.setEnabled(true);
                    Validacion = true;
                }
            }break;
            case"confirmacontraseña": {
                InicializarEditText();
                String pass=etContraseña.getText().toString();
                String ver=etConfimarContraseña.getText().toString();
                if(pass.equals(ver))
                {
                    Validacion = true;
                    btnRegistrar.setEnabled(true);
                }

                else {
                    etConfimarContraseña.setError("Debe coincidir con Contraseña", msgerror);
                    Validacion = false;
                    btnRegistrar.setEnabled(false);
                }
            }break;

        }

    }

    private void ValidarCorreo() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Validando correo...");
        progressDialog.show();

        URL_CORREO = URL_CORREO + "?correo=" + etCorreo.getText();
        URL_CORREO  = URL_CORREO.replace(" ","%20");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_CORREO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if (ServerResponse.equalsIgnoreCase("OK")) {
                            Registro();
                        } else {
                            Toast.makeText(getApplicationContext(), "Lo siento, pero el correo ya se encuentra registrado", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("nombre", Nombre);
                params.put("apellido", Apellido);
                params.put("correo", Correo);
                params.put("pass", Contraseña);
                params.put("imagen", pathImageWebService);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(this).addToRequestQueue(stringRequest);
    }

    //Realizar registro(webservice)
    public void Registro(){
        if(!withImage) {
            pathImageWebService = URL_CARPETAFOTO + "default.jpg";
        }
        else {
            enviarImagenDB(codigoOpcion);
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");
        progressDialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTRO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if(ServerResponse.equalsIgnoreCase("OK")) {
                            Toast.makeText(getApplicationContext(), "Usuario registrado exitosamente!!!", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error al registrar, por favor, vuelve a intentar.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<>();
                params.put("nombre",Nombre);
                params.put("apellido", Apellido);
                params.put("correo",Correo);
                params.put("pass",Contraseña);
                params.put("imagen", pathImageWebService);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(this).addToRequestQueue(stringRequest);

    }*/
}
