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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.whereismypet.whereismypet.R;


import java.io.File;
import java.io.IOException;
import java.util.Objects;

import Modelo.Marcadores;
import Modelo.Mascota;
import Modelo.Tienda;
import finalClass.GeneralMethod;


@SuppressLint("ValidFragment")
public class DialogShop extends DialogFragment implements View.OnClickListener {


    private LatLng mLatLng;
    private GoogleMap mGoogleMap;
    //Componentes
    private EditText mNombreTiendaMarcador,mDescripcionTiendaMarcador,mTelefonoTiendaMarcador,mDireccionTiendaMarcador;
    private ImageView mFotoTiendaMarcador;
    //CODIGOS CAMARA
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    private Uri mUriTiendaMarcador;
    private String tipoDeFoto = "VACIO";
    //Firebase
    private FirebaseAuth mFirebaseAuth;
    private StorageReference mStorageReference;
    private String pathCaptureShop;

    private ProgressDialog progressDialog;

    private final String defautMarkerShop = "https://firebasestorage.googleapis.com/v0/b/wimp-219219.appspot.com/o/Imagenes%2FMarcadores%2FShop%2FdefaultShop.png?alt=media&token=ca7b5630-d219-489c-b0f5-a0a75daed0ac";
    public DialogShop(GoogleMap map, LatLng latLng) {
        this.mLatLng = latLng;
        this.mGoogleMap = map;
    }

    @NonNull
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();

        @SuppressLint("InflateParams")
        View content = inflater.inflate(R.layout.dialog_shop, null);
        mNombreTiendaMarcador = content.findViewById(R.id.input_nombreShop);
        mDescripcionTiendaMarcador = content.findViewById(R.id.input_descripcionShop);
        mTelefonoTiendaMarcador = content.findViewById(R.id.input_telefonoShop);
        mDireccionTiendaMarcador = content.findViewById(R.id.input_direccionShop);
        mFotoTiendaMarcador = content.findViewById(R.id.imgMercado);
        mFotoTiendaMarcador.setOnClickListener(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);

