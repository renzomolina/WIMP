package actividades;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mercadopago.android.px.internal.view.MPButton;
import com.mercadopago.android.px.tracking.internal.TrackingEnvironments;
import com.whereismypet.whereismypet.R;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;
import mercadopago.utils.ExamplesUtils;
import misclases.CustomInfoWindowAdapter;
import misclases.Marcador;
import misclases.VolleySingleton;
import misclases.WebServiceJSON;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static mercadopago.utils.ExamplesUtils.resolveCheckoutResult;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,AppCompatCallback,LocationListener,
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    GoogleMap googleMap;
    GoogleApiClient apiClient = null;
    LatLng LocalizacionCoord;
    SharedPreferences sharedPreferences2;
    LocationManager locationManager;
    AlertDialog alertGPS = null;
    Location Localizacion;
    DrawerLayout drawer;
    CircleImageView perfil,imgPetsMarker;
    private static final String URL_MARCADORES = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_ListarMarcadores.php";
    private String  UrlConsultarUsuario ="http://www.secsanluis.com.ar/servicios/varios/wimp/W_ConsultarCliente.php";
    static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private ArrayList<Marcador> listaMarcador;



    static final int PETICION_PERMISO_LOCALIZACION = 0;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        drawer = findViewById(R.id.drawer_layout);


        ConectarAPI();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            assert locationManager != null;
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                GpsDesactivado();
            }
            else
            {
                MyLocation();
            }
        }catch (Exception ignored) { }

        WebServiceJSON.ConsultarPerfil(this,this);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PETICION_PERMISO_LOCALIZACION);
        } else {
            MyLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Se interrumpio la conexion con Google Play Services\"", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error al conectar con Google Play Services", Toast.LENGTH_SHORT).show();
    }

    //----------------------------LocationListener----------------------------------
    @Override
    public void onLocationChanged(Location location) {
        LocalizacionCoord = new LatLng(location.getLatitude(), location.getLongitude());
        ActualizarCamara(LocalizacionCoord);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onProviderDisabled(String provider) { }
//---------------------------------------------------------------------------------------------------------------
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void ConectarAPI() {
        apiClient = new GoogleApiClient
            .Builder(this)
            .enableAutoManage(this, this)
            .addConnectionCallbacks(this)
            .addApi(LocationServices.API)
            .build();
       ((SupportMapFragment) Objects.requireNonNull(getSupportFragmentManager().findFragmentById(R.id.mapa))).getMapAsync(new Mapa());
    }

    public void MyLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Localizacion = LocationServices.FusedLocationApi.getLastLocation(apiClient);
            LocalizacionCoord = new LatLng(Localizacion.getLatitude(), Localizacion.getLongitude());
            ActualizarCamara(LocalizacionCoord);
        } catch (Exception ex) { }
        googleMap.setMyLocationEnabled(true);

    }

    private void ActualizarCamara(LatLng COORDS) {
        if(MyPosition()) {
            CameraPosition CamPos = new CameraPosition
                    .Builder()
                    .target(COORDS)
                    .zoom(16)
                    .bearing(-10)
                    .tilt(0)
                    .build();
            CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(CamPos);
            googleMap.animateCamera(camUpdate);
        }
    }

    private void GpsDesactivado() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        alertGPS = builder.create();
        alertGPS.show();
    }

    //-----------------------------------MENU LATERAL-----------------------------------------------------

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.nav_salir){
            Intent intent = new Intent(this,LoginActivity.class);
            LoginManager.getInstance().logOut();
            SharedPreferences sh = getSharedPreferences("Mis preferencias",Context.MODE_PRIVATE);
            sh.edit().clear().apply();
            startActivity(intent);
            finish();

        }
        else if(id == R.id.nav_ajustes) {
            instaciarAjustes();
        }
        else if(id == R.id.nav_colaboradores){
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.oferta.educacion.ulp&hl=es");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }else if(id == R.id.nav_mascota) {
            instaciarMisMascotas();
        }
        else if(id == R.id.nav_premium)
        {
            instaciarPremium();
        }

        else if(id == R.id.nav_terminosycondiciones) {
            instaciarTerminos();
        }
        else if(id == R.id.nav_nosotros)
        {
            instaciarNosotros();
        }
        else if(id == R.id.nav_compartir){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,"https://wwww.facebook.com/wimp.ulp.5");
            startActivity(Intent.createChooser(intent,"Compartir"));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /*public String getFromSharedPreferences(String key){
        SharedPreferences sharedPreferences = getSharedPreferences("Mis preferencias", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key,"");
    }
*/

    //---------------------------------CARGA DE MARCADORES EN MAPA---------------------------------------------------------
    private void ConsultarMarcadores(){
        listaMarcador = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_MARCADORES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray listaMascotas = response.getJSONArray("pet");
                            for(int indice = 0; indice < listaMascotas.length(); indice++) {
                                JSONObject json_data = listaMascotas.getJSONObject(indice);

                                Marcador markersPets = new Marcador();
                                markersPets.setId_Marcador(json_data.getInt("idMarcador"));
                                markersPets.setNombre(json_data.getString("nombre"));
                                markersPets.setDescripcion(json_data.getString("descripcion"));
                                markersPets.setLatitud(json_data.getString("latitud"));
                                markersPets.setLongitud(json_data.getString("longitud"));
                                markersPets.setTipo(json_data.getString("tipo"));
                                markersPets.setCreador(json_data.getString("creador"));
                                markersPets.setFoto(json_data.getString("foto"));
                                listaMarcador.add(markersPets);
                                CargarMarcadores(markersPets);
                            }
                            /*JSONArray listaTiendas = response.getJSONArray("shop");
                            for(int indice =0; indice < listaTiendas.length(); indice++) {
                                JSONObject json_data = listaTiendas.getJSONObject(indice);
                                String imgPerfil = json_data.getString("imagen");
                            }*/
                        } catch (JSONException ignored) { }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Toast.makeText(getApplicationContext(), volleyError.toString(), Toast.LENGTH_LONG).show();
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(this).addToRequestQueue(jsonObjectRequest);
    }

    private void CargarMarcadores(final Marcador myMarker){
        LatLng latLng = new LatLng(Double.valueOf(myMarker.getLatitud()),Double.valueOf(myMarker.getLongitud()));
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(String.valueOf(myMarker.getId_Marcador()))
                .snippet(myMarker.getDescripcion())
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pet_markers)));

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        ConsultarMarcadores();
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                imgPetsMarker = findViewById(R.id.imgMarcadorMascota);
                for (Marcador m : listaMarcador) {
                    if (m.getId_Marcador() == Integer.valueOf(marker.getTitle())) {
                        googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(LayoutInflater.from(getApplicationContext()), m,MainActivity.this));
                    /*Picasso.get()
                            .load(m.getFoto())
                            .error(R.drawable.huella_mascota)
                            .fit()
                            .centerInside()
                            .into(imgPetsMarker);*/
                    }
                }
                return false;
            }
        });
        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                InstanciarDialogoComentario();
            }
        });
    }

    //-----------------------------------CLASE INTERNA MAPA-------------------------------------------------

    private class Mapa implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap mapa) {
            googleMap = mapa;
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng point) { }
            });
            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onMapLongClick(LatLng latLng) {
                    instanciarDialogoMarcadorMascota(googleMap,latLng);
                }
            });
        }
    }


    //-----------------------------------PREFERENCIAS---------------------------------------------------------------------------
    public void SaveStyle(String key, String value){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void SaveStyle2(boolean myPosition){
        sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor2.putBoolean("position",myPosition);
        editor2.apply();
    }
    public String LoadStyle(){
        String claveMapa;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        claveMapa = sharedPreferences.getString("estilo_mapa", "default");

        return claveMapa;
    }
    private boolean MyPosition(){
        sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return  sharedPreferences2.getBoolean("position",true);
    }

    //-------------------------------CLASE INTERNA DIALOG MARCADOR MASCOTA----------------------------------------------------------------
    @SuppressLint("ValidFragment")
    public class DialogMascota extends DialogFragment implements View.OnClickListener {
        private LatLng latLng;
        private EditText nombre, descripcion;
        private TextView titulo;
        private CircleImageView imgPetsDialog;
        private LinearLayout layout;
        private GoogleMap map;
        private static final String CARPETA_PRINCIPAL = "WIMP/";//directorio principal
        private static final String CARPETA_IMAGEN = "imagenes";//carpeta donde se guardan las fotos
        private static final String DIRECTORIO_IMAGEN = CARPETA_PRINCIPAL + CARPETA_IMAGEN;//ruta carpeta de directorios
        private static final int MIS_PERMISOS = 100;
        private static final int COD_SELECCIONA = 10;
        private static final int COD_FOTO = 20;
        private boolean withImage = false;
        private int COD_OPCION;
        private File fileImagen;
        private String pathCapturePets;
        private Uri pathSelectPets;
        private Bitmap bitmap;
        private Marcador marcador;


        public DialogMascota(GoogleMap map, LatLng latLng) {
            this.latLng = latLng;
            this.map = map;
            marcador = new Marcador();
        }

        @NonNull
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
            @SuppressLint("InflateParams")
            View content = inflater.inflate(R.layout.dialog_pets, null);

            imgPetsDialog = content.findViewById(R.id.imgMascota);
            titulo = content.findViewById(R.id.Mascota);
            nombre = content.findViewById(R.id.input_nombre);
            descripcion = content.findViewById(R.id.input_descripcion);
            layout = content.findViewById(R.id.ContenedorEditText);
            imgPetsDialog.setOnClickListener(this);


            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setPositiveButton("guardar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                    marcador.setNombre(nombre.getText().toString());
                    marcador.setDescripcion(descripcion.getText().toString());
                    marcador.setCreador(WebServiceJSON.getFromSharedPreferences("correo", MainActivity.this));
                    marcador.setTipo("pet");
                    marcador.setLatitud(String.valueOf(latLng.latitude));
                    marcador.setLongitud(String.valueOf(latLng.longitude));
                    switch (COD_OPCION) {
                        case COD_SELECCIONA: {
                            marcador.CheckInPets(MainActivity.this, getPath(pathSelectPets), COD_OPCION, withImage);

                        }
                        break;
                        case COD_FOTO: {
                            marcador.CheckInPets(MainActivity.this, pathCapturePets, COD_OPCION, withImage);
                        }
                        break;
                        default:
                            break;
                    }
                    CreateMarkers(latLng);
                    //CrearRadio(map,latLng);

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

        private void CreateMarkers(LatLng latLng) {
            googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(LayoutInflater.from(getApplicationContext()), marcador, MainActivity.this));
            googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(String.valueOf(marcador.getId_Marcador()))
                    .snippet(marcador.getDescripcion())
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pet_markers)));
        }

        public String getPath(Uri uri) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            String document_id = cursor.getString(0);
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
            cursor.close();

            cursor = getContentResolver().query(
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            return path;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imgMascota: {
                    if (solicitaPermisosVersionesSuperiores()) {
                        mostrarDialogOpciones();
                    }
                }
                break;
                default:
                    break;
            }

        }

        private void mostrarDialogOpciones() {
            final CharSequence[] opciones = {"Tomar Foto", "Elegir de Galeria", "Cancelar"};
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Elige una Opción");
            builder.setItems(opciones, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (opciones[i].equals("Tomar Foto")) {
                        abriCamara();
                    } else {
                        if (opciones[i].equals("Elegir de Galeria")) {
                            Intent intent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/");
                            startActivityForResult(intent.createChooser(intent, "Seleccione"), COD_SELECCIONA);
                        } else {
                            dialogInterface.dismiss();
                        }
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

                fileImagen = new File(pathCapturePets);

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

        private Bitmap redimensionarImagen(Bitmap bitmap, float anchoNuevo, float altoNuevo) {
            if (bitmap != null) {
                int ancho = bitmap.getWidth();
                int alto = bitmap.getHeight();

                if (ancho > anchoNuevo || alto > altoNuevo) {
                    float escalaAncho = anchoNuevo / ancho;
                    float escalaAlto = altoNuevo / alto;

                    Matrix matrix = new Matrix();
                    matrix.postScale(escalaAncho, escalaAlto);

                    return Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false);
                }
            }
            return bitmap;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            switch (requestCode) {
                case COD_SELECCIONA:
                    pathSelectPets = data.getData();
                    imgPetsDialog.setImageURI(pathSelectPets);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), pathSelectPets);
                        imgPetsDialog.setImageBitmap(bitmap);
                        marcador.setFoto(getPath(pathSelectPets));
                        COD_OPCION = COD_SELECCIONA;
                        withImage = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case COD_FOTO:
                    MediaScannerConnection.scanFile(getActivity(), new String[]{pathCapturePets}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("Path", "" + path);
                                }
                            });

                    bitmap = BitmapFactory.decodeFile(pathCapturePets);
                    imgPetsDialog.setImageBitmap(bitmap);
                    marcador.setFoto(pathCapturePets);
                    COD_OPCION = COD_FOTO;
                    withImage = true;
                    break;
            }
            if (bitmap == null) {
                imgPetsDialog.setImageDrawable(getResources().getDrawable(R.drawable.huella_mascota));
            } else
                bitmap = redimensionarImagen(bitmap, 500, 500);
        }


        //PERMISOS
        private boolean solicitaPermisosVersionesSuperiores() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return true;
            }

            if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE) || (shouldShowRequestPermissionRationale(CAMERA)))) {
                cargarDialogoRecomendacion();
            } else {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
            }

            return false;
        }

        private void cargarDialogoRecomendacion() {
            AlertDialog.Builder dialogo = new AlertDialog.Builder(getApplicationContext());
            dialogo.setTitle("Permisos Desactivados");
            dialogo.setMessage("Debe aceptar los permisos para el correcto funcionamiento de la App");

            dialogo.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                    }
                }
            });
            dialogo.show();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == MIS_PERMISOS) {
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {//el dos representa los 2 permisos
                    Toast.makeText(getApplicationContext(), "Permisos aceptados", Toast.LENGTH_SHORT);
                }
            } else {
                solicitarPermisosManual();
            }
        }

        private void solicitarPermisosManual() {
            final CharSequence[] opciones = {"si", "no"};
            final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(getApplicationContext());//estamos en fragment
            alertOpciones.setTitle("¿Desea configurar los permisos de forma manual?");
            alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (opciones[i].equals("si")) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getApplicationContext().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), "Los permisos no fueron aceptados", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }
                }
            });
            alertOpciones.show();
        }
    }
    private void instanciarDialogoMarcadorMascota(GoogleMap map,LatLng latLng) {
        DialogMascota dialog = new DialogMascota(map,latLng); //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        assert dialog.getFragmentManager() != null;
        dialog.show(getFragmentManager(), "NEWPOSITION");// Mostramos el dialogo
    }
    //------------------------------DIALOG AJUSTE----------------------------
    @SuppressLint("ValidFragment")
    private class AjusteDialog extends DialogFragment implements View.OnClickListener{
        ImageView mapa_config, soporte;
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_settings, null);

            mapa_config = content.findViewById(R.id.ivConfigurar_config);
            mapa_config.setOnClickListener(this);
            soporte = content.findViewById(R.id.ivContactar_config);
            soporte.setOnClickListener(this);


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


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ivConfigurar_config: {
                    instanciarMapas();
                }break;
                case R.id.ivContactar_config:{
                    ContactarSoporte();
                }
                default:break;
            }
        }
    }
    private void instaciarAjustes(){
        AjusteDialog dialog = new AjusteDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo

    }
    private void ContactarSoporte() {
        String[] TO = {"whereismypetulp@gmail.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
// Esto podrás modificarlo si quieres, el asunto y el cuerpo del mensaje
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Asunto");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Escribe aquí tu mensaje");
        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar email..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "No tienes clientes de email instalados.", Toast.LENGTH_SHORT).show();
        }
    }


    //----------------------DIALOG MAPAS----------------------------------------------
    @SuppressLint("ValidFragment")
    class MapasDialog extends DialogFragment implements View.OnClickListener {
        ImageView mapa_config;
        ImageButton fondoBlue, fondoBlack, fondoCandy, fondoVintage;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_setting_maps, null);

            fondoBlue = content.findViewById(R.id.btAzul);
            fondoBlue.setOnClickListener(this);
            fondoBlack = content.findViewById(R.id.btBlack);
            fondoBlack.setOnClickListener(this);
            fondoCandy = content.findViewById(R.id.btCandy);
            fondoCandy.setOnClickListener(this);
            fondoVintage = content.findViewById(R.id.btVintage);
            fondoVintage.setOnClickListener(this);

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

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btAzul: {
                    try {
                        boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.json_blue));
                        SaveStyle("estilo_mapa", "json_blue");
                        if (!success) {
                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }
                    Toast.makeText(MainActivity.this, "se cambio el estilo", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.btBlack: {
                    try {
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.json_black));
                        SaveStyle("estilo_mapa", "json_black");
                        if (!success) {
                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }
                    Toast.makeText(MainActivity.this, "se cambio el estilo", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.btCandy: {
                    try {
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.json_candy));
                        SaveStyle("estilo_mapa", "json_candy");
                        if (!success) {
                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }
                    Toast.makeText(MainActivity.this, "se cambio el estilo", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.btVintage: {
                    try {
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.json_vintage));
                        SaveStyle("estilo_mapa", "json_vintage");
                        if (!success) {
                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }
                    Toast.makeText(MainActivity.this, "se cambio el estilo", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    }
    private void instanciarMapas() {
        MapasDialog dialog = new MapasDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo

    }
// ------------------------ DIALOG MIS MASCOTAS-----------------------------------------
    @SuppressLint("ValidFragment")
    private class MisMascotasDialog extends DialogFragment {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_my_pets, null);
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
    }
    private void instaciarMisMascotas(){
        MisMascotasDialog dialog = new MisMascotasDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String ObtenerDireccion(Double lat, Double lng){
        List<Address> direcciones = null;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = null;
        try {
            direcciones = geocoder.getFromLocation(lat,lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!direcciones.isEmpty()) {
            Address DirCalle = direcciones.get(0);
            address = DirCalle.getAddressLine(0);
        }
        return address;
    }
    // ------------------------ DIALOG MIS TERMINOS-----------------------------------------
    @SuppressLint("ValidFragment")
    private class TerminosDialog extends DialogFragment {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_terms_and_conditions, null);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            return builder.create();
        }



    }
    private void instaciarTerminos(){
       TerminosDialog dialog = new TerminosDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo

    }
    // ------------------------ DIALOG NOSOTROS-----------------------------------------
    @SuppressLint("ValidFragment")
    private class NosotrosDialog extends DialogFragment {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
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



    }
    private void instaciarNosotros(){
        NosotrosDialog dialog = new NosotrosDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo

    }

    // ------------------------ DIALOG PREMIUM-----------------------------------------
    @SuppressLint("ValidFragment")
    private class PremiumDialog extends DialogFragment implements View.OnClickListener {
        private static final int REQUEST_CODE = 1;
        private ProgressBar mProgressBar;
        private View mRegularLayout;
        private MPButton continueSimpleCheckout;
        private static final int REQ_CODE_CHECKOUT = 1;
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_premium, null);
            mProgressBar = content.findViewById(R.id.progressBar);
            mRegularLayout = content.findViewById(R.id.regularLayout);
            continueSimpleCheckout = content.findViewById(R.id.continueButton);
            continueSimpleCheckout.setOnClickListener(this);


            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog()
                    .build());

            com.mercadopago.android.px.tracking.internal.Settings.setTrackingEnvironment(TrackingEnvironments.STAGING);

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

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.continueButton:{
                    ExamplesUtils.createBase().build().startPayment(MainActivity.this, REQUEST_CODE);
                }break;
            }
        }
        @Override
        public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
            resolveCheckoutResult(MainActivity.this, requestCode, resultCode, data, REQ_CODE_CHECKOUT);
            super.onActivityResult(requestCode, resultCode, data);
        }
        @Override
        public void onResume() {
            super.onResume();
            showRegularLayout();
            continueSimpleCheckout.setEnabled(true);
        }

        private void showRegularLayout() {
            mProgressBar.setVisibility(View.GONE);
            mRegularLayout.setVisibility(View.VISIBLE);
        }

    }
    private void instaciarPremium(){
        PremiumDialog dialog = new PremiumDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "PREMIUM");// Mostramos el dialogo
    }


    //-----------------------------------------------------DIALOGO COMENTARIO MASCOTA---------------------------------------------------------
    @SuppressLint("ValidFragment")
    private class DialogoComentario extends DialogFragment {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_comentario_mascota, null);
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
    }
    private void InstanciarDialogoComentario(){
        DialogoComentario dialog = new DialogoComentario();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "COMENTARIO");// Mostramos el dialogo

    }
}
