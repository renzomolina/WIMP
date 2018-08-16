package actividades;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.whereismypet.whereismypet.R;

public class SplashScreenActivity extends AppCompatActivity {
    private SharedPreferences appPref;
    private boolean isFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        RelativeLayout Splash = findViewById(R.id.Splash);

        appPref = getSharedPreferences("isFirstTime", 0);
        isFirstTime = appPref.getBoolean("isFirstTime", true);
        if (isFirstTime) {
            crearAccesoDirecto();
        }


        Animation animationSplash = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.transition);
        Splash.startAnimation(animationSplash);
        final Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SplashScreenActivity.this.startActivity(new Intent(SplashScreenActivity.this,LoginActivity.class));
                SplashScreenActivity.this.finish();
            }
        },2500);



        /*
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         Thread timer = new Thread(){
            @Override
            public void run() {
                super.run();
                try{
                    sleep(2000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    SplashScreenActivity.this.startActivity(intent);
                    SplashScreenActivity.this.finish();
                }
            }
        };*/

    }


    //----------------------------------------ACCESO DIRECTO---------------------------------------------------------
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

}
