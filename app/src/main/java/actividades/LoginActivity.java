package actividades;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
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
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.whereismypet.whereismypet.R;



import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import misclases.Usuario;

import static android.widget.Toast.LENGTH_SHORT;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LoginActivity extends BaseActivity implements View.OnClickListener {


    private CheckBox mRecordarUsuarioCheckBox;
    private TextView mRegistroTextView,mOlvidoContraseñaTextView;
    private CardView mIniciarCardView,mRestablecerCardView;
    private EditText mEmailEditText,mPasswordEditText;
    private boolean CheckEditText, Validacion=true;
    private String tipoDeLogin;
    private  Usuario user;

    //FIREBASE-----
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUserFireBase;
    public static final int RC_SIGN_IN = 1;
    //FACEBOOK--
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private List<String> permisosNecesariosFacebook = Arrays.asList("email","user_birthday","user_friends","public_profile");
    //GOOGLE
    private SignInButton mSignInButton;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Auth-Database FIREBASE
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Inicializar las vistas
        mIniciarCardView = findViewById(R.id.btnIniciarLogin);
        mRegistroTextView = findViewById(R.id.tvRegistrarseLogin);
        mOlvidoContraseñaTextView = findViewById(R.id.tvOlvidoContraseñaLogin);
        mEmailEditText = findViewById(R.id.Correo);
        mPasswordEditText = findViewById(R.id.Password);
        mRecordarUsuarioCheckBox = findViewById(R.id.RecordarSesion);
        loginButton = findViewById(R.id.login_button);
        mSignInButton = findViewById(R.id.sign_in_button);
        callbackManager = CallbackManager.Factory.create();

        //Botones
        mIniciarCardView.setOnClickListener(this);
        mRegistroTextView.setOnClickListener(this);
        mOlvidoContraseñaTextView.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);

        //Metodos de Login
        LoginGoogle();
        EscuchandoEstadoDeAutentificacion();

        ///KeyHash();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener!=null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        ValidarLogin();
    }


    private void EscuchandoEstadoDeAutentificacion(){

        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUserFireBase = firebaseAuth.getCurrentUser();
                if(AccessToken.getCurrentAccessToken()!=null){
                    Toast.makeText(LoginActivity.this,"Faceboook",LENGTH_SHORT).show();
                }

                /*if(mUserFireBase!=null)
                {
                    user = new Usuario();
                    //user.setNombre(userFireBase.getDisplayName());
                    user.setEmail(mUserFireBase.getEmail());
                }*/
            }
        };
    }

    private void KeyHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.whereismypet.whereismypet", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }
    }

    public void InicioSesionCorrecto() {
        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }





    private void ValidarLogin(){
        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if(!Patterns.EMAIL_ADDRESS.matcher(mEmailEditText.getText().toString()).matches()) {
                    Drawable msgerror = getResources().getDrawable(R.drawable.icon_error);
                    msgerror.setBounds(0, 0, msgerror.getIntrinsicWidth(), msgerror.getIntrinsicHeight());
                    mEmailEditText.setError("Correo Invalido",msgerror);
                    Validacion = false;
                }
                else {
                    mEmailEditText.setError(null);
                    Validacion = true;
                }
            }
        });
    }

    public boolean checkEditTextIsEmpty(){
        return !TextUtils.isEmpty(mEmailEditText.getText().toString().trim()) && !TextUtils.isEmpty(mPasswordEditText.getText().toString().trim());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnIniciarLogin: {
                if (Validacion && checkEditTextIsEmpty()) {
                    tipoDeLogin="EmailPassword";
                    LoginEmailPassword();
                }
                else
                    Toast.makeText(LoginActivity.this, "Todos los campos deben estar completos, por favor ingrese los datos que se le solicitan", LENGTH_SHORT).show();
            }
            break;
            case R.id.tvRegistrarseLogin: {
                instanciarDialogRegistro();
            }
            break;
            case R.id.login_button: {
                tipoDeLogin="Facebook";
                LoginFacebook();
            }break;
            case R.id.sign_in_button: {
                tipoDeLogin="Google";
                signIn();
            }break;
            default:break;
        }
    }

    //---------------------------------------FIREBASE EMAIL---------------------------------------------------
    private void LoginEmailPassword() {
        mUserFireBase = mFirebaseAuth.getCurrentUser();
        if (mUserFireBase != null) {
            mUserFireBase.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (mUserFireBase.isEmailVerified()) {
                        final String mEmailStringEditTextLogin = mEmailEditText.getText().toString().trim(),
                                mPasswordStringEditTextLogin = mPasswordEditText.getText().toString();
                        mFirebaseAuth.signInWithEmailAndPassword(mEmailStringEditTextLogin, mPasswordStringEditTextLogin)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Iniciar", LENGTH_SHORT).show();
                                            InicioSesionCorrecto();
                                        } else
                                            Toast.makeText(LoginActivity.this, "Lo siento, pero este correo no esta verificado, ingrese a su correo y verifique su cuenta", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else
                        Toast.makeText(LoginActivity.this, "El correo no se encuentra verificado, por favor vea su bandeja de entrada y verifique el correo para validar la cuenta", Toast.LENGTH_LONG).show();
                }
            });
        } else
            Toast.makeText(LoginActivity.this, "El correo ingresado no existe, registre dicho correo, o inicie sesion con las redes sociales opcionales", Toast.LENGTH_LONG).show();
    }


    //---------------------------------------GOOGLE------------------------------------------------------------
    private void LoginGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            InicioSesionCorrecto();
                        } /*else {
                            // If sign in fails, display a message to the user.
                            //---->Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        } */
                    }
                });
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mFirebaseAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mFirebaseAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    //---------------------------------------FACEBOOK----------------------------------------------------------
    private  void LoginFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this, permisosNecesariosFacebook);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this,error.getMessage(),LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token){
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            InicioSesionCorrecto();
                        }
                        else
                            Toast.makeText(LoginActivity.this,R.string.auth_failed,LENGTH_SHORT).show();

                    }
                });
    }
    //---------------------------------------------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (tipoDeLogin){
            case "Facebook":{
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }break;
            case "Google":{
                if (requestCode == RC_SIGN_IN) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        // Google Sign In was successful, authenticate with Firebase
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        assert (account) != null;
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException ignored) { }
                }
            }break;
            default:break;
        }


    }

    //--------------------------DIALOGO REGISTRO---------------------------------------------------------------
    @SuppressLint("ValidFragment")
    private class RegistroDialog extends DialogFragment implements View.OnClickListener {
        private CardView btnRegistro;
        private EditText mNombreRegistroEditText, mApellidoRegistroEditText, mEmailRegistroEditText, mPassRegistroEditText;
        private ImageView mImgPerfilDBRegistro;
        private FirebaseAuth mFirebaseAuthRegistro;
        private ProgressDialog progressDialog;

        //Permisos
        private static final int MIS_PERMISOS = 100;
        private static final int COD_SELECCIONA = 10;
        private static final int COD_FOTO = 20;
        //Url carpeta imagenes
        private static final String CARPETA_PRINCIPAL = "WIMP/";//directorio principal
        private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
        private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
        //Imagen
        private Bitmap bitmapImagenPerfil;
        private File fileImagen;
        private String pathTomarFoto;
        private Uri uriSeleccionarFoto;
        private boolean withImage = false;
        private int  uploadStatus = 0;
        private Uri mIMGUPLOAD;
        private StorageReference mStorageReference;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_registro, null);

            mStorageReference=FirebaseStorage.getInstance().getReference();

            btnRegistro = content.findViewById(R.id.CheckIn);
            mNombreRegistroEditText = content.findViewById(R.id.nombreRegistro);
            mApellidoRegistroEditText = content.findViewById(R.id.apellidoRegistro);
            mEmailRegistroEditText = content.findViewById(R.id.emailRegistro);
            mPassRegistroEditText = content.findViewById(R.id.passRegistro);
            mImgPerfilDBRegistro = content.findViewById(R.id.imgPerfilDBRegistro);
            btnRegistro.setOnClickListener(this);
            mImgPerfilDBRegistro.setOnClickListener(this);
            mFirebaseAuthRegistro = FirebaseAuth.getInstance();
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismiss();
                    }
                    return false;
                }
            });

            return builder.create();
        }
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case COD_SELECCIONA:
                    if (data != null) {
                        uriSeleccionarFoto = data.getData();
                        mImgPerfilDBRegistro.setImageURI(uriSeleccionarFoto);
                        mIMGUPLOAD = uriSeleccionarFoto;
                        try {
                            bitmapImagenPerfil = MediaStore.Images.Media.getBitmap(LoginActivity.this.getContentResolver(), uriSeleccionarFoto);
                            mImgPerfilDBRegistro.setImageBitmap(bitmapImagenPerfil);
                            withImage = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case COD_FOTO:
                    MediaScannerConnection.scanFile(LoginActivity.this, new String[]{pathTomarFoto}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Path", "" + path);
                                }
                            });
                    bitmapImagenPerfil = BitmapFactory.decodeFile(pathTomarFoto);
                    mImgPerfilDBRegistro.setImageBitmap(bitmapImagenPerfil);
                    mIMGUPLOAD = Uri.fromFile(new File(pathTomarFoto));
                    withImage = true;
                    break;
            }
            if(bitmapImagenPerfil == null){
                withImage = false;
                mImgPerfilDBRegistro.setImageDrawable(getResources().getDrawable(R.drawable.com_facebook_profile_picture_blank_square));
            }

        }
        private void RegistrarUsuarioEmailPassword() {
            if (EditTextIsEmpty()) {
                final String
                        mNombreString = mNombreRegistroEditText.getText().toString(),
                        mApellidoString = mApellidoRegistroEditText.getText().toString(),
                        mEmailString = mEmailRegistroEditText.getText().toString().trim(),
                        mPassString = mPassRegistroEditText.getText().toString();
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Registrando...");
                progressDialog.show();

                mFirebaseAuthRegistro.createUserWithEmailAndPassword(mEmailString, mPassString)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {

                                    final FirebaseUser firebaseUser = Objects.requireNonNull(task.getResult()).getUser();
                                    firebaseUser.sendEmailVerification();
                                    storageIMG();
                                    dismiss();
                                    Toast.makeText(LoginActivity.this, "Registrado con exito", Toast.LENGTH_SHORT).show();

                                } else {
                                    //userDelete
                                    Toast.makeText(LoginActivity.this, "Ocurrio un inconveniente al intentar registrar el email, por favor, vuelva a intentarlo",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            } else {
                Toast.makeText(LoginActivity.this, "VACIOS", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean EditTextIsEmpty() {
            return (!TextUtils.isEmpty(mNombreRegistroEditText.getText().toString().trim()) &&
                    !TextUtils.isEmpty(mApellidoRegistroEditText.getText().toString().trim()) &&
                    !TextUtils.isEmpty(mPassRegistroEditText.getText().toString().trim()));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.CheckIn: {
                    RegistrarUsuarioEmailPassword();
                }
                break;
                case R.id.imgPerfilDBRegistro: {
                    if (solicitaPermisosVersionesSuperiores()) {
                        mostrarDialogOpciones();
                    }
                }
                break;
            }
        }

        public void storageIMG () {
            String mIMGNombre;
            if (!withImage) {
                uploadStatus = 0;
            } else {

                mIMGNombre = "Imagenes/Perfil/" + mIMGUPLOAD.getLastPathSegment();
                UploadTask mUploadTask = mStorageReference.child(mIMGNombre).putFile(mIMGUPLOAD);
                mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "SUBIO", Toast.LENGTH_SHORT).show();
                        mStorageReference.getDownloadUrl();
                        uploadStatus = 1;
                    }
                });
                mUploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "NO SUBIO", Toast.LENGTH_SHORT).show();
                        uploadStatus = 2;
                    }
                });
            }
        }

        private void mostrarDialogOpciones() {
            final CharSequence[] opciones={"Tomar Foto","Elegir de Galeria","Cancelar"};
            final AlertDialog.Builder builder=new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Elige una Opción");
            builder.setItems(opciones, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (opciones[i].equals("Tomar Foto")){
                        abrirCamara();
                    }else{
                        if (opciones[i].equals("Elegir de Galeria")){
                            Intent intent=new Intent(Intent.ACTION_PICK);
                            intent.setType("image/");
                            startActivityForResult(Intent.createChooser(intent,"Seleccione"),COD_SELECCIONA);
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
                    String authorities=LoginActivity.this.getPackageName()+".provider";
                    Uri imageUri= FileProvider.getUriForFile(LoginActivity.this,authorities,fileImagen);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                }else
                {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
                }
                startActivityForResult(intent,COD_FOTO);
            }

        }
        private void cargarDialogoRecomendacion() {
            AlertDialog.Builder dialogo=new AlertDialog.Builder(LoginActivity.this);
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
        //PERMISOS
        private boolean solicitaPermisosVersionesSuperiores() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                return true;
            }

            if((LoginActivity.this.checkSelfPermission(WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED)&& LoginActivity.this.checkSelfPermission(CAMERA)==PackageManager.PERMISSION_GRANTED){
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
                    Toast.makeText(getApplicationContext(),"Permisos aceptados",Toast.LENGTH_SHORT).show();
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
    }

    public void instanciarDialogRegistro() {
        RegistroDialog dialog = new RegistroDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "REGISTRO");// Mostramos el dialogo
    }
}