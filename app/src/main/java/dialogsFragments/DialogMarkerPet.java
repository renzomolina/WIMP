package dialogsFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.whereismypet.whereismypet.R;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import Modelo.Mascota;
import finalClass.GeneralMethod;

public class DialogMarkerPet extends DialogFragment implements View.OnClickListener {

    private LatLng mLatLng;
    private GoogleMap mGoogleMap;
    private Mascota mMarcadorMascota;
    private View mView;
    private Activity mActivity;
    //Componentes
    private EditText mNombreMascotaMarcador,mDescripcionMascotaMarcador;
    private ImageView mFotoMascotaMarcador;
    //CODIGOS CAMARA
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    private Uri mUriMascotaMarcador;
    private String pathTomarFoto,tipoDeFoto = "VACIO";
    //Firebase
    private FirebaseAuth mFirebaseAuth;
    private StorageReference mStorageReference;

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();

        @SuppressLint("InflateParams")
        View content = inflater.inflate(R.layout.dialog_pets, null);
        mNombreMascotaMarcador = content.findViewById(R.id.input_nombre);
        mDescripcionMascotaMarcador = content.findViewById(R.id.input_descripcion);
        mFotoMascotaMarcador = content.findViewById(R.id.imgMascota);
        mFotoMascotaMarcador.setOnClickListener(this);
        mFotoMascotaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeResource(getResources(),R.drawable.huella_mascota)));
        mFirebaseAuth = FirebaseAuth.getInstance();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);
        builder.setPositiveButton("guardar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Mascota mMascota = new Mascota();
                mMascota.setNombre(mNombreMascotaMarcador.getText().toString());
                mMascota.setDescripcion(mDescripcionMascotaMarcador.getText().toString());
            }
        });
        builder.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgMascota:{
                if (GeneralMethod.solicitaPermisosVersionesSuperiores(mActivity)) {
                    GeneralMethod.mostrarDialogOpciones(mActivity);
                }
            }break;
        }
    }
    public  DialogMarkerPet setmActivity(Activity activity) {
        this.mActivity = activity;
        return this;
    }
    public DialogMarkerPet setLatLng(LatLng LatLng){
        mLatLng = LatLng;
        return this;
    }
    public DialogMarkerPet setGoogleMap(GoogleMap googleMap){
        mGoogleMap = googleMap;
        return this;
    }
    public DialogMarkerPet setView(View view){
        this.mView = view;
        return this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){//el dos representa los 2 permisos
                GeneralMethod.showSnackback("Gracias por aceptar los permisos..!",mView,mActivity);
                GeneralMethod.mostrarDialogOpciones(mActivity);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case COD_SELECCIONA: {
                    // URI Camara
                    Uri uriSeleccionarFoto = Objects.requireNonNull(data).getData();
                    mUriMascotaMarcador = uriSeleccionarFoto;
                    tipoDeFoto = "SELECCIONA";
                    try {
                        mFotoMascotaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), uriSeleccionarFoto)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }break;
                case COD_FOTO: {
                    MediaScannerConnection.scanFile(mActivity, new String[]{pathTomarFoto}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Path", "" + path);
                                }
                            });
                    mFotoMascotaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeFile(pathTomarFoto)));
                    mUriMascotaMarcador = Uri.fromFile(new File(pathTomarFoto));
                    tipoDeFoto = "FOTO";
                } break;
            }
        } else {
            mFotoMascotaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeResource(getResources(),R.drawable.com_facebook_profile_picture_blank_square)));
        }
    }

    private void RegistrarMarcadorDeMascota(Mascota mMascota){
        ProgressDialog progressDialog = new ProgressDialog(mActivity);
        progressDialog.setMessage("Registrando mascota perdida...");
        progressDialog.show();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference currentUserDB = mDatabase.child(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid());

        if(!tipoDeFoto.equals("VACIO")) {
            storageIMG(currentUserDB,mMascota);
        }
        else{
            currentUserDB.child("image").setValue("defaultUser");
        }

    }

    private void storageIMG(final DatabaseReference currentUserDB, final Mascota mMascota){
        final StorageReference mStorageImgMarkerPet = mStorageReference.child("Imagenes").child("Perfil").child(GeneralMethod.getRandomString());
        mStorageImgMarkerPet.putFile(mUriMascotaMarcador).addOnSuccessListener(mActivity, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                currentUserDB.child("Marcadores").child("nombre").setValue(mMascota.getNombre());
                currentUserDB.child("Marcadores").child("descripcion").setValue(mMascota.getDescripcion());
                currentUserDB.child("image").setValue(taskSnapshot.getStorage().getDownloadUrl().toString());
            }
        }).addOnFailureListener(mActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                GeneralMethod.showSnackback("Lo sentimos, pero ocurrio un incoveniente",mView,mActivity);
            }
        });

    }

}
