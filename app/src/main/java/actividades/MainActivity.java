package actividades;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.ArrayList;
import java.util.Objects;
import android.support.design.widget.NavigationView;

import android.support.v4.app.ActivityCompat;
import com.google.android.gms.maps.model.MarkerOptions;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;

import com.whereismypet.whereismypet.R;

import misclases.Marcador;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,AppCompatCallback,LocationListener,
        NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    GoogleMap googleMap;
    GoogleApiClient apiClient = null;
    ArrayList<LatLng> MarkerPoints;
    Marcador marcador;
    LatLng LocalizacionCoord;
    SharedPreferences sharedPreferences2;
    LocationManager locationManager;
    AlertDialog alertGPS = null;
    Location Localizacion;
    DrawerLayout drawer;

    static final int PETICION_PERMISO_LOCALIZACION = 0;
    int status = 0;

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

       //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapa);
        //mapFragment.getMapAsync(this);
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
        }catch (Exception ex) { }
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

    private void CrearMarkers(LatLng latLng) {

        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(marcador.getTitulo())
                .snippet(marcador.getDescripcion())

                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marcador_rojo_chiquito)));


    }

    //------------------------------SOLICITUD DE PERMISOS-------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PETICION_PERMISO_LOCALIZACION && permissions.length != 0) {
            if (permissions.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MyLocation();
                Toast.makeText(this, "Permisos de ubicacion aceptado", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permimos de ubicacion ignorados", Toast.LENGTH_SHORT).show();
            }
        }
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
        if (id == R.id.nav_lugares) {
            //Intent intent = new Intent(this, MisLugares.class);
            //startActivity(intent);
        } else if (id == R.id.nav_drawrute) {
            //drawDialog();
        } else if (id == R.id.nav_setting) {
            //instaciarAjustes();

        } else if (id == R.id.nav_share) {
            /*Uri uri = Uri.parse("http://www.ulp.edu.ar/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);*/
        }else if(id == R.id.nav_send){
            /*Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.oferta.educacion.ulp&hl=es");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);*/
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

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

                    if (status == 1) {
                        if (MarkerPoints.size() > 1) {
                            MarkerPoints.clear();
                            googleMap.clear();
                        }
                        MarkerPoints.add(point);
                        MarkerOptions options = new MarkerOptions();
                        options.position(point);

                        googleMap.addMarker(options);

                    }
                }
            });
            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onMapLongClick(LatLng latLng) {
                    marcador.setLatitud(String.valueOf(latLng.latitude));
                    marcador.setLongitud(String.valueOf(latLng.longitude));
                    CrearMarkers(latLng);
                }
            });
        }
    }


    //-----------------------------------PREFERENCIAS---------------------------------------------------------------------------
    private boolean MyPosition(){
        sharedPreferences2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return  sharedPreferences2.getBoolean("position",true);
    }

}
