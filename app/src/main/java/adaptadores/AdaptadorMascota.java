package adaptadores;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.whereismypet.whereismypet.R;

import java.util.ArrayList;


import  Modelo.Mascota;

public class AdaptadorMascota extends RecyclerView.Adapter<AdaptadorComentarios.ViewHolderComentarios> implements View.OnClickListener{
    private View.OnClickListener listener;
    private ArrayList<Mascota> datosMascota;

    public AdaptadorMascota(ArrayList<Mascota> datos) {
        this.datosMascota = datos;
    }

    public class ViewHolderPublicidades extends RecyclerView.ViewHolder {

        private ImageView imagen;
        private TextView Nombre;
        private TextView descripcion;

        public ViewHolderPublicidades(View itemView) {
            super( itemView );


            imagen=(ImageView) itemView.findViewById(R.id.imagenMascotaComentario);
            Nombre=(TextView) itemView.findViewById(R.id.NombreComentarioMascota);
            descripcion=(TextView) itemView.findViewById(R.id.descripcionComentarioMascota);



        }

    }


    @NonNull
    @Override
    public AdaptadorComentarios.ViewHolderComentarios onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_mascota,null,false );
        return new AdaptadorComentarios.ViewHolderComentarios(view);
    }

    @Override
    public void onBindViewHolder(AdaptadorComentarios.ViewHolderComentarios holder, int pos) {


    }


    @Override
    public int getItemCount() {
        return datosMascota.size();
    }

    public void setOnClickListener(View.OnClickListener listener){
        listener = listener;
    }
    @Override
    public void onClick(View v) {
        if(listener!=null)
            listener.onClick( v );
    }
}
