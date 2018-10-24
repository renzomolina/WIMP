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
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.whereismypet.whereismypet.R;



import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import Modelo.Usuario;
import finalClass.GeneralMethod;

import static android.widget.Toast.LENGTH_SHORT;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LoginActivity extends BaseActivity implements View.OnClickListener {


    private EditText mEmailEditText,mPasswordEditText;
    private String tipoDeLogin;

    //FIREBASE-----
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUserFireBase;
    public static final int RC_SIGN_IN = 1;
    //FACEBOOK--
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private List<String> permisosNecesariosFacebook = Arrays.asList("email","user_birthday","user_friends","public_profile");
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Auth-Database FIREBASE
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Inicializar las vistas
        CardView mIniciarCardView = findViewById(R.id.btnIniciarLogin);
        TextView mRegistroTextView = findViewById(R.id.tvRegistrarseLogin);
        TextView mOlvidoContrasenaTextView = findViewById(R.id.tvOlvidoContraseñaLogin);
        mEmailEditText = findViewById(R.id.CorreoLogin);
        mPasswordEditText = findViewById(R.id.PasswordLogin);
        CheckBox mRecordarUsuarioCheckBox = findViewById(R.id.RecordarSesion);
        loginButton = findViewById(R.id.login_button);
        //GOOGLE
        SignInButton mSignInButton = findViewById(R.id.sign_in_button);
        callbackManager = CallbackManager.Factory.create();

        //Botones
        mIniciarCardView.setOnClickListener(this);
        mRegistroTextView.setOnClickListener(this);
        mOlvidoContrasenaTextView.setOnClickListener(this);
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
                GeneralMethod.RegexLogin("correo",LoginActivity.this);
                GeneralMethod.RegexLogin("correovacio",LoginActivity.this);


            }
        });
        mPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                GeneralMethod.RegexLogin("contrasenavacio",LoginActivity.this);
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
                if (checkEditTextIsEmpty()) {
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

    //------------------------------------------------

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
        //Componentes Registro
        private CardView btnRegistro;
        private EditText mNombreRegistroEditText, mApellidoRegistroEditText, mEmailRegistroEditText, mPassRegistroEditText;
        private ImageView mImgPerfilDBRegistroImageView;
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
        private File fileImagen;
        private String pathTomarFoto;
        private Uri uriSeleccionarFoto;
        private Uri mFotoPerfilRegistro;

        //Firebase
        private FirebaseAuth mFirebaseAuthRegistro;
        private StorageReference mStorageReference;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_registro, null);

            //Firebase
            mStorageReference = FirebaseStorage.getInstance().getReference();
            mFirebaseAuthRegistro = FirebaseAuth.getInstance();


            //Vistas
            btnRegistro = content.findViewById(R.id.CheckIn);
            mNombreRegistroEditText = content.findViewById(R.id.nombreRegistro);
            mApellidoRegistroEditText = content.findViewById(R.id.apellidoRegistro);
            mEmailRegistroEditText = content.findViewById(R.id.emailRegistro);
            mPassRegistroEditText = content.findViewById(R.id.passRegistro);
            mImgPerfilDBRegistroImageView = content.findViewById(R.id.imgPerfilDBRegistro);
            mImgPerfilDBRegistroImageView.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeResource(getResources(),R.drawable.com_facebook_profile_picture_blank_square)));
            btnRegistro.setOnClickListener(this);
            mImgPerfilDBRegistroImageView.setOnClickListener(this);

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

            EscuchandoEstadoDeAutentificacion();
            return builder.create();
        }

        private void EscuchandoEstadoDeAutentificacion() {

            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    mUserFireBase = firebaseAuth.getCurrentUser();
                    if (mUserFireBase != null) {
                        dismiss();
                    }
                }
            };
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case COD_SELECCIONA:
                        uriSeleccionarFoto = Objects.requireNonNull(data).getData();
                        mImgPerfilDBRegistroImageView.setImageURI(uriSeleccionarFoto);
                        mFotoPerfilRegistro = uriSeleccionarFoto;
                        try {
                            mImgPerfilDBRegistroImageView.setImageBitmap(GeneralMethod.getBitmapClip(MediaStore.Images.Media.getBitmap(LoginActivity.this.getContentResolver(), uriSeleccionarFoto)));
                        } catch (IOException e) {
                            e.printStackTrace();
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
                        mImgPerfilDBRegistroImageView.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeFile(pathTomarFoto)));
                        mFotoPerfilRegistro = Uri.fromFile(new File(pathTomarFoto));
                        break;
                }
            } else {
                mImgPerfilDBRegistroImageView.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeResource(getResources(),R.drawable.com_facebook_profile_picture_blank_square)));
            }

        }

        // Click de boton registar y imagenview de imagenPerfil
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

        //METODO DE REGISTRO FIREBASE
        private void RegistrarUsuarioEmailPassword() {
            if (EditTextIsEmpty()) {
                final Usuario mUser = new Usuario(
                        mNombreRegistroEditText.getText().toString(),
                        mApellidoRegistroEditText.getText().toString(),
                        mEmailRegistroEditText.getText().toString().trim(),
                        mPassRegistroEditText.getText().toString());
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Registrando...");
                progressDialog.show();

                mFirebaseAuthRegistro.createUserWithEmailAndPassword(mUser.getEmail(), mUser.getContraseña())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    storageIMG(mUser);
                                    Toast.makeText(LoginActivity.this, "Registrado con exito", Toast.LENGTH_SHORT).show();
                                    final FirebaseUser firebaseUser = Objects.requireNonNull(task.getResult()).getUser();
                                    firebaseUser.sendEmailVerification();
                                    dismiss();
                                    InicioSesionCorrecto();

                                } else {
                                    Toast.makeText(LoginActivity.this, "Ocurrio un inconveniente al intentar registrar el email, o la contraseña no cumple los " +
                                                    "requisitos minimo, por favor, vuelva a intentarlo",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            } else {
                Toast.makeText(LoginActivity.this, "VACIOS", Toast.LENGTH_SHORT).show();
            }
        }

        public void storageIMG (final Usuario mUserParam) {

            final StorageReference mStorageImgPerfilUsuario = mStorageReference.child("Imagenes").child("Perfil").child(getRandomString());

            mStorageImgPerfilUsuario.putFile(mFotoPerfilRegistro).addOnSuccessListener(LoginActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference currentUserDB = mDatabase.child(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid());
                    currentUserDB.child("name").setValue(mUserParam.getNombre());
                    currentUserDB.child("last_name").setValue(mUserParam.getApellido());
                    currentUserDB.child("image").setValue(taskSnapshot.getStorage().getDownloadUrl().toString());

                }
            }).addOnFailureListener(LoginActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // METODOS COMPROBACION CAMPOS Y GENERANDO NOMBRE IMAGEN
        private boolean EditTextIsEmpty() {
            return (!TextUtils.isEmpty(mNombreRegistroEditText.getText().toString().trim()) &&
                    !TextUtils.isEmpty(mApellidoRegistroEditText.getText().toString().trim()) &&
                    !TextUtils.isEmpty(mPassRegistroEditText.getText().toString().trim()));
        }

        public String getRandomString() {
            SecureRandom random = new SecureRandom();
            return new BigInteger(130, random).toString(32);
        }

        //CAMARA O SELECCION DE IMAGEN
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
                            if(intent.resolveActivity(getPackageManager()) != null)
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

            }
        }

    }

    public void instanciarDialogRegistro() {
        RegistroDialog dialog = new RegistroDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "REGISTRO");// Mostramos el dialogo
    }
}