package Modelo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.whereismypet.whereismypet.R;

import Modelo.Mascota;


public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private static final String TAG = "CustomInfoWindowAdapter";
    private LayoutInflater inflater;
    private String nombreMascota;
    private String descripcionMascota;
    private String imgMascota;
    private Context context;

    public CustomInfoWindowAdapter(LayoutInflater inflater, Mascota pet, Context context){
        this.inflater = inflater;
        nombreMascota = pet.getNombre();
        descripcionMascota = pet.getDescripcion();
        imgMascota = pet.getImagen();
        this.context = context;
    }

    @Override
    public View getInfoContents(final Marker m) {
        //Carga layout personalizado.
        View v = inflater.inflate(R.layout.dialog_marker_pets, null);
        String[] info = m.getTitle().split("&");
        String url = m.getSnippet();
        ((TextView)v.findViewById(R.id.tvNombreMarcador)).setText(nombreMascota);
        ((TextView)v.findViewById(R.id.tvDescrpcionMarcador)).setText(descripcionMascota);

        //WebServiceJSON.Picasso(context,imgMascota, v.<ImageView>findViewById(R.id.imgMarcadorMascota));
        return v;
    }

    @Override
    public View getInfoWindow(Marker m) {
        return null;
    }
}
