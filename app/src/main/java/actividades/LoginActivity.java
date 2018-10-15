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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.auth.AuthUI;

import com.whereismypet.whereismypet.R;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import misclases.Usuario;

import static android.widget.Toast.LENGTH_SHORT;

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
    private FirebaseUser userFireBase;
    public static final int RC_SIGN_IN = 1;
    //FACEBOOK--
    private CallbackManager callbackManager;
    private LoginButton loginButton;
    private List<String> permisosNecesariosFacebook = Arrays.asList("email","user_birthday","user_friends","public_profile");
    //GOOGLE
    private SignInButton signInButton;
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
        signInButton = findViewById(R.id.sign_in_button);
        callbackManager = CallbackManager.Factory.create();

        //Botones
        mIniciarCardView.setOnClickListener(this);
        mRegistroTextView.setOnClickListener(this);
        mOlvidoContraseñaTextView.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);

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
                userFireBase = firebaseAuth.getCurrentUser();
                if(AccessToken.getCurrentAccessToken()!=null){
                    Toast.makeText(LoginActivity.this,"Faceboook",LENGTH_SHORT).show();
                }

                if(userFireBase!=null)
                {
                    user = new Usuario();
                    //user.setNombre(userFireBase.getDisplayName());
                    user.setEmail(userFireBase.getEmail());
                }
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
        mFirebaseAuth.signInWithEmailAndPassword(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Iniciar", LENGTH_SHORT).show();
                            InicioSesionCorrecto();
                        }
                    }
                });
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
        private TextView mNombreRegistroTextView, mApellidoRegistroTextView, mEmailRegistroTextView, mPassRegistroTextView;
        private FirebaseAuth mFirebaseAuthRegistro;

        private ProgressDialog progressDialog;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_registro, null);
            btnRegistro = content.findViewById(R.id.CheckIn);
            mNombreRegistroTextView = content.findViewById(R.id.nombreRegistro);
            mApellidoRegistroTextView = content.findViewById(R.id.apellidoRegistro);
            mEmailRegistroTextView = content.findViewById(R.id.emailRegistro);
            mPassRegistroTextView = content.findViewById(R.id.passRegistro);
            btnRegistro.setOnClickListener(this);
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

        private void RegistrarUsuarioEmailPassword() {
            if (EditTextIsEmpty()) {
                progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setMessage("Registrando...");
                progressDialog.show();
                mFirebaseAuthRegistro.createUserWithEmailAndPassword(mEmailRegistroTextView.getText().toString().trim(), mNombreRegistroTextView.getText().toString()).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    //crear entrada a base
                                    HashMap<String, Object> map = new HashMap<>();
                                    final FirebaseUser firebaseUser = Objects.requireNonNull(task.getResult()).getUser();
                                    map.put("user_id", mNombreRegistroTextView.getText().toString());
                                    map.put("email", mEmailRegistroTextView.getText().toString().trim());
                                    map.put("last_connection", Calendar.getInstance(Locale.US).getTimeInMillis());
                                    DatabaseReference userDbRef = FirebaseDatabase.getInstance().getReference().child("users").child(firebaseUser.getUid());
                                    userDbRef.setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(mNombreRegistroTextView.getText().toString())
                                                        .build();
                                                Objects.requireNonNull(mFirebaseAuthRegistro.getCurrentUser()).updateProfile(userProfileChangeRequest).addOnCompleteListener(
                                                        new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                firebaseUser.sendEmailVerification();
                                                                dismiss();
                                                                Toast.makeText(LoginActivity.this, "Registrado con exito", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                );
                                            }
                                            else {
                                                //userDelete
                                                Toast.makeText(LoginActivity.this, "No se pudo Agregar", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(LoginActivity.this, "ERROR", Toast.LENGTH_SHORT).show(); }
                            }
                        }
                );

            } else {
                Toast.makeText(LoginActivity.this, "VACIOS", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean EditTextIsEmpty() {
            return (!TextUtils.isEmpty(mNombreRegistroTextView.getText().toString().trim()) &&
                    !TextUtils.isEmpty(mApellidoRegistroTextView.getText().toString().trim()) &&
                    !TextUtils.isEmpty(mPassRegistroTextView.getText().toString().trim()));
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.CheckIn: {
                    RegistrarUsuarioEmailPassword();
                }
                break;
            }
        }
    }

    public void instanciarDialogRegistro() {
        RegistroDialog dialog = new RegistroDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "REGISTRO");// Mostramos el dialogo
    }
}