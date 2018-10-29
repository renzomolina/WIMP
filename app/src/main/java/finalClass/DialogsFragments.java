package finalClass;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.whereismypet.whereismypet.R;

import java.util.Objects;

public final class DialogsFragments {

    @SuppressLint("ValidFragment")
    public static class NosotrosDialog extends DialogFragment {
        @NonNull
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_we_are_us, null);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setNegativeButton("cerrar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }
        public void instaciarNosotros(){
            NosotrosDialog dialog = new NosotrosDialog();  //Instanciamos la clase con el dialogo
            dialog.setCancelable(false);
            assert getFragmentManager() != null;
            dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo
        }
    }

}
