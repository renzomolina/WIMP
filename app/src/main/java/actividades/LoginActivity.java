package actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whereismypet.whereismypet.R;

import misclases.GestionarFacebook;

public class LoginActivity extends AppCompatActivity{

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private TextView Registro,OlvidoContraseña;
    private CardView Iniciar,Restablecer,Registrar;
    private List<String> permisoNecesario = Arrays.asList("email", "user_birthday", "user_friends", "public_profile");

    private EditText email,password;
    private EditText nombre, apellido, correo, pass, confirmpass;
    private boolean CheckEditText;
    RequestQueue requestQueue;
    String EmailHolder, PasswordHolder, Apellido, Nombre, Correo, Contraseña, ConfirmacionPass;
    ProgressDialog progressDialog;
    String HttpUrl = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_Logueo.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        requestQueue = Volley.newRequestQueue(this);
        progressDialog = new ProgressDialog(this);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if(isLoggedIn()) {
            InicioSesionCorrecto();
        }
        else {
            setContentView(R.layout.login_activity);
            Login();
        }
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
            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    RelativeLayout containerLogin = findViewById(R.id.ContainerLogin);
                    containerLogin.setVisibility(View.GONE);
                    InicioSesionCorrecto();
                    savedLoginSharedPreferences(loginResult.getAccessToken().getToken(),loginResult.getAccessToken().getUserId());
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

    public void savedLoginSharedPreferences(String token, String userID){
        SharedPreferences sharedPreferences;
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.putString("userID", userID);
        editor.apply();
    }

    public String getFromSharedPreferences(String key){
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }

    private boolean isLoggedIn(){
        boolean b1,b2,b3,b4;
        AccessToken accessToken = Token();
        if(accessToken != null) {
            b1= (accessToken.getToken().equals(getFromSharedPreferences("token")) ||
                accessToken.getUserId().equals(getFromSharedPreferences("userID")));
            b2= !accessToken.isExpired();
            b3= !accessToken.getToken().isEmpty();

            return (b1 && b2 && b3);

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
    }

    private void InicializarEditText(){
        nombre = findViewById(R.id.nombre);
        apellido = findViewById(R.id.apellido);
        correo = findViewById(R.id.email);
        pass = findViewById(R.id.pass);
        confirmpass = findViewById(R.id.confirmpass);
    }

    private void Login(){
        Inicializar();
        Iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckEditTextIsEmptyOrNot("Login");
                if (CheckEditText) {
                    UserLogin();
                } else {
                    Toast.makeText(getApplicationContext(), "Campos vacios, por favor vuelva a verificar los datos", Toast.LENGTH_LONG).show();
                }
            }
        });
        Registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.registro_activity);
                Registrar = findViewById(R.id.CheckIn);
                Registrar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckEditTextIsEmptyOrNot("CheckIn");
                        if (CheckEditText) {
                            Registro();
                        } else {
                            Toast.makeText(getApplicationContext(), "Lo siento, pero todos los campos deben estar completos...", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FacebookLogin();
            }
        });
    }

    public void CheckEditTextIsEmptyOrNot(String tipo) {
        switch (tipo){
            case "Login": {
                EmailHolder = email.getText().toString().trim();
                PasswordHolder = password.getText().toString().trim();
                if (TextUtils.isEmpty(EmailHolder) || TextUtils.isEmpty(PasswordHolder)) {
                    CheckEditText = false;
                } else {
                    CheckEditText = true;
                }
            }break;
            case"CheckIn":{
                InicializarEditText();
                Nombre = nombre.getText().toString().trim();
                Apellido = apellido.getText().toString().trim();
                Correo = correo.getText().toString().trim();
                Contraseña = pass.getText().toString().trim();
                ConfirmacionPass = confirmpass.getText().toString().trim();

                if (TextUtils.isEmpty(Nombre) || TextUtils.isEmpty(Apellido) || TextUtils.isEmpty(Correo)
                        || TextUtils.isEmpty(Contraseña) || TextUtils.isEmpty(ConfirmacionPass)) {

                    CheckEditText = false;
                } else {
                    CheckEditText = true;
                }
            }break;
        }
    }

    public void UserLogin() {
        progressDialog.setMessage("Por favor, esperar...");
        progressDialog.show();

        HttpUrl= HttpUrl+"?correo="+email.getText()+"&pass="+password.getText();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if(ServerResponse.equalsIgnoreCase("Data Matched")) {
                            InicioSesionCorrecto();
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Correo o Contraseña incorrectos, por favor verifique los datos", Toast.LENGTH_LONG).show();
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
                params.put("User_Email", EmailHolder);
                params.put("User_Password", PasswordHolder);

                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void Registro(){
        progressDialog.setMessage("Por favor, esperar...");
        progressDialog.show();
//http://www.secsanluis.com.ar/servicios/varios/wimp/W_insertarCliente.php?id=NULL&nombre=jose23&apellido=garcia2
// &correo=algo@era&premium=0&verificado=1&password=234&imagen=jojojo.jpg&facebook=algo
        HttpUrl= HttpUrl+"?id="+null+"&nombre="+nombre.getText()+"&apellido="+apellido.getText()+"&correo="+correo.getText()
                +"&premium="+0+"&verificado="+1+"&password="+pass.getText()+"&imagen="+""+"&facebook="+"";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, HttpUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String ServerResponse) {
                        progressDialog.dismiss();
                        if(ServerResponse.equalsIgnoreCase("Data Matched")) {
                            setContentView(R.layout.login_activity);
                            Toast.makeText(getApplicationContext(), "Usuario creado exitosamente!!!", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Correo o Contraseña incorrectos, por favor verifique los datos", Toast.LENGTH_LONG).show();
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
                Map<String,String> params = new HashMap<String, String>();
                params.put("User_Nombre",Nombre);
                params.put("User_Apellido", Apellido);
                params.put("User_Correo",Correo);
                params.put("User_Contraseña",Contraseña);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }
}