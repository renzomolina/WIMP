package dialogsFragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.whereismypet.whereismypet.R;

import java.util.Objects;

import Modelo.Comentario;
import Modelo.Mascota;
import Modelo.Usuario;
import finalClass.GeneralMethod;

public class DialogComentario extends DialogFragment implements View.OnClickListener {
    //Componentes
    private EditText eComentario;
    private ImageView eOk;
    private ImageView eCancelar;
    //Firebase
    private FirebaseAuth mFirebaseAuth;
    private StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();

        @SuppressLint("InflateParams")
        View content = inflater.inflate(R.layout.dialog_comentar_marcador_mascota, null);
        eComentario = content.findViewById(R.id.eComentarMarcador);
        eOk = content.findViewById(R.id.ImgOk);
        eCancelar = content.findViewById(R.id.ImgCancelar);
        eOk.setOnClickListener(this);
        eCancelar.setOnClickListener(this);
        mFirebaseAuth = FirebaseAuth.getInstance();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);


        return builder.create();
    }

    @Override
    public void onClick(View v) {

    /*    switch (v.getId()){
            case R.id.ImgOk:{
                Comentario mComentario= new Comentario()
                        .setCuerpo(eComentario.getText().toString())
                        .setIdUsuario(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid());
            //    mDatabaseReference.child("Usuarios").child("Marcadores").child("Pet").child()
            }break;

            case R.id.ImgCancelar:{
                dismiss();
            }break;
        }
    }*/
    }
}
