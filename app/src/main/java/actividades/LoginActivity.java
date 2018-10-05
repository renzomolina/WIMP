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
    String EmailHolder, PasswordHolder;
    ProgressDialog progressDialog;
    private static final String URL_LOGIN = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Logueo2.php";
    private static final String URL_REGISTRO = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Registro.php";
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

        String correo = getFromSharedPreferences("correo"),
                contraseña=getFromSharedPreferences("contraseña");
        Boolean aux = getFromSharedPreferencesDB("rememberUser");
        if(aux && !correo.equals("") && !contraseña.equals("")){
            UserLogin(correo,contraseña);
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                    savedLoginSharedPreferencesFB(send_token,send_user,"FB");
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
                    DatosPerfil(profile);
                    Registro();
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
        user.setImagenPerfil(perfil.getProfilePictureUri(400,400));
    }
    public void savedLoginSharedPreferencesFB(String token, String userID, String FB){
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("userID", userID);
        editor.putString("facebook",FB);
        editor.apply();
    }

    public void savedLoginSharedPreferencesDB(String correo, String contraseña, String DB, boolean remember){
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("correo", correo);
        editor.putString("contraseña", contraseña);
        editor.putString("base", DB);
        editor.putBoolean("rememberUser",remember);
        editor.apply();
    }

    public String getFromSharedPreferences(String key){
        SharedPreferences sharedPreferences = getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }
    public Boolean getFromSharedPreferencesDB(String key){
        SharedPreferences sharedPreferences = getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key,false);
    }

    private boolean isLoggedIn(){
        boolean user,expirado,vacio;
        AccessToken accessToken = Token();
        if(accessToken != null) {
            user = (accessToken.getToken().equals(getFromSharedPreferences("token")) ||
                accessToken.getUserId().equals(getFromSharedPreferences("userID")));
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

        Intent i = new Intent(LoginActivity.this,MainActivity.class);
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
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {

           }

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
               CheckEditTextIsEmptyOrNot();
                if (CheckEditText && Validacion) {
                    if(recordarUsuario.isChecked()){
                        savedLoginSharedPreferencesDB(email.getText().toString(),password.getText().toString(),"DB",true);
                    }
                    else{
                        savedLoginSharedPreferencesDB(email.getText().toString(),password.getText().toString(),"DB",false);
                    }
                    UserLogin(email.getText().toString(),password.getText().toString());
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

    public void CheckEditTextIsEmptyOrNot() {
        EmailHolder = email.getText().toString().trim();
        PasswordHolder = password.getText().toString().trim();
        if (TextUtils.isEmpty(EmailHolder) || TextUtils.isEmpty(PasswordHolder)) {
            CheckEditText = false;
        }
        else {
            CheckEditText = true;
        }


    }

    public void UserLogin(final String userDB, final String passDB) {
        progressDialog.setMessage("Por favor, esperar...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if(ServerResponse.equalsIgnoreCase("Data Matched")) {
                            InicioSesionCorrecto();
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Correo o Contraseña incorrectos, por favor verifique los datos", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("correo", userDB);
                params.put("pass", passDB);

                return params;
            }
        };
        requestQueue.add(stringRequest);
    }


    public void Registro(){

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
                params.put("nombre",user.getNombre());
                params.put("apellido", user.getApellido());
                params.put("correo",user.getEmail());

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(this).addToRequestQueue(stringRequest);

    }
}