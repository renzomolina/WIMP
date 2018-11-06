package dialogsFragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.whereismypet.whereismypet.R;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import Modelo.Marcadores;
import Modelo.Mascota;
import finalClass.GeneralMethod;

@SuppressLint("ValidFragment")
public class DialogMarkerPet extends DialogFragment implements View.OnClickListener{
    //Componentes
    private EditText mNombreMascotaMarcador,mDescripcionMascotaMarcador;
    private ImageView mFotoMascotaMarcador;
    private String tipoDeFoto = "VACIO";
    //Firebase
    private FirebaseAuth mFirebaseAuth;
    private StorageReference mStorageReference;

    //Mapa
    private LatLng latLng;
    private GoogleMap map;
    private Mascota marcadorMacota;
    //Imagen
    private String pathCapturePets;
    private Uri mUriMascotaMarcador;
    //Carpeta imagen
    private static final String CARPETA_PRINCIPAL = "WIMP/";//directorio principal
    private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
    private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
    //Permisos
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    ProgressDialog progressDialog;
    public DialogMarkerPet(GoogleMap map, LatLng latLng) {
        this.latLng = latLng;
        this.map = map;
        marcadorMacota = new Mascota();
    }
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
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content);
        builder.setPositiveButton("GUARDAR", (dialog, id) -> {
            Marcadores mMascota = new Mascota()
                    .setIdMarcador(GeneralMethod.getRandomString())
                    .setNombre(mNombreMascotaMarcador.getText().toString())
                    .setDescripcion(mDescripcionMascotaMarcador.getText().toString())
                    .setLatitud(String.valueOf(latLng.latitude))
                    .setLongitud(String.valueOf(latLng.longitude));
            RegistrarMarcadorDeMascota((Mascota) mMascota);
        });
        builder.setNegativeButton("CANCELAR", (dialog, id) -> dialog.dismiss());
        return builder.create();
    }

    private void CreateMarkers(LatLng latLng,GoogleMap googleMap) {
       // googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(LayoutInflater.from(DialogMarkerPet.this.getActivity().getApplicationContext()), marcadorMacota, DialogMarkerPet.this.getActivity()));
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(String.valueOf(marcadorMacota.getNombre()))
                .snippet(marcadorMacota.getDescripcion())
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pet_markers)));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.imgMascota:{
                if (GeneralMethod.solicitaPermisosVersionesSuperiores(this.getActivity())) {
                    mostrarDialogOpciones();
                }
            }break;
        }
    }
    private void RegistrarMarcadorDeMascota(Mascota mMascota){
        progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setMessage("Registrando mascota perdida...");
        progressDialog.show();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference currentUserDB = mDatabase.child(Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).getUid());

        if(!tipoDeFoto.equals("VACIO")) {
            storageIMG(currentUserDB,mMascota,mDatabase,mMascota.getIdMarcador());
        }
        else{

            SubirRealtimeDatabase(currentUserDB,mMascota,mDatabase,mMascota.getIdMarcador());
            mDatabase.child("Usuarios").child(Objects.requireNonNull(currentUserDB.getKey())).child("Marcadores").child("Pet").child(mMascota.getIdMarcador()).child("imagen").setValue("defaultPet");
        }


    }

    private void SubirRealtimeDatabase(final DatabaseReference currentUserDB, final Mascota mMascota, final DatabaseReference mDatabase, final String nombreAleatorio){
        mDatabase.child("Usuarios").child(Objects.requireNonNull(currentUserDB.getKey())).child("Marcadores").child("Pet").child(nombreAleatorio).setValue(mMascota);

        CreateMarkers(new LatLng(Double.valueOf(mMascota.getLatitud()),Double.valueOf(mMascota.getLongitud())),map);
        progressDialog.dismiss();
    }
    private void storageIMG(final DatabaseReference currentUserDB, final Mascota mMascota, final DatabaseReference mDatabase, final String nombreAleatorio ){
        final StorageReference mStorageImgMarkerPet = mStorageReference.child("Imagenes").child("Marcadores").child("Pets").child(GeneralMethod.getRandomString());
        mStorageImgMarkerPet.putFile(mUriMascotaMarcador).addOnSuccessListener(this.getActivity(), taskSnapshot -> {
            SubirRealtimeDatabase(currentUserDB,mMascota,mDatabase,nombreAleatorio);
            mDatabase.child("Usuarios").child(Objects.requireNonNull(currentUserDB.getKey())).child("Marcadores").child("Pet").child(nombreAleatorio).child("imagen").setValue(taskSnapshot.getStorage().getDownloadUrl().toString());
        }).addOnFailureListener(this.getActivity(), e -> {
            //GeneralMethod.showSnackback("Lo sentimos, pero ocurrio un incoveniente",,MainActivity.this);
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case COD_SELECCIONA: {
                    // URI Camara
                    mUriMascotaMarcador = Objects.requireNonNull(data).getData();
                    tipoDeFoto = "SELECCIONA";
                    try {
                        mFotoMascotaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(MediaStore.Images.Media.getBitmap(DialogMarkerPet.this.getActivity().getContentResolver(), mUriMascotaMarcador)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }break;
                case COD_FOTO: {
                    MediaScannerConnection.scanFile(DialogMarkerPet.this.getActivity(), new String[]{pathCapturePets}, null,(path, uri) -> Log.i("Path", "" + path));
                    mFotoMascotaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeFile(pathCapturePets)));
                    mUriMascotaMarcador = Uri.fromFile(new File(pathCapturePets));
                    tipoDeFoto = "FOTO";
                } break;
            }
        } else {
            mFotoMascotaMarcador.setImageBitmap(GeneralMethod.getBitmapClip(BitmapFactory.decodeResource(getResources(),R.drawable.com_facebook_profile_picture_blank_square)));
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
        File miFile = new File(Environment.getExternalStorageDirectory(), DIRECTORIO_IMAGEN);
        boolean isCreada = miFile.exists();

        if (!isCreada) {
            isCreada = miFile.mkdirs();
        }
        if (isCreada) {
            Long consecutivo = System.currentTimeMillis() / 1000;
            String nombre = consecutivo.toString() + ".jpg";

            pathCapturePets = Environment.getExternalStorageDirectory() + File.separator + DIRECTORIO_IMAGEN
                    + File.separator + nombre;//indicamos la ruta de almacenamiento

            File fileImagen = new File(pathCapturePets);

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

}
