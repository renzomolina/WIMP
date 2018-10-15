package actividades;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;


import com.whereismypet.whereismypet.R;

import clases_estaticas.Facebook;
import clases_estaticas.FragmentsDialogs;
import clases_estaticas.GeneralMethods;
import misclases.Usuario;
import clases_estaticas.WebServiceJSON;

public class LoginActivity extends AppCompatActivity {

    //  Botonos de Login con Facebook
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    //  Componentes
    private CheckBox recordarUsuario;
    private TextView Registro, OlvidoContraseña;
    private CardView Iniciar, Restablecer;
    private EditText email, password;
    //  Extras
    private boolean CheckEditText, Validacion;
    private Usuario user;
    private int tipoDeLogin = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        user = new Usuario();
        String correoShared = GeneralMethods.getFromSharedPreferences("correo", this);
        Boolean aux = GeneralMethods.getFromSharedPreferencesDB("rememberUser", this);
        if (aux && !correoShared.equals("")) {
            GeneralMethods.InicioSesionCorrecto(this, this);
        } else {
            if (Facebook.isLoggedIn(LoginActivity.this)) {
                GeneralMethods.InicioSesionCorrecto(LoginActivity.this, LoginActivity.this);
            } else {
                setContentView(R.layout.activity_login);
                ClickBotonesLogin();
                ValidarLogin();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (tipoDeLogin){
            case 0:{ FragmentsDialogs.RegistroDialog.ResultadoDeCamara(requestCode, data);}break;//Login con base de datos
            case 1:{ callbackManager.onActivityResult(requestCode, resultCode, data);}break; //Login con facebook
            default:break;
        }



    }

    private void Inicializar() {
        Iniciar = findViewById(R.id.Login);
        loginButton = findViewById(R.id.login_button);
        Registro = findViewById(R.id.tvRegistrarse);
        OlvidoContraseña = findViewById(R.id.tvOlvidoContraseña);
        email = findViewById(R.id.Correo);
        password = findViewById(R.id.Password);
        recordarUsuario = findViewById(R.id.RecordarSesion);

    }

    private boolean ValidarLogin() {

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()) {
                    Drawable msgerror = getResources().getDrawable(R.drawable.icon_error);
                    msgerror.setBounds(0, 0, msgerror.getIntrinsicWidth(), msgerror.getIntrinsicHeight());
                    email.setError("Correo Invalido", msgerror);
                    Validacion = false;
                } else {
                    email.setError(null);
                    Validacion = true;
                }

            }
        });
        return Validacion;
    }

    private void ClickBotonesLogin() {
        Inicializar();
        Iniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckEditText = !TextUtils.isEmpty(email.getText().toString().trim()) && !TextUtils.isEmpty(password.getText().toString().trim());
                if (CheckEditText && Validacion) {
                    user = new Usuario(email.getText().toString(), password.getText().toString());
                    if (recordarUsuario.isChecked()) {
                        WebServiceJSON.UserLogin(user, LoginActivity.this, true, LoginActivity.this);
                    } else {
                        WebServiceJSON.UserLogin(user, LoginActivity.this, false, LoginActivity.this);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Todos los capos son obligatorios, por favor vuelva a verificar los datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDeLogin = 0;
                FragmentsDialogs.context = LoginActivity.this;
                FragmentsDialogs.activity = LoginActivity.this;
                FragmentsDialogs.instaciaRegistro();

            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tipoDeLogin = 1;
                Facebook.callbackManager = callbackManager;
                callbackManager = Facebook.FacebookLogin(LoginActivity.this, LoginActivity.this,loginButton);
            }
        });
    }

}

