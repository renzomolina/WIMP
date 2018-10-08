package actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whereismypet.whereismypet.R;

import org.json.JSONException;
import org.json.JSONObject;

import misclases.GestionarFacebook;
import misclases.Usuario;
import misclases.VolleySingleton;
import misclases.WebServiceJSON;

public class LoginActivity extends AppCompatActivity{

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private Profile profile;
    private AccessTokenTracker accessTokenTracker;
    private ProfileTracker profileTracker;
    private CheckBox recordarUsuario;
    private TextView Registro,OlvidoContraseña;
    private CardView Iniciar,Restablecer;
    private List<String> permisoNecesario = Arrays.asList("email", "user_birthday", "user_friends", "public_profile");


    private EditText email,password;
    private boolean CheckEditText, Validacion;
    RequestQueue requestQueue;
    ProgressDialog progressDialog;
    private  Usuario user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(this);
        progressDialog = new ProgressDialog(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        user = new Usuario();

        String correoShared= WebServiceJSON.getFromSharedPreferences("correo",this);
        Boolean aux = WebServiceJSON.getFromSharedPreferencesDB("rememberUser",this);
        if(aux && !correoShared.equals("")){
            WebServiceJSON.InicioSesionCorrecto(this,this);
        }
        else{
            if(isLoggedIn()) {
                InicioSesionCorrecto();
            }
            else {
                setContentView(R.layout.activity_login);
                Login();
                ValidarLogin();
            }}
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data ){
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public  void FacebookLogin(){
        try{
            LoginManager.getInstance().logOut();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        if(GestionarFacebook.comprobarInternet(getApplicationContext())){
            callbackManager = CallbackManager.Factory.create();
            LoginManager.getInstance().logInWithReadPermissions(this,permisoNecesario);

            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    RelativeLayout containerLogin = findViewById(R.id.ContainerLogin);
                    containerLogin.setVisibility(View.GONE);
                    String  send_token = loginResult.getAccessToken().getToken(),
                            send_user=loginResult.getAccessToken().getUserId();
                    WebServiceJSON.savedLoginSharedPreferencesFB(send_token,send_user,"FB",LoginActivity.this);
                    InicioSesionCorrecto();

                    GraphRequest request = GraphRequest.newMeRequest(Token(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    try {
                                        user.setEmail(object.getString("email"));
                                        user.setFacebook(object.getString("id"));


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,gender,birthday");
                    request.setParameters(parameters);
                    request.executeAsync();
                    accessTokenTracker = new AccessTokenTracker() {
                        @Override
                        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                        }
                    };
                    profileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            DatosPerfil(currentProfile);
                        }
                    };
                    accessTokenTracker.startTracking();
                    profileTracker.startTracking();
                    profile = Profile.getCurrentProfile();

                }

                @Override
                public void onCancel() {
                    Log.e("Login Cancelado", "n" + "login de Facebook cancelado");
                }

                @Override
                public void onError(FacebookException error) {
                    Log.e("Login Error", "n" + "error de inicio de sesión de Facebook" + error.toString());
                }
            });
        }
    }

    private void DatosPerfil(Profile perfil){
        user.setNombre(perfil.getFirstName());
        user.setApellido(perfil.getLastName());
        user.setImagenPerfilFacebook(perfil.getProfilePictureUri(400,400));
    }

    private boolean isLoggedIn(){
        boolean user,expirado,vacio;
        AccessToken accessToken = Token();
        if(accessToken != null) {
            user = (accessToken.getToken().equals(WebServiceJSON.getFromSharedPreferences("token",LoginActivity.this)) ||
                accessToken.getUserId().equals(WebServiceJSON.getFromSharedPreferences("userID",LoginActivity.this)));
            expirado = !accessToken.isExpired();
            vacio = !accessToken.getToken().isEmpty();

            return (user && expirado && vacio);

        }
        return (false);
    }
    private AccessToken Token(){
        return AccessToken.getCurrentAccessToken();
    }

    public void InicioSesionCorrecto() {

        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void Inicializar(){
        Iniciar = findViewById(R.id.Login);
        loginButton = findViewById(R.id.login_button);
        Registro = findViewById(R.id.tvRegistrarse);
        OlvidoContraseña = findViewById(R.id.tvOlvidoContraseña);
        email = findViewById(R.id.Correo);
        password = findViewById(R.id.Password);
        recordarUsuario = findViewById(R.id.RecordarSesion);
    }

    private boolean ValidarLogin(){

       email.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) { }

           @Override
           public void afterTextChanged(Editable s) {
               if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                   Drawable msgerror = getResources().getDrawable(R.drawable.icon_error);
                   msgerror.setBounds(0, 0, msgerror.getIntrinsicWidth(), msgerror.getIntrinsicHeight());
                   email.setError("Correo Invalido",msgerror);
                   Validacion = false;
               }
               else {
                   email.setError(null);
                   Validacion = true;
               }

           }
       });
       return Validacion;
    }

    private void Login(){
        Inicializar();
        Iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckEditText = !TextUtils.isEmpty(email.getText().toString().trim()) && !TextUtils.isEmpty(password.getText().toString().trim());
                if (CheckEditText && Validacion) {
                    user = new Usuario(email.getText().toString(), password.getText().toString());
                    if (recordarUsuario.isChecked()) {
                        WebServiceJSON.UserLogin(user, LoginActivity.this,true,LoginActivity.this);
                    } else {
                        WebServiceJSON.UserLogin(user, LoginActivity.this,false,LoginActivity.this);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Todos los capos son obligatorios, por favor vuelva a verificar los datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistroActivity.class);
               startActivity(i);

            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FacebookLogin();
            }
        });
    }

}