package actividades;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.whereismypet.whereismypet.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


import Modelo.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;
import dialogsFragments.DialogMarkerPet;
import finalClass.GeneralMethod;
import misclases.CustomInfoWindowAdapter;
import Modelo.Mascota;
import misclases.VolleySingleton;


import static android.widget.Toast.LENGTH_SHORT;



public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,AppCompatCallback,LocationListener,
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, View.OnClickListener {

    GoogleMap googleMap;
    GoogleApiClient apiClient = null;
    LatLng LocalizacionCoord;
    LocationManager locationManager;
    AlertDialog alertGPS = null;
    Location Localizacion;
    DrawerLayout drawer;
    CircleImageView mImgFotoPerfil, imgPetsMarker;
    private static final String URL_MARCADORES = "http://www.secsanluis.com.ar/servicios/varios/wimp/W_ListarMarcadores.php";
    static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private ArrayList<Mascota> listaMarcador;
    static final int PETICION_PERMISO_LOCALIZACION = 0;
    //FIREBASE
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mUserFireBase;
    private String UserId;
    private Usuario.UsuarioPublico mUserPublic;
    //GOOGLE
    private GoogleSignInClient mGoogleSignInClient;


    //images d perfil quienes somos


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        NavigationView navigationView = findViewById(R.id.nav_view);
        drawer = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ConectarAPI();
        //FIREBASE
        mFirebaseAuth = FirebaseAuth.getInstance();
        mUserFireBase = mFirebaseAuth.getCurrentUser();
        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
        UserId = Objects.requireNonNull(mUserFireBase).getUid();
        EscuchandoEstadoDeAutenticacion();
        LoginGoogle();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            assert locationManager != null;
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                GpsDesactivado();
            } else {
                MyLocation();
            }
        } catch (Exception ignored) { }
        mImgFotoPerfil = (navigationView.getHeaderView(0)).findViewById(R.id.imgPerfilMenu);
        mImgFotoPerfil.setOnClickListener(this);
        ObtenerDatosPerfil();

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

    //----------------------------FIREBASE-----------------------------------------
    private void EscuchandoEstadoDeAutenticacion() {

        FirebaseAuth.AuthStateListener mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUserFireBase = firebaseAuth.getCurrentUser();
                UserId = Objects.requireNonNull(mUserFireBase).getUid();
                if (AccessToken.getCurrentAccessToken() != null) {
                    Toast.makeText(MainActivity.this, "Faceboook", LENGTH_SHORT).show();
                    VolverAlLogin();
                }
                if (mUserFireBase != null) {
                    VolverAlLogin();
                }
                /*if(mUserFireBase!=null)
                {
                    user = new Usuario();
                    //user.setNombre(userFireBase.getDisplayName());
                    user.setEmail(mUserFireBase.getEmail());
                }*/
            }
        };
    }

    private void LoginGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signOutGoogle() {
        // Firebase sign out
        mFirebaseAuth.signOut();
        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        VolverAlLogin();
                    }
                });
    }

    private void singOutFabebook() {
        mFirebaseAuth.signOut();
        LoginManager.getInstance().logOut();
        VolverAlLogin();

    }

    private void VolverAlLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    //--------------------------DATOS PERFIL FIREBASE------------------------------
    public void ObtenerDatosPerfil() {
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Usuarios").child(Objects.requireNonNull(mDatabase.child(UserId).getKey())).child("Datos Personales").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserPublic = dataSnapshot.getValue(Usuario.UsuarioPublico.class);
                assert mUserPublic != null;

                if (mUserFireBase.getProviderData().get(1).getProviderId().equals("password")){
                    GeneralMethod.GlideUrl(MainActivity.this,mUserPublic.getImagen(),mImgFotoPerfil);
                }
                else {
                    GeneralMethod.GlideUrl(MainActivity.this,Objects.requireNonNull(mUserFireBase.getPhotoUrl()).toString(),mImgFotoPerfil);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    //----------------------------LocationListener----------------------------------
    @Override
    public void onLocationChanged(Location location) {
        LocalizacionCoord = new LatLng(location.getLatitude(), location.getLongitude());
        ActualizarCamara(LocalizacionCoord);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

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
        } catch (Exception ignored) {
        }
        googleMap.setMyLocationEnabled(true);

    }

    private void ActualizarCamara(LatLng COORDS) {
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
        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        if (id == R.id.nav_salir) {

            if (mUserFireBase.getProviderData().get(0).equals("facebook.com")) {
                singOutFabebook();
            } else if (mUserFireBase.getProviderData().get(0).equals("google.com")) {
                // mFirebaseAuth.signOut();
                //signOutEmailPassword();
                signOutGoogle();


            } else {
                VolverAlLogin();
            }

        } else if (id == R.id.nav_ajustes) {
            instaciarAjustes();
        } else if (id == R.id.nav_colaboradores) {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.oferta.educacion.ulp&hl=es");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (id == R.id.nav_mascota) {
            instaciarMisMascotas();
        } else if (id == R.id.nav_premium) {
            instaciarPremium();
        } else if (id == R.id.nav_terminosycondiciones) {
            instaciarTerminos();
        } else if (id == R.id.nav_nosotros) {
            instaciarNosotros();
        } else if (id == R.id.nav_compartir) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "https://wwww.facebook.com/wimp.ulp.5");
            startActivity(Intent.createChooser(intent, "COMPARTIR"));
        }

        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //---------------------------------CARGA DE MARCADORES EN MAPA---------------------------------------------------------
    private void ConsultarMarcadores() {
        listaMarcador = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_MARCADORES, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray listaMascotas = response.getJSONArray("pet");
                            for (int indice = 0; indice < listaMascotas.length(); indice++) {
                                JSONObject json_data = listaMascotas.getJSONObject(indice);

                                Mascota markersPets = new Mascota();
                                //markersPets.setId_Marcador(json_data.getInt("idMarcador"));
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
                        } catch (JSONException ignored) {
                        }
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

    private void CargarMarcadores(final Mascota myMarker) {
        LatLng latLng = new LatLng(Double.valueOf(myMarker.getLatitud()), Double.valueOf(myMarker.getLongitud()));
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                //.title(String.valueOf(myMarker.getId_Marcador()))
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
                for (Mascota m : listaMarcador) {
                    //if (m.getId_Marcador() == Integer.valueOf(marker.getTitle())) {
                    googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(LayoutInflater.from(getApplicationContext()), m, MainActivity.this));
                    /*Picasso.get()
                            .load(m.getFoto())
                            .error(R.drawable.huella_mascota)
                            .fit()
                            .centerInside()
                            .into(imgPetsMarker);*/
                }
                //}
                return false;
            }
        });
    }

    //-----------------------------------PREFERENCIAS---------------------------------------------------------------------------
    public void SaveStyle(String key, String value) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String LoadStyle() {
        String claveMapa;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        claveMapa = sharedPreferences.getString("estilo_mapa", "default");
        return claveMapa;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgPerfilMenu: {
                instaciarDialogoActualizar();
            }
            break;
        }
    }

    //-----------------------------------CLASE INTERNA MAPA-------------------------------------------------

    private class Mapa implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap mapa) {
            googleMap = mapa;
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng point) {
                }
            });
            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onMapLongClick(LatLng latLng) {
                    instanciarDialogoMarcadorMascota(googleMap, latLng);
                }
            });
        }
    }
    //-------------------------------CLASE INTERNA DIALOG MARCADOR MASCOTA--------------MEJORADO CON FIREBASE--------------------------------------------------


    private void instanciarDialogoMarcadorMascota(GoogleMap map, LatLng latLng) {
        DialogMarkerPet markerPet = new DialogMarkerPet()
                .setLatLng(latLng)
                .setGoogleMap(googleMap)
                .setmActivity(MainActivity.this)
                .setView(drawer);
        markerPet.setCancelable(false);
        markerPet.show(getFragmentManager(), "AJUSTES");

    }

    //------------------------------DIALOG AJUSTE----------------------------
    @SuppressLint("ValidFragment")
    private class AjusteDialog extends DialogFragment implements View.OnClickListener {
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
                }
                break;
                case R.id.ivContactar_config: {
                    ContactarSoporte();
                }
                default:
                    break;
            }
        }
    }

    private void instaciarAjustes() {
        AjusteDialog dialog = new AjusteDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo

    }

    private void ContactarSoporte() {
        String[] TO = {"whereismypetulp@gmail.com"};
        String[] CC = {""};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setDataAndType(Uri.parse("mailto:"), "text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Asunto");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Escribe aquí tu mensaje");
        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar email..."));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "No tienes ningun programa para enviar email instalado.", Toast.LENGTH_SHORT).show();
        }
    }

    //----------------------DIALOG AJUSTE MAPAS----------------------------------------------
    @SuppressLint("ValidFragment")
    class SettingMapsDialog extends DialogFragment implements View.OnClickListener {
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
        SettingMapsDialog dialog = new SettingMapsDialog();  //Instanciamos la clase con el dialogo
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

    private void instaciarMisMascotas() {
        MisMascotasDialog dialog = new MisMascotasDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String ObtenerDireccion(Double lat, Double lng) {
        List<Address> direcciones = null;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = null;
        try {
            direcciones = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert direcciones != null;
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
            builder.setNegativeButton("cerrar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            return builder.create();
        }
    }

    private void instaciarTerminos() {
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

    private void instaciarNosotros() {
        NosotrosDialog dialog = new NosotrosDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES");// Mostramos el dialogo
    }

    // ------------------------ DIALOG PREMIUM-----------------------------------------
    @SuppressLint("ValidFragment")
    private class PremiumDialog extends DialogFragment {
        private ProgressBar mProgressBar;
        private View mRegularLayout;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_premium, null);
            mProgressBar = content.findViewById(R.id.progressBar);
            mRegularLayout = content.findViewById(R.id.regularLayout);

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog()
                    .build());

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

    private void instaciarPremium() {
        PremiumDialog dialog = new PremiumDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "PREMIUM");// Mostramos el dialogo
    }


    // -------------------------------- Actualizar Datos De Perfil..........................................
    @SuppressLint("ValidFragment")
    private class DialogActualizar extends DialogFragment implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_modificar_datos, null);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dismiss();
                    }
                    return false;
                }
            });
            CargarDatosPerfil(content);
            return builder.create();
        }

        @Override
        public void onClick(View v) {

        }


        private void CargarDatosPerfil(View view) {
            final TextView nombreActualizar = view.findViewById(R.id.nombreActualizar),
                    apellidoActualizar = view.findViewById(R.id.apellidoActualizar),
                    emailActualziar = view.findViewById(R.id.emailActualizar);
            final CircleImageView imgActualizar = view.findViewById(R.id.imgPerfilDBActualizar);

            if (mUserFireBase.getProviderData().get(1).getProviderId().equals("password")) {
                nombreActualizar.setText(mUserPublic.getNombre());
                apellidoActualizar.setText(mUserPublic.getApellido());
                emailActualziar.setText(mUserFireBase.getEmail());
                GeneralMethod.GlideUrl(this.getActivity(), mUserPublic.getImagen(), imgActualizar);
            } else {
                nombreActualizar.setText(mUserFireBase.getDisplayName());
                emailActualziar.setText(mUserFireBase.getEmail());
                GeneralMethod.GlideUrl(this.getActivity(),Objects.requireNonNull(mUserFireBase.getPhotoUrl()).toString(),imgActualizar);
            }
        }

        public void sendPasswordReset() {
            // [START send_password_reset]
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String emailAddressNuevo = "user@example.com";

            mFirebaseAuth.sendPasswordResetEmail(emailAddressNuevo)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email sent.");
                            }
                        }
                    });
            // [END send_password_reset]
        }

        public void deleteUser() {
            // [START delete_user]
            mUserFireBase.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User account deleted.");
                            }
                        }
                    });
            // [END delete_user]
        }

        public void updateProfile() {
            // [START update_profile]
            /*UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName("Jane Q. User")
                    .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                    .build();

            mUserFireBase.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User profile updated.");
                            }
                        }
                    });*/
            // [END update_profile]
        }

        public void updateEmail() {
            // [START update_email]
            mUserFireBase.updateEmail("user@example.com")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User email address updated.");
                            }
                        }
                    });
            // [END update_email]
        }
    }

        private void instaciarDialogoActualizar() {
            DialogActualizar dialog = new DialogActualizar();  //Instanciamos la clase con el dialogo
            dialog.setCancelable(false);
            dialog.show(getFragmentManager(), "ACTUALIZAR");// Mostramos el dialogo

        }
}

