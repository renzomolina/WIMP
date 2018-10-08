package misclases;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import com.whereismypet.whereismypet.R;
import java.io.File;

public class GestionarFacebook {
    public static File file = null;
    public static File dir = null;
    public static File root = null;
    Dialog alertDialog;

    public static void AboutBox(String Msg, Context con) {
        new AlertDialog.Builder(con).setTitle(con.getResources().getString(R.string.alert)).setMessage(Msg)
                .setPositiveButton(con.getResources().getString(R.string.ok), null).show();
    }

    public static void AboutBoxWithFinishActivity(String Msg, final Context con) {
        new AlertDialog.Builder(con).setTitle(con.getResources().getString(R.string.alert)).setMessage(Msg)
                .setPositiveButton("OK", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        ((Activity) con).finish();
                        dialog.dismiss();
                        //((Activity) con).finish();

                    }
                }).show();

    }

    public static boolean comprobarInternet(Context ctx) {

        NetworkInfo informacion = ((ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();

        if (informacion == null || !informacion.isConnected()) {
            return false;
        }
        if (informacion.isRoaming()) {

            // aquí está la opción de roaming puedes cambiarla si quieres
            // deshabilita internet en itinerancia, o simplemente devuelve falso
            return false;
        }
        return true;
    }

}
