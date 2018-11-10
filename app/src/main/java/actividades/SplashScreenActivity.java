package actividades;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.whereismypet.whereismypet.R;

import Modelo.PreferenciasLogin;

public class SplashScreenActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUserFireBase;
    SharedPreferences sharedPreferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        RelativeLayout Splash = findViewById(R.id.Splash);
        //crearAccesoDirectoAlInstalar(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserFireBase = mFirebaseAuth.getCurrentUser();

        boolean chek = LecturaDeTipoLogin().isRecordarUsuario();

        Animation animationSplash = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.transition);
        Splash.startAnimation(animationSplash);

        final Intent intent = new Intent(getApplicationContext(),LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Pref(chek);

               // SplashScreenActivity.this.startActivity(new Intent(SplashScreenActivity.this,LoginActivity.class));
                // SplashScreenActivity.this.finish();
            }
        },2500);




        crearAccesoDirectoEnEscritorio("WIMP?");
    }
    private PreferenciasLogin LecturaDeTipoLogin(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return new PreferenciasLogin().setTipoSignOut(sharedPreferences.getString("type_sign_out", "default"))
                .setRecordarUsuario(sharedPreferences.getBoolean("remember", true))
                .setTipoSignIn(sharedPreferences.getString("type_sign_in", "default"));}

    private void Pref(boolean t){
        String res = String.valueOf(t);
        switch (LecturaDeTipoLogin().getTipoSignIn())
        {
            case "password":
                switch (res) {
                    case "true": {
                        InicioSesion();
                    }
                    break;
                    case "false": {
                        Loguearse();
                    }break;
                }
                break;
            case"facebook":
                InicioSesion();
                break;
            case "google":
                InicioSesion();
                break;
            default:
                Loguearse();
                break;

        }
    }

    private void InicioSesion() {
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }
    private void Loguearse()
    {
        SplashScreenActivity.this.startActivity(new Intent(SplashScreenActivity.this,LoginActivity.class));
        SplashScreenActivity.this.finish();
    }


    //----------------------------------------ACCESO DIRECTO---------------------------------------------------------
     private void crearAccesoDirectoEnEscritorio(String nombre) {
        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getIntentShortcut());
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, nombre);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this.getApplicationContext(), R.drawable.icon_app));
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        this.sendBroadcast(shortcutIntent);
    }

    public Intent getIntentShortcut(){
        Intent i = new Intent();
        i.setClassName(getApplicationContext().getPackageName(), getApplicationContext().getPackageName() + "." + this.getLocalClassName());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }


/*
    public void crearAccesoDirectoAlInstalar(Activity actividad)
    {
        SharedPreferences preferenciasapp;
        boolean aplicacioninstalada;

        preferenciasapp = PreferenceManager.getDefaultSharedPreferences(actividad);
        aplicacioninstalada = preferenciasapp.getBoolean("aplicacioninstalada", Boolean.FALSE);

        if(!aplicacioninstalada)
        {
            Intent shortcutIntent = new Intent();
            //shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getIntentShortcut());
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, Intent.ShortcutIconResource.fromContext(actividad, R.string.app_name));
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,Intent.ShortcutIconResource.fromContext(actividad, R.drawable.icon_app));
            shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            actividad.sendBroadcast(shortcutIntent);

            SharedPreferences.Editor editor = preferenciasapp.edit();
            editor.putBoolean("aplicacioninstalada", true);
            editor.apply();
        }
    }
*/
   /*
   private SharedPreferences appPref;
   appPref = getSharedPreferences("isFirstTime", 0);
        boolean isFirstTime = appPref.getBoolean("isFirstTime", true);
        if (isFirstTime) {
            crearAccesoDirecto();
        }

    private void crearAccesoDirecto() {
        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, getIntentShortcut());
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "WIMP?");
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this.getApplicationContext(), R.drawable.icon_app));
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        this.sendBroadcast(shortcutIntent);
        SharedPreferences.Editor editor = appPref.edit();
        editor.putBoolean("isFirstTime", false);
        editor.apply();
    }

    public Intent getIntentShortcut(){
        Intent i = new Intent();
        i.setClassName(this.getPackageName(), this.getPackageName() + "." + this.getLocalClassName());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return i;
    }
    */

}
