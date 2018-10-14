package actividades;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.auth.AuthUI;

import com.whereismypet.whereismypet.R;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import misclases.Usuario;

import static android.widget.Toast.LENGTH_SHORT;

public class LoginActivity extends BaseActivity implements View.OnClickListener {


    private CheckBox mRecordarUsuarioCheckBox;
    private TextView mRegistroTextView,mOlvidoContraseñaTextView;
    private CardView mIniciarCardView,mRestablecerCardView;
    private EditText mEmailEditText,mPasswordEditText;
    private boolean CheckEditText, Validacion=true;
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
        callbackManager = CallbackManager.Factory.create();
        //Botones
        mIniciarCardView.setOnClickListener(this);
        mRegistroTextView.setOnClickListener(this);
        mOlvidoContraseñaTextView.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        EscuchandoEstadoDeAutentificacion();
        KeyHash();
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
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.whereismypet.whereismypet",
                    PackageManager.GET_SIGNATURES);
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


    private boolean ValidarLogin(){

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
        return Validacion;
    }

    public boolean checkEditTextIsEmpty(){
        return !TextUtils.isEmpty(mEmailEditText.getText().toString().trim()) && !TextUtils.isEmpty(mPasswordEditText.getText().toString().trim());
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btnIniciarLogin: {
                if (Validacion && checkEditTextIsEmpty()) {
                    if(mFirebaseAuth.signInWithEmailAndPassword(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString()).isSuccessful()){
                        Toast.makeText(LoginActivity.this, "Iniciar", LENGTH_SHORT).show();
                        InicioSesionCorrecto();
                    }

                }
                else
                    Toast.makeText(LoginActivity.this, "Todos los campos deben estar completos, por favor ingrese los datos que se le solicitan", LENGTH_SHORT).show();
            }
            break;
            case R.id.tvRegistrarseLogin: {
                Toast.makeText(LoginActivity.this, "Crear cuenta", LENGTH_SHORT).show();
            }
            break;
            case R.id.login_button: {
                LoginFacebook();
            }break;
            default:break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

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
                            Toast.makeText(LoginActivity.this,R.string.auth_failed,LENGTH_SHORT);

                    }
                });
    }
}