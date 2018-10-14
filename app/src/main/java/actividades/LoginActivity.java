package actividades;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.whereismypet.whereismypet.R;


import misclases.Usuario;
import misclases.WebServiceJSON;

public class LoginActivity extends BaseActivity implements View.OnClickListener {


    private CheckBox recordarUsuario;
    private TextView Registro,OlvidoContraseña;
    private CardView Iniciar,Restablecer;


    private EditText email,password;
    private boolean CheckEditText, Validacion=true;

    private  Usuario user;

    //FIREBASE-----
    private FirebaseAuth mAuth;

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
        Iniciar.setOnClickListener(this);
        Registro.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        user = new Usuario();
        mAuth = FirebaseAuth.getInstance();
        ValidarLogin();
        String correoShared= WebServiceJSON.getFromSharedPreferences("correo",this);
        Boolean aux = WebServiceJSON.getFromSharedPreferencesDB("rememberUser",this);
        if(aux && !correoShared.equals("")){
            WebServiceJSON.InicioSesionCorrecto(this,this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth!=null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user != null) {
            InicioSesionCorrecto();
        }
    }

    private void signIn(String email, String password) {
        if (!ValidarLogin()) {
            return;
        }
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this,R.string.auth_failed,Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void sendEmailVerification() {
        // findViewById(R.id.verifyEmailButton).setEnabled(false);
        final FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //findViewById(R.id.verifyEmailButton).setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void createAccount(String email, String password) {
        if (!ValidarLogin()) {
            return;
        }
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
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
                    Validacion = false;                }
                else {
                    email.setError(null);
                    Validacion = true;
                }
            }
        });
        return Validacion;
    }


    @Override
    public void onClick(View v) {
        CheckEditText = !TextUtils.isEmpty(email.getText().toString().trim()) && !TextUtils.isEmpty(password.getText().toString().trim());
        if (CheckEditText && Validacion) {
            switch (v.getId()) {
                case R.id.btnIniciarLogin: {
                    signIn(email.getText().toString(), password.getText().toString());
                    Toast.makeText(LoginActivity.this, "Iniciar", Toast.LENGTH_SHORT).show();
                }
                break;
                case R.id.tvRegistrarseLogin: {
                    createAccount(email.getText().toString(), password.getText().toString());
                    Toast.makeText(LoginActivity.this, "Crear cuenta", Toast.LENGTH_SHORT).show();
                }
                break;
                default:
                    break;
            }
        }
        else
            Toast.makeText(LoginActivity.this, "Todos los campos deben estar completos, por favor ingrese los datos que se le solicitan", Toast.LENGTH_LONG).show();

    }
}