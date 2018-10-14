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


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.whereismypet.whereismypet.R;

import org.json.JSONException;
import org.json.JSONObject;

import misclases.Usuario;
import misclases.VolleySingleton;
import misclases.WebServiceJSON;

public class LoginActivity extends AppCompatActivity{


    private CheckBox recordarUsuario;
    private TextView Registro,OlvidoContraseña;
    private CardView Iniciar,Restablecer;


    private EditText email,password;
    private boolean CheckEditText, Validacion;

    private  Usuario user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Iniciar = findViewById(R.id.btnIniciarLogin);
        Registro = findViewById(R.id.tvRegistrarseLogin);
        OlvidoContraseña = findViewById(R.id.tvOlvidoContraseñaLogin);
        email = findViewById(R.id.Correo);
        password = findViewById(R.id.Password);
        recordarUsuario = findViewById(R.id.RecordarSesion);
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
    }



    public void InicioSesionCorrecto() {

        Intent i = new Intent(this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
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


}