        builder.setPositiveButton("GUARDAR", (dialogInterface, i) -> {
            Marcadores mTienda = new Tienda()
                    .setDireccion(mDireccionTiendaMarcador.getText().toString())
                    .setLongitud(String.valueOf(mLatLng.longitude))
                    .setLatitud(String.valueOf(mLatLng.latitude))
                    .setIdMarcador(GeneralMethod.getRandomString())
                    .setNombre(mNombreTiendaMarcador.getText().toString())
                    .setDescripcion(mDescripcionTiendaMarcador.getText().toString())
                    .setTelefono(mTelefonoTiendaMarcador.getText().toString());
            RegistrarMarcadorDeTienda((Tienda)mTienda);
        });
        builder.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dismiss();
            }
            return false;
        });

        return builder.create();
    }
    private void CreateMarkers(LatLng latLng,GoogleMap googleMap, Tienda mMarcadorTienda) {
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(String.valueOf(mMarcadorTienda.getNombre()))
                .snippet(mMarcadorTienda.getDescripcion())
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_markers)));
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgMercado:{
                if (GeneralMethod.solicitaPermisosVersionesSuperiores(this.getActivity())) {
                    mostrarDialogOpciones();
                }
            }break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case COD_SELECCIONA: {
                    // URI Camara
                    mUriTiendaMarcador = Objects.requireNonNull(data).getData();
                    tipoDeFoto = "SELECCIONA";
                    try {
                        mFotoTiendaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(MediaStore.Images.Media.getBitmap(DialogShop.this.getActivity().getContentResolver(), mUriTiendaMarcador)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }break;
                case COD_FOTO: {
                    MediaScannerConnection.scanFile(DialogShop.this.getActivity(), new String[]{pathCaptureShop}, null,(path, uri) -> Log.i("Path", "" + path));
                    mFotoTiendaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeFile(pathCaptureShop)));
                    mUriTiendaMarcador = Uri.fromFile(new File(pathCaptureShop));
                    tipoDeFoto = "FOTO";
                } break;
            }
        } else {
            mFotoTiendaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeResource(getResources(),R.drawable.com_facebook_profile_picture_blank_square)));
        }
    }

    private void mostrarDialogOpciones() {
        final CharSequence[] opciones = {"Tomar Foto", "Elegir de Galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Elige una Opción");
        builder.setItems(opciones, (dialogInterface, i) -> {
            if (opciones[i].equals("Tomar Foto")) {
                abriCamara();
            } else {
                if (opciones[i].equals("Elegir de Galeria")) {
                    Intent intent=new Intent(Intent.ACTION_PICK);
                    intent.setType("image/");
                    startActivityForResult(Intent.createChooser(intent, "Seleccione"), COD_SELECCIONA);
                } else {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private void abriCamara() {
        File miFile = new File(Environment.getExternalStorageDirectory(), GeneralMethod.getDirectorioImagen());
        boolean isCreada = miFile.exists();

        if (!isCreada) {
            isCreada = miFile.mkdirs();
        }
        if (isCreada) {
            Long consecutivo = System.currentTimeMillis() / 1000;
            String nombre = consecutivo.toString() + ".jpg";

            pathCaptureShop = Environment.getExternalStorageDirectory() + File.separator + GeneralMethod.getDirectorioImagen()
                    + File.separator + nombre;//indicamos la ruta de almacenamiento

            File fileImagen = new File(pathCaptureShop);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));

            ////
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String authorities = getActivity().getPackageName() + ".provider";
                Uri imageUri = FileProvider.getUriForFile(getActivity(), authorities, fileImagen);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(fileImagen));
            }
            startActivityForResult(intent, COD_FOTO);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){//el dos representa los 2 permisos
                //GeneralMethod.showSnackback("Gracias por aceptar los permisos..!",mView,mActivity);
                GeneralMethod.mostrarDialogOpciones(this.getActivity());
            }
        }

    }


    private void RegistrarMarcadorDeTienda(Tienda mTienda){
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setMessage("Registrando tienda...");
        progressDialog.show();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference currentUserDB = mDatabase.child(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid());

        if(!tipoDeFoto.equals("VACIO")) {
            storageIMG(currentUserDB,mTienda,mDatabase,mTienda.getIdMarcador());
        }
        else{

            SubirRealtimeDatabase(currentUserDB,mTienda,mDatabase,mTienda.getIdMarcador());
            mDatabase.child("Usuarios").child(Objects.requireNonNull(currentUserDB.getKey())).child("Marcadores").child("Shop").child(mTienda.getIdMarcador()).child("imagen").setValue(defautMarkerShop);
        }


    }

    private void SubirRealtimeDatabase(final DatabaseReference currentUserDB, final Tienda mTienda, final DatabaseReference mDatabase, final String nombreAleatorio){
        mDatabase.child("Usuarios").child(Objects.requireNonNull(currentUserDB.getKey())).child("Marcadores").child("Shop").child(nombreAleatorio).setValue(mTienda);

        CreateMarkers(new LatLng(Double.valueOf(mTienda.getLatitud()),Double.valueOf(mTienda.getLongitud())),mGoogleMap,mTienda);
        progressDialog.dismiss();
    }
    private void storageIMG(final DatabaseReference currentUserDB, final Tienda mTienda, final DatabaseReference mDatabase, final String nombreAleatorio ){
        final StorageReference mStorageImgMarkerShop = mStorageReference.child("Imagenes").child("Marcadores").child("Shop").child(GeneralMethod.getRandomString());
        mStorageImgMarkerShop.putFile(mUriTiendaMarcador).addOnSuccessListener(this.getActivity(), taskSnapshot -> {
            SubirRealtimeDatabase(currentUserDB,mTienda,mDatabase,nombreAleatorio);
            mDatabase.child("Usuarios").child(Objects.requireNonNull(currentUserDB.getKey())).child("Marcadores").child("Shop").child(nombreAleatorio).child("imagen").setValue(mStorageImgMarkerShop.getDownloadUrl().getResult()).toString().replace("\"", "");
        }).addOnFailureListener(this.getActivity(), e -> {
            //GeneralMethod.showSnackback("Lo sentimos, pero ocurrio un incoveniente",,MainActivity.this);
        });

    }
}
