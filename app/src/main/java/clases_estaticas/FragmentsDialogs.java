package clases_estaticas;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.whereismypet.whereismypet.R;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public final class FragmentsDialogs {
    public static Context context;
    public static Activity activity;
    @SuppressLint("ValidFragment")
    public static class RegistroDialog extends DialogFragment implements View.OnClickListener {
        private static CircleImageView btnImagenPerfilCircular;
        private CardView btnRegistrar;
        private static final int COD_SELECCIONA = 10;
        private static final int COD_FOTO = 20;
        private static Bitmap bitmapImagenPerfil;
        private static boolean withImage = false;
        private static int codigoOpcion = 0;
    private static View content;
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            content = inflater.inflate(R.layout.activity_registro, null);
            btnImagenPerfilCircular = content.findViewById(R.id.imgPerfilDB);
            btnImagenPerfilCircular.setOnClickListener(this);
            btnRegistrar = content.findViewById(R.id.CheckIn);
            btnRegistrar.setOnClickListener(this);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setNegativeButton("Volver", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }


        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.CheckIn: {
                }
                break;
                case R.id.imgPerfilDB: {
                    if (GeneralMethods.solicitaPermisosVersionesSuperiores(context, activity)) {
                        GeneralMethods.mostrarDialogOpciones(context, activity);
                    }
                }
                break;
            }
        }

        public static void ResultadoDeCamara(int requestCode, Intent data) {
            switch (requestCode) {
                case COD_SELECCIONA:
                    if (data != null) {
                        try {
                            bitmapImagenPerfil = MediaStore.Images.Media.getBitmap(context.getContentResolver(), data.getData());
                            btnImagenPerfilCircular.setImageBitmap(bitmapImagenPerfil);
                            codigoOpcion = COD_SELECCIONA;
                            withImage = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case COD_FOTO:
                    MediaScannerConnection.scanFile(context, new String[]{GeneralMethods.pathTomarFoto}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Path", "" + path);
                                }
                            });
                    codigoOpcion = COD_FOTO;
                    bitmapImagenPerfil = BitmapFactory.decodeFile(GeneralMethods.pathTomarFoto);
                    btnImagenPerfilCircular.setImageBitmap(bitmapImagenPerfil);
                    withImage = true;
                    break;
            }
            if (bitmapImagenPerfil == null) {
                withImage = false;
                btnImagenPerfilCircular = content.findViewById(R.id.imgPerfilDB);
                btnImagenPerfilCircular.setImageDrawable(activity.getResources().getDrawable(R.drawable.com_facebook_profile_picture_blank_square));
            }
        }
    }

    public static void instaciaRegistro() {
        RegistroDialog dialog = new RegistroDialog(); //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(activity.getFragmentManager(), "REGISTRO");// Mostramos el dialogo
    }
}