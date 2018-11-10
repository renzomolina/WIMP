package adaptadores;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.whereismypet.whereismypet.R;

import java.util.ArrayList;


import  Modelo.Mascota;
import de.hdodenhof.circleimageview.CircleImageView;
import finalClass.GeneralMethod;

public class AdaptadorMascota extends RecyclerView.Adapter<AdaptadorMascota.ViewHolderMascotas>{
    private View.OnClickListener listener;
    private ArrayList<Mascota>ListaMascota;
    private Context context;

    public AdaptadorMascota(ArrayList<Mascota> datos,Context context) {
        this.ListaMascota = datos;
        this.context=context;
    }


    @NonNull
    @Override
    public ViewHolderMascotas onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_mascota,null,false );
        return new ViewHolderMascotas(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderMascotas holder, int i) {
        holder.nombreFavorito.setText(ListaMascota.get(i).getNombre());
        holder.descripcionfavorito.setText(ListaMascota.get(i).getDescripcion());
        GeneralMethod.GlideUrl((Activity) context, ListaMascota.get(i).getImagen(),holder.imagenFavorito);



    }



    class ViewHolderMascotas extends RecyclerView.ViewHolder {

        private CircleImageView imagenFavorito;
        private TextView nombreFavorito;
        private TextView descripcionfavorito;

        ViewHolderMascotas(View itemView) {
            super( itemView );



            nombreFavorito=(TextView) itemView.findViewById(R.id.eNombreMascotaFavorito);
            descripcionfavorito=(TextView) itemView.findViewById(R.id.eDescripcionMascotafavorito);
            imagenFavorito=(CircleImageView) itemView.findViewById(R.id.imgMascotaFavorito);



        }

    }





    @Override
    public int getItemCount() {
        return ListaMascota.size();
    }






}
