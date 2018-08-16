package actividades;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.whereismypet.whereismypet.R;


public class LoginActivity extends AppCompatActivity{

    TextView tvRegistro,tvOlvidoContraseña,tvInfo;
    CardView cdIniciarSesion,cdRestablecer,cdRegistrar;
    LoginButton loginButton;
    CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.login_activity);

        // Botones
        cdIniciarSesion = findViewById(R.id.Login);
        cdRegistrar = findViewById(R.id.CheckIn);
        cdRestablecer = findViewById(R.id.Restore);

        tvRegistro = findViewById(R.id.tvRegistrarse);
        tvOlvidoContraseña = findViewById(R.id.tvOlvidoContraseña);

        tvInfo = findViewById(R.id.tvInfo);

        // Establecer las devoluciones de llamada
        callbackManager = CallbackManager.Factory.create();
        loginButton = findViewById(R.id.login_button);

        // Registrar las devoluciones de llamada
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        tvInfo.setText(
                                "User ID: "
                                        + loginResult.getAccessToken().
                                        getUserId()
                                        + "n" +
                                        "Auth Token: "
                                        + loginResult.getAccessToken().
                                        getToken()
                        );
                        Toast.makeText(LoginActivity.this, loginResult.getAccessToken().toString(), Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onCancel() {
                        String cancelMessage = "Login Cancelado.";
                        tvInfo.setText(cancelMessage);
                        Toast.makeText(LoginActivity.this, cancelMessage, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        String errorMessage = "Login error.";
                        tvInfo.setText(errorMessage);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();


                    }
                }
        );



        if(isLoggedIn())
            tvInfo.setText("User ID: "
                    + AccessToken.getCurrentAccessToken().getUserId());

        cdIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(i);
            }
        });



        tvRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.registro_activity);

            }
        });
        tvOlvidoContraseña.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.restore_password);

            }
        });

    }

    /**
     * Comprueba si el usuario ha iniciado sesión en Facebook y el
     token de acceso está activo
     * @return
     */
    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return (accessToken != null) && (!accessToken.isExpired());
    }

    /**
     * datos de interés en el gestor de devolución de llamada
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //los logs de 'instalar' y 'aplicación activa' App Eventos.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs de'app desactivada' App Eventos.
        AppEventsLogger.deactivateApp(this);
    }

}

