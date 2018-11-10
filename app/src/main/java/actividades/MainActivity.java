package actividades;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.whereismypet.whereismypet.R;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


import Modelo.Comentario;
import Modelo.Marcadores;
import Modelo.PreferenciasLogin;
import Modelo.Publicidad;
import Modelo.Tienda;
import Modelo.Usuario;
import adaptadores.AdaptadorComentarios;
import adaptadores.AdaptadorMascota;
import adaptadores.AdaptadorPublicidades;
import de.hdodenhof.circleimageview.CircleImageView;
import dialogsFragments.DialogMarkerPet;
import Modelo.Mascota;
import dialogsFragments.DialogShop;
import finalClass.GeneralMethod;
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
    private DatabaseReference mDatabase;
    private String UserId;
    private Usuario.UsuarioPublico mUserPublic;
    //GOOGLE
    private GoogleSignInClient mGoogleSignInClient;

    //STRING DE LOGUEO
    final String mFacebook = "facebook.com",
                mGoogle = "google.com",
                mPassword = "password";
    //string tipo marcador

    private String tipoMarcador = "pet";
    // FLOATING ACTION BUTTON
    FloatingActionButton mFloatingActionButtonMarkers;

    private ArrayList<Mascota>ListaMarcadoresMacota;
    private ArrayList<Tienda>ListaMarcadoresTienda;
    private ArrayList<Comentario>ListaComentario;
    private ArrayList<Publicidad>ListaPublicidad;

   //private ArrayList<Mascota>ListaMascotaSiguiendo;
    RecyclerView recyclerComentarios;
    RecyclerView recyclerPublicidad;
    RecyclerView recyclerFavoritos;

    private Map<String,ArrayList<Mascota>> mListaMarcadoresMascotas;
    private Map<String,ArrayList<Tienda>> mListaMarcadoresTiendas;
    private boolean fabExpanded = false;
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
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if(mUserFireBase!=null){
            UserId = mUserFireBase.getUid();
        }
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
        mFloatingActionButtonMarkers = findViewById(R.id.floatingMarkers);
        mFloatingActionButtonMarkers.setOnClickListener(this);
        /*layoutFabPet = findViewById(R.id.layoutFabPet);
        layoutFabShop = findViewById(R.id.layoutFabShop);
        */
        ObtenerDatosPerfil();

        escuchadorLinks();

    }

    private void escuchadorLinks() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("mLinks", "getDynamicLink:onFailure", e);
                    }
                });    }

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
        FirebaseAuth.AuthStateListener mAuthStateListener = firebaseAuth -> {
            mUserFireBase = firebaseAuth.getCurrentUser();
            UserId = Objects.requireNonNull(mUserFireBase).getUid();
            if (AccessToken.getCurrentAccessToken() != null) {
                Toast.makeText(MainActivity.this, "Faceboook", LENGTH_SHORT).show();
                VolverAlLogin(mFacebook);
            }
            if (mUserFireBase != null) {
                VolverAlLogin(mPassword);
            }
            /*if(mUserFireBase!=null)
            {
                user = new Usuario();
                //user.setNombre(userFireBase.getDisplayName());
                user.setEmail(mUserFireBase.getEmail());
            }*/
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
                task -> VolverAlLogin(mGoogle));
    }

    private void singOutFabebook() {
        mFirebaseAuth.signOut();
        LoginManager.getInstance().logOut();
        VolverAlLogin(mFacebook);
    }

    private void VolverAlLogin(final String cerrar_sesion) {

        GuardarTipoDeLogin(new PreferenciasLogin().setTipoSignOut(cerrar_sesion).setRecordarUsuario(false));
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }


    //--------------------------DATOS PERFIL FIREBASE------------------------------
    public void ObtenerDatosPerfil() {
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
        } catch (Exception ignored) { }
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
                .setPositiveButton("Si", (dialogInterface, i) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());
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
            switch (mUserFireBase.getProviderData().get(1).getProviderId()) {
                case "facebook.com":
                    singOutFabebook();
                    break;
                case "google.com":
                    signOutGoogle();
                    break;
                case "password":
                    VolverAlLogin(mPassword);
                    break;
                default:break;
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

    private void ConsultarMarcadores(){
        ListaMarcadoresMacota = new ArrayList<>();
        ListaMarcadoresTienda = new ArrayList<>();
        mListaMarcadoresMascotas = new HashMap<>();
        mListaMarcadoresTiendas = new HashMap<>();
        ListaComentario = new ArrayList<>();
        ListaPublicidad=new ArrayList<>();
        mDatabase.child("Usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot mDataSnapshot : dataSnapshot.getChildren()){
                        String keyUserPet = mDataSnapshot.getKey();
                        Iterator<DataSnapshot> items = mDataSnapshot.child("Marcadores").child("Pet").getChildren().iterator();
                        HashMap<String, Object> marker;
                        while(items.hasNext()){
                            DataSnapshot dt = items.next();
                            marker = (HashMap<String, Object>) dt.getValue();
                            if (marker != null) {
                                ListaMarcadoresMacota.add((Mascota) new Mascota()
                                        .setIdComentario(Objects.requireNonNull(marker.get("idComentario")).toString())
                                        .setIdMarcador(Objects.requireNonNull(marker.get("idMarcador")).toString())
                                        .setNombre(Objects.requireNonNull(marker.get("nombre")).toString())
                                        .setDescripcion(Objects.requireNonNull(marker.get("descripcion")).toString())
                                        .setImagen(Objects.requireNonNull(marker.get("imagen")).toString())
                                        .setLatitud(Objects.requireNonNull(marker.get("latitud")).toString())
                                        .setLongitud(Objects.requireNonNull(marker.get("longitud")).toString()));
                            }
                        }
                    if (keyUserPet != null) {
                        mListaMarcadoresMascotas.put(keyUserPet,ListaMarcadoresMacota);
                    }
                }
               for (DataSnapshot mDataSnapshot : dataSnapshot.getChildren()){
                   String keyUserShop = mDataSnapshot.getKey();
                    Iterator<DataSnapshot> items = mDataSnapshot.child("Marcadores").child("Shop").getChildren().iterator();
                    HashMap<String, Object> marker;
                    while(items.hasNext()){
                        DataSnapshot dt = items.next();
                        marker = (HashMap<String, Object>) dt.getValue();
                        if (marker != null) {
                            ListaMarcadoresTienda.add((Tienda) new Tienda()
                                    .setIdPublicidad(Objects.requireNonNull(marker.get("idPublicidad")).toString())
                                    .setDireccion(Objects.requireNonNull(marker.get("direccion")).toString())
                                    .setIdMarcador(Objects.requireNonNull(marker.get("idMarcador")).toString())
                                    .setNombre(Objects.requireNonNull(marker.get("nombre")).toString())
                                    .setDescripcion(Objects.requireNonNull(marker.get("descripcion")).toString())
                                    .setTelefono(Objects.requireNonNull(marker.get("telefono")).toString())
                                    .setImagen(Objects.requireNonNull(marker.get("imagen")).toString())
                                    .setLatitud(Objects.requireNonNull(marker.get("latitud")).toString())
                                    .setLongitud(Objects.requireNonNull(marker.get("longitud")).toString()));
                        }
                    }

                    if (keyUserShop != null) {
                        mListaMarcadoresTiendas.put(keyUserShop, ListaMarcadoresTienda);
                    }
               }


                for (Marcadores m : ListaMarcadoresMacota){
                    CargarMarcadoresMascota((Mascota)m);
                }
                for (Marcadores m : ListaMarcadoresTienda){
                    CargarMarcadoresTienda((Tienda)m);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // esto tengo que hacerlo cuando leo la imagen  url cambiarle las comillas
        //final String UrlFoto = Objects.requireNonNull(taskUri.getResult()).toString().replace("\"", "");

    }

    private void CargarMarcadoresMascota(final Mascota myMarker){
        LatLng latLng = new LatLng(Double.valueOf(myMarker.getLatitud()),Double.valueOf(myMarker.getLongitud()));
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(String.valueOf(myMarker.getIdMarcador()))
                .snippet(myMarker.getDescripcion())
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pet_markers)));

    }
    private void CargarMarcadoresTienda(final Tienda myMarker){
        LatLng latLng = new LatLng(Double.valueOf(myMarker.getLatitud()),Double.valueOf(myMarker.getLongitud()));
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(String.valueOf(myMarker.getIdMarcador()))
                .snippet(myMarker.getDescripcion())
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop_markers)));

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        ConsultarMarcadores();
        try {
            if(!LoadStyle().equals("default")) {
                int resID = getRaw(this,LoadStyle());
                boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, resID));
                if (!success) {
                    Log.e(TAG, "Style parsing failed.");
                }
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        googleMap.setOnMarkerClickListener(marker -> {
          for(Marcadores x :ListaMarcadoresTienda)
          {
              if(marker.getTitle().equals(x.getIdMarcador())) {
                  instaciarDialogoMostrarMarcadorTienda((Tienda) x);
              }

          }
          for(Marcadores x :ListaMarcadoresMacota) {
              if (marker.getTitle().equals(x.getIdMarcador())) {
                  instaciarDialogoMostrarMarcadorMascota((Mascota) x);

              }
          }
          return false;
        });
    }
    private int getRaw(Context c, String name) {
        return c.getResources().getIdentifier(name, "raw", c.getPackageName());
    }

    //-----------------------------------PREFERENCIAS---------------------------------------------------------------------------
    private void SaveStyle(String value){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("estilo_mapa", value);
        editor.apply();
    }

    private String LoadStyle(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString("estilo_mapa", "default");

    }

    private void GuardarTipoDeLogin(final PreferenciasLogin cerrar_sesion){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("type_sign_out",cerrar_sesion.getTipoSignOut());
        editor.putString("type_sign_in",cerrar_sesion.getTipoSignOut());
        editor.putBoolean("remember",cerrar_sesion.isRecordarUsuario());
        editor.apply();
    }
    private void GuardarNotificacionesCercanas(boolean cercanas)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notificaciones_cercanas",cercanas);
        editor.apply();
        Log.i("boolean Guardar", (editor.toString()));
    }
    private boolean LoadNotificationsCercanas()
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("notificaciones_cercanas", true);

    }
    private void GuardarNotificacionesOfertas(boolean ofertas){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("notificaciones_ofertas",ofertas);
        editor.apply();
    }
    private boolean LoadNotificationsOfertas()
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("notificaciones_ofertas", true);

    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgPerfilMenu: {
                instaciarDialogoActualizar();
            }
            break;
            case R.id.floatingMarkers:{
                if(fabExpanded){
                    //closeSubMenusFab();
                    tipoMarcador = "pet";
                    mFloatingActionButtonMarkers.setImageResource(R.drawable.mascota);
                    fabExpanded = false;
                    Context context = getApplicationContext();
                    Toast.makeText(context,"Marcador de Mascota" , Toast.LENGTH_SHORT).show();
                }
                else {
                    //openSubMenusFab();
                    tipoMarcador = "shop";
                    mFloatingActionButtonMarkers.setImageResource(R.drawable.tienda);
                    fabExpanded = true;
                    Context context = getApplicationContext();
                    Toast.makeText(context,"Marcador de Tienda" , Toast.LENGTH_SHORT).show();
                }
            }break;
        }
    }


    //-----------------------------------CLASE INTERNA MAPA-------------------------------------------------

    private class Mapa implements OnMapReadyCallback {
        @Override
        public void onMapReady(GoogleMap mapa) {
            googleMap = mapa;
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.setOnMapClickListener(point -> {//CLICK EN EL MAPA EVENTO ONCLICK CON LAMDA
            });
            googleMap.setOnMapLongClickListener(latLng -> {
                switch (tipoMarcador)
                {
                    case "pet":
                        instanciarDialogoMarcadorMascota(mapa,latLng);
                        break;
                    case "shop":
                         instanciarDialogoMarcadorTienda(mapa,latLng);
                        break;
                }
            });
        }
    }
    //-------------------------------CLASE INTERNA DIALOG MARCADOR MASCOTA--------------MEJORADO CON FIREBASE--------------------------------------------------

    private void instanciarDialogoMarcadorMascota(GoogleMap map,LatLng latLng) {
        DialogMarkerPet markerPet = new DialogMarkerPet(map,latLng);
        markerPet.setCancelable(false);
        markerPet.show(getFragmentManager(), "MARKER-PET");
    }

    private void instanciarDialogoMarcadorTienda(GoogleMap map,LatLng latLng){
        DialogShop markerShop = new DialogShop(map,latLng);
        markerShop.setCancelable(false);
        markerShop.show(getFragmentManager(),"MARKER-SHOP");
    }

    //------------------------------DIALOG AJUSTE----------------------------
    @SuppressLint("ValidFragment")
    private class AjusteDialog extends DialogFragment implements View.OnClickListener, Switch.OnCheckedChangeListener {
        ImageView mapa_config, soporte;
        Switch swCercanas;
        Switch swOfertas;

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_settings, null);

            mapa_config = content.findViewById(R.id.ivConfigurar_config);
            mapa_config.setOnClickListener(this);
            soporte = content.findViewById(R.id.ivContactar_config);
            soporte.setOnClickListener(this);

            swCercanas = content.findViewById(R.id.swNotificaciones_config);
            swOfertas = content.findViewById(R.id.swOfertas_config);
            swCercanas.setOnCheckedChangeListener(this);
            swOfertas.setOnCheckedChangeListener(this);
            swCercanas.setChecked(LoadNotificationsCercanas());
            swOfertas.setChecked(LoadNotificationsOfertas());

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
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
                }break;
                default:
                    break;
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            switch (compoundButton.getId()){
                case R.id.swOfertas_config:
                    GuardarNotificacionesOfertas(swOfertas.isChecked());
                    break;
                case R.id.swNotificaciones_config:
                    GuardarNotificacionesCercanas(swCercanas.isChecked());
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
        ImageView imagen1, imagen2, imagen3, imagen4;
        Switch swgoogle;
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
            imagen1=content.findViewById(R.id.checkmapa1);
                    imagen2=content.findViewById(R.id.checkmapa2);
                    imagen3=content.findViewById(R.id.checkmapa3);
                    imagen4=content.findViewById(R.id.checkmapa4);
                    swgoogle=content.findViewById(R.id.swEstiloGoogle);
                    swgoogle.setOnClickListener(this);

    //////----- PREFERECIAS------ MAPA SELECCIONADO----- CON UN SWICH Y LOADSTYLE
            if(!LoadStyle().equals("default")) {
                String style = LoadStyle();
                switch(style){
                    case "json_blue":{
                        seleccionarMapa(imagen1,imagen2, imagen3, imagen4, swgoogle);

                        break;
                    }
                    case "json_black":{
                        seleccionarMapa(imagen2,imagen1, imagen4, imagen3, swgoogle);
                        break;
                    }
                    case "json_candy":{
                        seleccionarMapa(imagen3,imagen1, imagen2, imagen4, swgoogle);
                        break;
                    }
                    case "json_vintage":{
                        seleccionarMapa(imagen4,imagen1, imagen2, imagen3, swgoogle);
                        break;
                    }
                }
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
            });
            return builder.create();
        }

        @SuppressLint("WrongConstant")
        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.btAzul: {
                    try {

                        seleccionarMapa(imagen1,imagen2, imagen3, imagen4, swgoogle);
                        boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.json_blue));

                        if (!success) {

                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }
                    SaveStyle("json_blue");

                    Toast.makeText(MainActivity.this, "Se aplico el nuevo estilo de mapa", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.btBlack: {
                    try {
                        seleccionarMapa(imagen2,imagen1, imagen4, imagen3, swgoogle);

                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.json_black));
                        SaveStyle("json_black");

                        if (!success) {

                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }

                    Toast.makeText(MainActivity.this, "Se aplico el nuevo estilo de mapa", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.btCandy: {
                    try {
                        seleccionarMapa(imagen3,imagen1, imagen2, imagen4, swgoogle);
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.json_candy));
                        SaveStyle("json_candy");

                        if (!success) {

                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }

                    Toast.makeText(MainActivity.this, "Se aplico el nuevo estilo de mapa", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.btVintage: {
                    try {
                        seleccionarMapa(imagen4,imagen1, imagen2, imagen3, swgoogle);
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.json_vintage));
                        SaveStyle("json_vintage");

                        if (!success) {


                            Log.e(TAG, "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e(TAG, "Can't find style. Error: ", e);
                    }

                    Toast.makeText(MainActivity.this, "Se aplico el nuevo estilo de mapa", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.swEstiloGoogle: {
                    if(swgoogle.isChecked())
                    {
                        imagen1.setVisibility(View.INVISIBLE);
                        imagen2.setVisibility(View.INVISIBLE);
                        imagen3.setVisibility(View.INVISIBLE);
                        imagen4.setVisibility(View.INVISIBLE);
                        SaveStyle("default");
                        googleMap.setMapStyle(new MapStyleOptions("º"));
                    }
                        break;
                }
            }
        }

        private void seleccionarMapa(ImageView i1,ImageView i2,ImageView i3,ImageView i4,Switch s){
                i1.setVisibility(View.VISIBLE);
                i2.setVisibility(View.INVISIBLE);
                i3.setVisibility(View.INVISIBLE);
                i4.setVisibility(View.INVISIBLE);
                s.setChecked(false);
        }
    }
    private void instanciarMapas() {
        SettingMapsDialog dialog = new SettingMapsDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "AJUSTES_MAPAS");// Mostramos el dialogo

    }

    // ------------------------ DIALOG MIS MASCOTAS-----------------------------------------
    @SuppressLint("ValidFragment")
    private class MisMascotasDialog extends DialogFragment {
        private ArrayList<Mascota>ListaMisMascotas;
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_my_pets, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
            });
            ListaMisMascotas=new ArrayList<>();
            CargarDatosMascotaRecyclerMisMascotas(content);
            return builder.create();
        }
        private void CargarDatosMascotaRecyclerMisMascotas(View view) {
            mDatabase.child("Usuarios").child(Objects.requireNonNull(mDatabase.child(UserId).getKey())).child("Marcadores").child("Pet").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("Pet", dataSnapshot.toString());

                    for(DataSnapshot d:dataSnapshot.getChildren()){

                        Mascota mMascota = d.getValue(Mascota.class);
                        ListaMisMascotas.add(mMascota);

                    }

                    recyclerFavoritos = view.findViewById(R.id.RecViewFavoritos);
                    recyclerFavoritos.setLayoutManager(new LinearLayoutManager(view.getContext()));
                    AdaptadorMascota adapter = new AdaptadorMascota(ListaMisMascotas,view.getContext());
                    recyclerFavoritos.setAdapter(adapter);

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
///este metodo tiene que ver donde se  van a aguardar los favoritos  reccorrerlo, por eso salta el error en la line 1010


    //cuando llames al boton de fb, deves ahcer lo mismo pero asigandole a listaFavorito y setear el campo del dialogo  que dice mis mascotas,  a mis favoritos...







    private void instaciarMisMascotas() {
        MisMascotasDialog dialog = new MisMascotasDialog();  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "MIS_MASCOTAS");// Mostramos el dialogo

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
        dialog.show(getFragmentManager(), "TERMINOS");// Mostramos el dialogo

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
        dialog.show(getFragmentManager(), "NOSOTROS");// Mostramos el dialogo
    }

    // ------------------------ DIALOG PREMIUM-----------------------------------------
    @SuppressLint("ValidFragment")
    private class PremiumDialog extends DialogFragment implements View.OnClickListener {//IMPLEMENTAR EL CLICK EN EL DIALOGO
        CardView mMensual,mTrimestral,mSemestral;
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_premium, null);
            //// ESTO AGREGUE
            mMensual = content.findViewById(R.id.btnMensual);
            mTrimestral = content.findViewById(R.id.btnTrimestral);
            mSemestral = content.findViewById(R.id.btnSemestral);
            mMensual.setOnClickListener(this);
            mTrimestral.setOnClickListener(this);
            mSemestral.setOnClickListener(this);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
            });
            return builder.create();
        }



     public  void ConsultarPremium(final String urlws){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlws, null,
                response -> {
                    try {
                        String IdPremium = GeneralMethod.getRandomString();
                        JSONArray lista = response.getJSONArray("pago");
                        JSONObject json_data = lista.getJSONObject(0);
                        mDatabase.child("Usuarios").child(Objects.requireNonNull(mDatabase.child(UserId).getKey())).child("Datos Personales").child("premium").setValue(IdPremium);
                        mDatabase.child("Usuarios").child(Objects.requireNonNull(mDatabase.child(UserId).getKey())).child("Premium").child(IdPremium).setValue(json_data);

                    } catch (JSONException ignored) {
                    }
                },
                volleyError -> Toast.makeText(MainActivity.this, volleyError.toString(), Toast.LENGTH_LONG).show());
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getIntanciaVolley(this.getActivity()).addToRequestQueue(jsonObjectRequest);
    }

        public void onClick(View v) {
            Uri uri = null;
            switch (v.getId()) {
                case R.id.btnMensual: {
                   // uri = Uri.parse("http://www.secsanluis.com.ar/servicios/varios/wimp/W_Premium.php?link=1");
                    ConsultarPremium("http://www.secsanluis.com.ar/servicios/varios/wimp/W_Premium.php?link=1");
                }break;
                case R.id.btnTrimestral: {
                    //uri = Uri.parse("http://www.secsanluis.com.ar/servicios/varios/wimp/W_Premium.php?link=2");
                    ConsultarPremium("http://www.secsanluis.com.ar/servicios/varios/wimp/W_Premium.php?link=2");
                }break;
                case R.id.btnSemestral: {
                    //uri = Uri.parse("http://www.secsanluis.com.ar/servicios/varios/wimp/W_Premium.php?link=3");
                    ConsultarPremium("http://www.secsanluis.com.ar/servicios/varios/wimp/W_Premium.php?link=3");
                }break;
            }
            /*Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);*/
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
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
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
            AuthCredential credential = EmailAuthProvider
                    .getCredential("user@example.com", "password1234");
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
    // -------------------------------- Mostrar datos de masctota..........................................
    @SuppressLint("ValidFragment")
    private class DialogMostrarMarcadorMascota extends DialogFragment implements View.OnClickListener {
        Marcadores mDatosMascotas;
        public DialogMostrarMarcadorMascota(Mascota mDatosMascotas){
            this.mDatosMascotas = mDatosMascotas;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_marcador, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
            });
            CargarDatosMascota(content,(Mascota) mDatosMascotas);
            return builder.create();
        }

        @Override
        public void onClick(View v) {

        }


        private void CargarDatosMascota(View view, Mascota mMascota) {
            final TextView eDescripcionMascota = view.findViewById(R.id.eDescripcionMascota),
                    eNombreMascota = view.findViewById(R.id.eNombreMascota);
            final CircleImageView imgMascota = view.findViewById(R.id.imgFotomascota);

            eDescripcionMascota.setText(mMascota.getDescripcion());
            eNombreMascota.setText(mMascota.getNombre());
            GeneralMethod.GlideUrl(this.getActivity(), mMascota.getImagen(),imgMascota);
            CargarComentariosMascota(mMascota,view);
        }

        private void CargarComentariosMascota(Mascota mMascota,View view) {
            mDatabase.child("Usuarios").child(Objects.requireNonNull(mMascota.getIdMarcador())).child("Marcadores")
                    .child("Comentarios").child(mMascota.getIdComentario()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("COMENTARIO", dataSnapshot.toString());


                    Comentario mComentario = dataSnapshot.getValue(Comentario.class);
                    ListaComentario.add(mComentario);

                    if(ListaComentario.get(0) != null){
                        recyclerComentarios = view.findViewById(R.id.RecViewComentario);
                        recyclerComentarios.setLayoutManager(new LinearLayoutManager(view.getContext()));
                        AdaptadorComentarios adapter = new AdaptadorComentarios(ListaComentario,view.getContext());
                        recyclerComentarios.setAdapter(adapter);
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
    private void instaciarDialogoMostrarMarcadorMascota(Mascota mDatosMascota) {
        DialogMostrarMarcadorMascota dialog = new DialogMostrarMarcadorMascota(mDatosMascota);  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "MASCOTA");// Mostramos el dialogo
    }

    // -------------------------------- Mostrar datos de Tienda..........................................
    @SuppressLint("ValidFragment")
    private class DialogMostrarMarcadorTienda extends DialogFragment implements View.OnClickListener {
        Marcadores mDatosTienda;
        public DialogMostrarMarcadorTienda(Tienda mDatosTienda){
            this.mDatosTienda = mDatosTienda;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View content = inflater.inflate(R.layout.dialog_marcador_tienda, null);
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss();
                }
                return false;
            });
            CargarDatosTienda(content,(Tienda) mDatosTienda);
            return builder.create();
        }

        @Override
        public void onClick(View v) {

        }

        private void CargarDatosTienda(View view, Tienda mTienda) {
            final TextView eDireccionTienda = view.findViewById(R.id.eDireccionTienda),
                    eNombreTienda = view.findViewById(R.id.eNombreMascota),
                    eTelefonoTienda=view.findViewById(R.id.eTelefonoTienda);

            final CircleImageView imgTienda = view.findViewById(R.id.imgFotoTienda);

            eDireccionTienda.setText(mTienda.getDireccion());
            eNombreTienda.setText(mTienda.getNombre());
            eTelefonoTienda.setText(mTienda.getTelefono());
            GeneralMethod.GlideUrl(this.getActivity(), mTienda.getImagen(),imgTienda);
            CargarPublicidadesTienda(mTienda,view);
        }

        private void CargarPublicidadesTienda(Tienda mTienda,View view) {
            mDatabase.child("Usuarios").child(Objects.requireNonNull(mTienda.getIdMarcador()))
                    .child("Marcadores").child("Publicidad").child(mTienda.getIdPublicidad()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("PUBLICIDAD", dataSnapshot.toString());
                    Publicidad mPublicidad=dataSnapshot.getValue(Publicidad.class);

                    ListaPublicidad.add(mPublicidad);
                    recyclerPublicidad = view.findViewById(R.id.RecViewPublicidad);
                    recyclerPublicidad.setLayoutManager(new LinearLayoutManager(view.getContext()));
                    AdaptadorPublicidades adapter = new AdaptadorPublicidades(ListaPublicidad,view.getContext());
                    recyclerPublicidad.setAdapter(adapter);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void instaciarDialogoMostrarMarcadorTienda(Tienda mDatosTienda) {
        DialogMostrarMarcadorTienda dialog = new DialogMostrarMarcadorTienda(mDatosTienda);  //Instanciamos la clase con el dialogo
        dialog.setCancelable(false);
        dialog.show(getFragmentManager(), "MASCOTA");// Mostramos el dialogo
    }

























}

