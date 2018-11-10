package actividades;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.whereismypet.whereismypet.R;



import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import Modelo.PreferenciasLogin;
import Modelo.Usuario;
import finalClass.GeneralMethod;

import static android.widget.Toast.LENGTH_SHORT;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {


    private EditText mEmailEditTextLogin, mPasswordEditTextLogin;
    private String tipoDeLogin;
    private ConstraintLayout mContainerLogin;
    CheckBox mRecordarUsuarioCheckBox;
    //FIREBASE-----
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mUserFireBase;
    public static final int RC_SIGN_IN = 1;

    //FACEBOOK--
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private List<String> permisosNecesariosFacebook = Arrays.asList("email", "user_birthday", "user_friends", "public_profile");
    //GOOGLE------
    private GoogleSignInClient mGoogleSignInClient;
    //IMAGEN REGISTRO
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    //Imagen
    private String tipoDeFoto = "VACIO";
    private Uri mFotoPerfilRegistro;
    private ImageView mImgPerfilDBRegistroImageView;
    private final String defaultUser = "https://firebasestorage.googleapis.com/v0/b/wimp-219219.appspot.com/o/Imagenes%2FPerfil%2FdefaultUser.jpg?alt=media&token=0651674e-50a9-45f6-990e-f36e3928fe98";
    //STRING DE LOGUEO
    final String mFacebook = "facebook.com";
    final String mGoogle = "google.com";
    final String mPassword = "password";
    String mEstado = "logueo";
    //Preferemcias

    SharedPreferences sharedPreferences;

    //----------------------------------------CICLOS DE VIDA DE ACTIVITY-------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Auth-Database FIREBASE
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserFireBase = mFirebaseAuth.getCurrentUser();
        //Metodos de Login
        LoginGoogle();
        EscuchandoEstadoDeAutenticacion();

        //Inicializar las vistas
        final CardView mIniciarCardView = findViewById(R.id.btnIniciarLogin);
        final TextView mRegistroTextView = findViewById(R.id.tvRegistrarseLogin);
        final TextView mOlvidoContrasenaTextView = findViewById(R.id.tvOlvidoContraseñaLogin);
        mRecordarUsuarioCheckBox = findViewById(R.id.RecordarSesion);
        mContainerLogin = findViewById(R.id.ContainerLogin);
        mEmailEditTextLogin = findViewById(R.id.CorreoLogin);
        mPasswordEditTextLogin = findViewById(R.id.PasswordLogin);
        loginButton = findViewById(R.id.login_button);
        mRecordarUsuarioCheckBox.setOnClickListener(this);

        //mRecordarUsuarioCheckBox.setChecked(LecturaDeTipoLogin().isRecordarUsuario());

        //GOOGLE
        SignInButton mSignInButton = findViewById(R.id.sign_in_button);
        callbackManager = CallbackManager.Factory.create();

        //Botones
        mIniciarCardView.setOnClickListener(this);
        mRegistroTextView.setOnClickListener(this);
        mOlvidoContrasenaTextView.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        mSignInButton.setOnClickListener(this);

        //KeyHash();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }



    @Override
    protected void onResume() {

        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        ValidarLogin();
        LimpiarEditText();

        /*mEmailEditText.addTextChangedListener(new GeneralMethod.addListenerOnTextChange(this,mEmailEditText,string tipo));
        mPasswordEditText.addTextChangedListener(new GeneralMethod.addListenerOnTextChange(this,mPasswordEditText));*/
    }

    //--------------------------------------ESCUCHADOR DE AUTENTICACION, POR SI CAMBIA DE LOGUEO------------------------------------
    private void EscuchandoEstadoDeAutenticacion() {

        mAuthStateListener = firebaseAuth -> {
            mUserFireBase = firebaseAuth.getCurrentUser();
            if (mUserFireBase != null && LecturaDeTipoLogin().getTipoSignOut().equals("default")) {
               InicioSesionCorrecto();
            }
            /*else
                GeneralMethod.showSnackback("El correo no se encuentra verificado, por favor verifique el correo",mContainerLogin,this);*/
        };
    }
    //-------------------------------------------PREFERENCIAS------------------------------------------------------------------
    private PreferenciasLogin LecturaDeTipoLogin(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return new PreferenciasLogin().setTipoSignOut(sharedPreferences.getString("type_sign_out", "default"))
                .setRecordarUsuario(sharedPreferences.getBoolean("remember", true))
                .setTipoSignIn(sharedPreferences.getString("type_sign_in", "default"));

    }
    private PreferenciasLogin LoadRememver (){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return new PreferenciasLogin().setRecordarUsuario(sharedPreferences.getBoolean("remember", true));
    }

    private void GuardarRemember(final boolean remember){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("remember",remember);
        editor.apply();
    }

    //-------------------------------------------OBTENER HASH DEL PROYECTO PARA IDENTIFICARLO----------------------------------
    private void KeyHash() {
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
    //-------------------------------------------METODO DE CAMBIO DE ACTIVITY A LA PRINCIPAL----------------------------------
    public void InicioSesionCorrecto() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
    //-------------------------------------------METODOS QUE VACIO LOS MENSAJES Y DATOS DE LOS COMPONENTES Y OTRO LLAMO A LAS VALIDACIONES CUANDO CAMBIA EL TEXT DEL EDITTEXT----------------------------------
    private void LimpiarEditText() {
        mEmailEditTextLogin.setText("");
        mPasswordEditTextLogin.setText("");
        mEmailEditTextLogin.setError(null);
        mPasswordEditTextLogin.setError(null);
    }

    private void ValidarLogin() {
        /* mEmailEditTextLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                GeneralMethod.RegexLogin("correo", LoginActivity.this);
                GeneralMethod.RegexLogin("correovacio", LoginActivity.this);


            }
        });
        mPasswordEditTextLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                GeneralMethod.RegexLogin("contrasenavacio", LoginActivity.this);
            }
        });
        */
    }



    //---------------------------------------FIREBASE EMAIL---------------------------------------------------
    private void LoginEmailPassword() {
        final String mMsgShowSnackBarVerificado = "El correo no se encuentra verificado, por favor verifique el correo",
                mMsgShowSnackBarEmailPassword = "Usuario o Contraseña incorrecto, por favor vuelva a ingresarlos.!",
                mMsgShowSnackBarCurrentUser = "Cuenta no registrada, registra la cuenta o elija alguna de las opciones";
        final String mEmailStringEditTextLogin = mEmailEditTextLogin.getText().toString().trim(),
                  mPasswordStringEditTextLogin = mPasswordEditTextLogin.getText().toString();
        if(mFirebaseAuth != null){
            if (mUserFireBase != null) {
                mUserFireBase.reload().addOnCompleteListener(task -> {
                    if (mUserFireBase.isEmailVerified()) {

                        mFirebaseAuth.signInWithEmailAndPassword(mEmailStringEditTextLogin, mPasswordStringEditTextLogin)
                                .addOnCompleteListener(LoginActivity.this, taskSignInWithEmailAndPassword -> {
                                    if (taskSignInWithEmailAndPassword.isSuccessful()) {
                                        Toast.makeText(LoginActivity.this, "Iniciando", LENGTH_SHORT).show();
                                        InicioSesionCorrecto();

                                    } else
                                        GeneralMethod.showSnackback(mMsgShowSnackBarEmailPassword,mContainerLogin,LoginActivity.this);
                                });
                    } else
                        GeneralMethod.showSnackback(mMsgShowSnackBarVerificado,mContainerLogin,LoginActivity.this);
                });
            } else {
                AuthCredential mAuthCredential = EmailAuthProvider
                        .getCredential(mEmailStringEditTextLogin, mPasswordStringEditTextLogin);
                mFirebaseAuth.signInWithCredential(mAuthCredential)
                        .addOnCompleteListener(task -> InicioSesionCorrecto())
                        .addOnFailureListener(e -> GeneralMethod.showSnackback(mMsgShowSnackBarCurrentUser,mContainerLogin,LoginActivity.this));

            }
        }
    }


    //---------------------------------------GOOGLE------------------------------------------------------------
    private void LoginGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            InicioSesionCorrecto();
                            GeneralMethod.showSnackback("Bienvenido a WIMP?",mContainerLogin,LoginActivity.this);

                        } else {
                            // If sign in fails, display a message to the user.
                            GeneralMethod.showSnackback("Lo sentimos,pero la autentificacion fallo",mContainerLogin,LoginActivity.this);
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }



    private void revokeAccess() {
        // Firebase sign out
        mFirebaseAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                task -> { });
    }



    //---------------------------------------FACEBOOK----------------------------------------------------------
    private void LoginFacebook() {
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
                Toast.makeText(LoginActivity.this, error.getMessage(), LENGTH_SHORT).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        //FirebaseUser user = mFirebaseAuth.getCurrentUser();
                        InicioSesionCorrecto();
                    } else
                        Toast.makeText(LoginActivity.this, R.string.auth_failed, LENGTH_SHORT).show();

                });
    }

    //---------------------------------------CAPTURO EL RESULTADO DE LA ACTIVIDAD LUEGO DE LOGUEARSE CON LAS REDES------------------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (tipoDeLogin) {
            case "Facebook": {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
            break;
            case "Google": {
                if (requestCode == RC_SIGN_IN) {
                    GoogleSignInResult mGoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = mGoogleSignInResult.getSignInAccount();
                    assert (account) != null;
                    firebaseAuthWithGoogle(account);
                }
            }
            break;
            case "ImagenRegistro":{
                if (resultCode == RESULT_OK) {
                    switch (requestCode) {
                        case COD_SELECCIONA: {
                            assert data != null;
                            try {
                                mFotoPerfilRegistro = data.getData();
                                mFotoPerfilRegistro = GeneralMethod.reducirTamano(mFotoPerfilRegistro,this);
                                mImgPerfilDBRegistroImageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), mFotoPerfilRegistro));
                                tipoDeFoto = "SELECCIONA";

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }break;
                        case COD_FOTO: {
                            String pathTomarFoto = GeneralMethod.getPathTomarFoto();
                            assert (pathTomarFoto) != null;
                            MediaScannerConnection.scanFile(this, new String[]{pathTomarFoto}, null,
                                    (path, uri) -> Log.i("Path",""+path));
                            mImgPerfilDBRegistroImageView.setImageBitmap(BitmapFactory.decodeFile(pathTomarFoto));
                            mFotoPerfilRegistro = Uri.fromFile(new File(Objects.requireNonNull(pathTomarFoto)));
                            mFotoPerfilRegistro = GeneralMethod.reducirTamano(mFotoPerfilRegistro,this);
                            tipoDeFoto = "FOTO";
                        } break;

                    }
                }
            }break;
            default:break;
        }


    }

    //------------------------------------------------METODOS SOBREESCRITOS POR LA IMPLEMENTACION DE LA INTERFACES DE LA CLASE-------------------------------------
    // ESTE METODO ES POR DI FALLA LA CONECCION CON LA API DE GOOGLE, LO SOBREESCRIBO POR LA IMPLEMENTACION EN LA CLASE
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { }
    //METODO ONCLICK DE LOS COMPONENTES(BOTONES,TEXTVIEW)
    @Override
    public void onClick(View v) {
        final String mMsgShowSnackBar = "Todos los campos deben estar completos correctamente, por favor verifiquelos";
        switch (v.getId()) {
            case R.id.btnIniciarLogin: {
                if (GeneralMethod.RegexLogin("correo", this) && GeneralMethod.RegexLogin("contrasenavacio", this)) {
                    tipoDeLogin = "EmailPassword";
                    LoginEmailPassword();
                } else
                    GeneralMethod.showSnackback(mMsgShowSnackBar,mContainerLogin,LoginActivity.this);
            }
            break;
            case R.id.tvRegistrarseLogin: {
                instanciarDialogRegistro();
            }
            break;
            case R.id.login_button: {
                tipoDeLogin = "Facebook";
                LoginFacebook();
            }
            break;
            case R.id.sign_in_button: {
                tipoDeLogin = "Google";
                signIn();
            }
            case R.id.RecordarSesion:{
                GuardarRemember(mRecordarUsuarioCheckBox.isChecked());
            }
            break;
            default:
                break;
        }
    }

    //------------------------------------------------------------------------DIALOGO REGISTRO---------------------------------------------------------------
    @SuppressLint("ValidFragment")
    private class RegistroDialog extends DialogFragment implements View.OnClickListener {
        //Componentes Registro
        private CardView btnRegistro;
        private EditText mNombreRegistroEditText, mApellidoRegistroEditText, mEmailRegistroEditText, mPassRegistroEditText, mPassConfirmRegistroEditText,mEmailConfirmarRegistroEditText;
        private ProgressDialog progressDialog;
        private ConstraintLayout mContrainerRegistro;
        //Validaciones
        private boolean RespuestaValidacion = false;
        //Permisos
        private static final int MIS_PERMISOS = 100;

        //Firebase
        private FirebaseAuth mFirebaseAuthRegistro;
        private DatabaseReference mDatabase;
        private StorageReference mStorageReference;
        //----------------------------------------CICLOS DE VIDA DEL DIALOG-------------------------------------

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_registro, null);

            //Firebase
            mStorageReference = FirebaseStorage.getInstance().getReference();
            mFirebaseAuthRegistro = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            //Vistas
            btnRegistro = content.findViewById(R.id.CheckIn);
            mNombreRegistroEditText = content.findViewById(R.id.nombreRegistro);
            mApellidoRegistroEditText = content.findViewById(R.id.apellidoRegistro);
            mEmailRegistroEditText = content.findViewById(R.id.emailRegistro);
            mEmailConfirmarRegistroEditText=content.findViewById(R.id.confirmar_emailRegistro);
            mPassRegistroEditText = content.findViewById(R.id.passRegistro);
            mPassConfirmRegistroEditText = content.findViewById(R.id.confirmpassRegistro);
            mContrainerRegistro = content.findViewById(R.id.ContenedorRegistro);
            mImgPerfilDBRegistroImageView = content.findViewById(R.id.imgPerfilDBRegistro);
            mImgPerfilDBRegistroImageView.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeResource(getResources(),R.drawable.com_facebook_profile_picture_blank_square)));
            btnRegistro.setOnClickListener(this);
            mImgPerfilDBRegistroImageView.setOnClickListener(this);
            LimpiarEditText();

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
            });

            EscuchandoEstadoDeAutentificacion();
            return builder.create();
        }

        @Override
        public void onResume() {
            super.onResume();
            ValidarRegistro();

        }
        //--------------------------------------ESCUCHADOR DE AUTENTICACION, POR SI CAMBIA DE LOGUEO------------------------------------
        private void EscuchandoEstadoDeAutentificacion() {

            mAuthStateListener = firebaseAuth -> {
                mUserFireBase = firebaseAuth.getCurrentUser();
                if (mUserFireBase != null) {
                    dismiss();
                }
            };
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
                    tipoDeLogin="ImagenRegistro";
                    if (GeneralMethod.solicitaPermisosVersionesSuperiores(LoginActivity.this)) {
                        GeneralMethod.mostrarDialogOpciones(LoginActivity.this);
                    }
                }
                break;
            }
        }

        //METODO DE REGISTRO FIREBASE
        private void RegistrarUsuarioEmailPassword() {
            if (ValidarRegistro()) {
                final Usuario mUser = new Usuario()
                        .setEmail(mEmailRegistroEditText.getText().toString().trim())
                        .setContraseña(mPassRegistroEditText.getText().toString())
                        .setIdUsuario(mUserFireBase.getUid());
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Registrando...");
                progressDialog.show();


                    mFirebaseAuthRegistro.createUserWithEmailAndPassword(mUser.getEmail(), mUser.getContraseña())
                            .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    final Usuario.UsuarioPublico mUserPublic = new Usuario.UsuarioPublico()
                                            .setNombre(mNombreRegistroEditText.getText().toString())
                                            .setApellido(mApellidoRegistroEditText.getText().toString());
                                    mEstado = "registro";
                                    if(!tipoDeFoto.equals("VACIO")) {
                                        storageIMG(mUserFireBase.getUid(),mDatabase,mUserPublic);
                                    }
                                    else{
                                        mDatabase.child("Usuarios").child(Objects.requireNonNull(mUserFireBase.getUid())).child("Datos Personales").setValue(mUserPublic);
                                        mDatabase.child("Usuarios").child(Objects.requireNonNull(mUserFireBase.getUid())).child("Datos Personales").child("imagen").setValue(defaultUser);
                                    }
                                    final FirebaseUser firebaseUser = Objects.requireNonNull(task.getResult()).getUser();
                                    firebaseUser.sendEmailVerification();
                                    dismiss();
                                    GeneralMethod.showSnackback("Registro exitoso, gracias por registrarse!",mContainerLogin,LoginActivity.this);
                                        //poner este mensaje en el activity main porque se autologue de inicio
                                } else {
                                    Toast.makeText(RegistroDialog.this.getActivity(), "Ocurrio un inconveniente al intentar registrar el email, por favor, vuelva a intentarlo",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });

            } else {
                GeneralMethod.showSnackback("Algunos campos se encuentran vacios, por favor verifiquelos",mContrainerRegistro,RegistroDialog.this.getActivity());
            }
        }

        public void storageIMG (final String currentUserDB, final DatabaseReference mDatabase, final Usuario.UsuarioPublico mUserPublic) {

            final StorageReference mStorageImgPerfilUsuario = mStorageReference.child("Imagenes").child("Perfil").child(GeneralMethod.getRandomString());

            mStorageImgPerfilUsuario.putFile(mFotoPerfilRegistro).addOnSuccessListener(this.getActivity(), taskSnapshot -> {
                Task<Uri> taskUri = mStorageImgPerfilUsuario.getDownloadUrl();
                final String UrlFoto = Objects.requireNonNull(taskUri.getResult()).toString().replace("\"", "");
                mDatabase.child("Usuarios").child(Objects.requireNonNull(currentUserDB)).child("Datos Personales").setValue(mUserPublic);
                mDatabase.child("Usuarios").child(Objects.requireNonNull(currentUserDB)).child("Datos Personales").child("imagen").setValue(UrlFoto);
            }).addOnFailureListener(this.getActivity(), e -> Toast.makeText(RegistroDialog.this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        // METODOS COMPROBACION CAMPOS Y GENERANDO NOMBRE IMAGEN
        private boolean ValidarRegistro(){
            mNombreRegistroEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    RespuestaValidacion = GeneralMethod.RegexRegistro("nombre",mContrainerRegistro);
                }
            });
            mApellidoRegistroEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    RespuestaValidacion = GeneralMethod.RegexRegistro("apellido",mContrainerRegistro);
                }
            });
            mEmailRegistroEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    RespuestaValidacion = GeneralMethod.RegexRegistro("email",mContrainerRegistro);
                }
            });
            mEmailConfirmarRegistroEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    RespuestaValidacion = GeneralMethod.RegexRegistro("confirmaremail",mContrainerRegistro);
                }
            });
            mPassRegistroEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    RespuestaValidacion = GeneralMethod.RegexRegistro("password",mContrainerRegistro);
                }
            });
            mPassConfirmRegistroEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    RespuestaValidacion = GeneralMethod.RegexRegistro("confirmacontraseña",mContrainerRegistro);
                }
            });
            return RespuestaValidacion;
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode==MIS_PERMISOS){
                if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){//el dos representa los 2 permisos
                    GeneralMethod.showSnackback("Gracias por aceptar los permisos..!",mContrainerRegistro,LoginActivity.this);
                    GeneralMethod.mostrarDialogOpciones(this.getActivity());
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