package clases_estaticas;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.whereismypet.whereismypet.R;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Pattern;

import actividades.MainActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.CONTROL_LOCATION_UPDATES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public final class GeneralMethods {
    //-----------------------------------COMPONENTES--------------------------------------------------------------------
    private static EditText etNombre, etApellido, etCorreo, etContraseña, etConfimarContraseña;
    private static CardView btnRegistrar;
    //-----------------------------------URL WEB SERVICE----------------------------------------------------------------
    private static final String URL_SUBIRFOTO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Upload.php";
    private static final String URL_CARPETAFOTO ="http://www.secsanluis.com.ar/servicios/varios/wimp/W_Imagenes/";
    //-----------------------------------VALIDACIONES REGEX-------------------------------------------------------------
    private static final String REGEX_LETRAS = "^[a-zA-ZáÁéÉíÍóÓúÚñÑüÜ\\s]+$";

    //-----------------------------------COD PERMISOS-------------------------------------------------------------------
    private static final int MIS_PERMISOS = 100;
    //-----------------------------------IMAGEN--------------------------------------------------------------------------
    private String pathImageWebService;
    public static File fileImagen;
    public static String pathTomarFoto;
    //---------------------------------Url carpeta imagenes--------------------------------------------------------------
    private static final String CARPETA_PRINCIPAL = "WIMP/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    //--------------------------------------PREFERENCIAS----------------------------------------------------------------
    public static String getFromSharedPreferences(String key,Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Mis preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }

    public static void savedLoginSharedPreferencesFB(String token, String userID, String FB,Context context){
        SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("userID", userID);
        editor.putString("facebook",FB);
        editor.apply();
    }

    public static void savedLoginSharedPreferencesDB(String correo, String DB, boolean remember,Context context){
        SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("correo", correo);
        editor.putString("base", DB);
        editor.putBoolean("rememberUser",remember);
        editor.apply();
    }

    public static Boolean getFromSharedPreferencesDB(String key,Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key,false);
    }
    //-----------------------------------------------------------------------------------------------------------------
    public static void InicioSesionCorrecto(Context context,Activity activity) {
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(context,i,new Bundle());
        activity.startActivity(i);
        activity.finish();
    }

    public static  void Picasso(Context context, String imgPerfil, CircleImageView circleImageView){
        File fileImage = new File(imgPerfil);
        Picasso.with(context)
                .load(fileImage)
                .placeholder(R.drawable.com_facebook_profile_picture_blank_square)
                .error(R.drawable.com_facebook_profile_picture_blank_square)
                .resize(400, 400)
                .fit()
                .centerInside()
                .into(circleImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });
    }

    private static void InicializarEditText(Activity activity){
        //EditText
        etNombre = activity.findViewById(R.id.nombre);
        etApellido = activity.findViewById(R.id.apellido);
        etCorreo = activity.findViewById(R.id.email);
        etContraseña = activity.findViewById(R.id.pass);
        etConfimarContraseña = activity.findViewById(R.id.confirmpass);
        // Botones
        btnRegistrar = activity.findViewById(R.id.CheckIn);
    }
    //--------------------------------------CAMARA-----------------------------------------------------------------------

    private static void cargarDialogoRecomendacion(Context context, final Activity activity) {
        AlertDialog.Builder dialogo=new AlertDialog.Builder(context);
        dialogo.setTitle("Permisos Desactivados");
        dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

        dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);
                }
            }
        });
        dialogo.show();
    }

    public static void mostrarDialogOpciones(final Context context, final Activity activity) {
        final CharSequence[] opciones={"Tomar Foto","Elegir de Galeria","Cancelar"};
        final AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Elige una Opción");
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals("Tomar Foto")){
                    abrirCamara(context,activity);
                }else{
                    if (opciones[i].equals("Elegir de Galeria")){
                        Intent intent=new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/");
                        activity.startActivityForResult(intent.createChooser(intent,"Seleccione"),10);
                    }else{
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        builder.show();
    }

    private static void abrirCamara(Context context,Activity activity) {
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
                String authorities=context.getPackageName()+".provider";
                Uri imageUri= FileProvider.getUriForFile(context,authorities,fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            }else
            {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }
            activity.startActivityForResult(intent,10);
        }

    }


    //------------------------------------------------------------------------------------------------------------------
    public static String AndroidUploadService(final String pathX, String nombreFotoServidor,Context context){
        String uploadId,
                extension = pathX.substring(pathX.indexOf('.'),pathX.length());
        try {
            uploadId = UUID.randomUUID().toString();
            nombreFotoServidor = String.valueOf(Calendar.getInstance().getTime()).replace(":","-").replace(" ","") + String.valueOf(Calendar.getInstance().getTimeInMillis());
            new MultipartUploadRequest(context, uploadId, URL_SUBIRFOTO)
                    .addFileToUpload(pathX, "picture")
                    .addParameter("filename", nombreFotoServidor)
                    .setNotificationConfig(new UploadNotificationConfig())
                    .setMaxRetries(2)
                    .setDelegate(new UploadStatusDelegate() {
                        @Override
                        public void onProgress(Context context, UploadInfo uploadInfo) { }
                        @Override
                        public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) { }
                        @Override
                        public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                            //ELiminar imagen
                            File eliminar = new File(pathX);
                            if (eliminar.exists()) {
                                if (eliminar.delete()) {
                                    System.out.println("archivo eliminado:" + pathX);
                                } else {
                                    System.out.println("archivo no eliminado" + pathX);
                                }
                            }
                        }
                        @Override
                        public void onCancelled(Context context, UploadInfo uploadInfo) { }
                    }).startUpload();

        } catch (Exception exc) {
            System.out.println(exc.getMessage()+" "+exc.getLocalizedMessage());
        }
        return URL_CARPETAFOTO + nombreFotoServidor + extension;
    }

    //---------------------------------------VALIDACIONES DE COMPONENTES------------------------------------------------
    public static void Regex(String edit,Context context,Activity activity) {
        Drawable msgerror = context.getResources().getDrawable(R.drawable.icon_error);
        msgerror.setBounds(0, 0, msgerror.getIntrinsicWidth(), msgerror.getIntrinsicHeight());
        InicializarEditText(activity);
        switch (edit){
            case"apellido":{
                Pattern p = Pattern.compile(REGEX_LETRAS);
                if (!p.matcher(etApellido.getText().toString()).matches()) {
                    etApellido.setError("Este campo permite solo letras", msgerror);

                    btnRegistrar.setEnabled(false);
                } else {
                    btnRegistrar.setEnabled(true);
                    etApellido.setError(null);
                }
            }break;
            case"nombre":{
                Pattern p = Pattern.compile(REGEX_LETRAS);
                if (!p.matcher(etNombre.getText().toString()).matches()) {
                    etNombre.setError("Este campo permite solo letras", msgerror);
                    btnRegistrar.setEnabled(false);
                } else {
                    btnRegistrar.setEnabled(true);
                    etNombre.setError(null);

                }
            }break;
            case"email": {
                if (!Patterns.EMAIL_ADDRESS.matcher(etCorreo.getText().toString()).matches()) {
                    etCorreo.setError("Correo Invalido", msgerror);

                    btnRegistrar.setEnabled(false);
                } else {
                    btnRegistrar.setEnabled(true);
                    etCorreo.setError(null);
                }
            }break;
            case"nombrevacio": {
                if (CheckEditTextIsEmptyOrNot(etNombre)){
                    etNombre.setError("Campo Vacio", msgerror);
                    btnRegistrar.setEnabled(false);
                }
                else {
                    btnRegistrar.setEnabled(true);
                }
            }break;
            case"apellidovacio": {

                if (CheckEditTextIsEmptyOrNot(etApellido))
                {
                    etApellido.setError("Campo Vacio", msgerror);
                    btnRegistrar.setEnabled(false);
                }
                else {
                    btnRegistrar.setEnabled(true);
                }
            }break;
            case"correovacio": {

                if(CheckEditTextIsEmptyOrNot(etCorreo))
                {
                    etCorreo.setError("Campo Vacio", msgerror);

                    btnRegistrar.setEnabled(false);
                }
                else {
                    btnRegistrar.setEnabled(true);

                }

            }break;
            case"contraseñavacio": {

                if (CheckEditTextIsEmptyOrNot(etContraseña)) {
                    etContraseña.setError("Campo Vacio", msgerror);
                    btnRegistrar.setEnabled(false);
                } else {
                    btnRegistrar.setEnabled(true);
                }
            }break;
            case"confirmacontraseñavacio": {
                if(CheckEditTextIsEmptyOrNot(etConfimarContraseña))
                {
                    etConfimarContraseña.setError("Campo Vacio", msgerror);
                    btnRegistrar.setEnabled(false);
                }

                else {
                    btnRegistrar.setEnabled(true);

                }
            }break;
            case"confirmacontraseña": {
                if(etContraseña.getText().toString().equals(etConfimarContraseña.getText().toString()))
                {
                    btnRegistrar.setEnabled(true);
                }
                else {
                    etConfimarContraseña.setError("Debe coincidir con Contraseña", msgerror);
                    btnRegistrar.setEnabled(false);
                }
            }break;

        }

    }

    public static boolean CheckEditTextIsEmptyOrNot(EditText editText){
        if(TextUtils.isEmpty(editText.getText().toString().trim()))
            return false;
        return true;
    }

    //--------------------------------------- PERMISOS --------------------------------------------------------------
    public static boolean solicitaPermisosVersionesSuperiores(Context context,Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return true;
        }

        if((context.checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) && context.checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
            return true;
        }
        if ((activity.shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)||(activity.shouldShowRequestPermissionRationale(CAMERA)))){
            cargarDialogoRecomendacion(context,activity);
        }else{
            activity.requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;
    }


}
