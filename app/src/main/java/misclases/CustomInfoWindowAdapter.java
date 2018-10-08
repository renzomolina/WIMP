package misclases;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.whereismypet.whereismypet.R;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private static final String TAG = "CustomInfoWindowAdapter";
    private LayoutInflater inflater;
    private String nombreMascota;
    private String descripcionMascota;

    public CustomInfoWindowAdapter(LayoutInflater inflater, Marcador pet){
        this.inflater = inflater;
        nombreMascota = pet.getNombre();
        descripcionMascota = pet.getDescripcion();
    }

    @Override
    public View getInfoContents(final Marker m) {
        //Carga layout personalizado.
        View v = inflater.inflate(R.layout.dialog_marker_pets, null);
        String[] info = m.getTitle().split("&");
        String url = m.getSnippet();
        ((TextView)v.findViewById(R.id.tvNombreMarcador)).setText(nombreMascota);
        ((TextView)v.findViewById(R.id.tvDescrpcionMarcador)).setText(descripcionMascota);

        return v;
    }

    @Override
    public View getInfoWindow(Marker m) {
        return null;
    }
}
