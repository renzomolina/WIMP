package adaptadores;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import com.whereismypet.whereismypet.R;
import Modelo.Comentario;
import de.hdodenhof.circleimageview.CircleImageView;
import finalClass.GeneralMethod;

public class AdaptadorComentarios extends RecyclerView.Adapter<AdaptadorComentarios.ViewHolderComentarios>{

    private View.OnClickListener listener;
    private ArrayList<Comentario> ListaComentarios;
    private Context context;


    public AdaptadorComentarios(ArrayList<Comentario> listaComentarios,Context context2){

        this.ListaComentarios=listaComentarios;
        this.context=context2;
    }



    @NonNull
    @Override
    public ViewHolderComentarios onCreateViewHolder(@NonNull ViewGroup vg, int viewType) {
        View view=LayoutInflater.from(vg.getContext()).inflate(R.layout.item_list,null,false);
        return new ViewHolderComentarios(view);
    }


    public void onBindViewHolder(@NonNull ViewHolderComentarios holder, int pos) {
        holder.tvCuerpo.setText(ListaComentarios.get(pos).getCuerpo());
        GeneralMethod.GlideUrl((Activity) context, ListaComentarios.get(pos).getUrlFoto(),holder.ImgUsuario);
    }





    static class ViewHolderComentarios extends RecyclerView.ViewHolder {

         CircleImageView ImgUsuario;
         TextView tvCuerpo;

         ViewHolderComentarios(View itemView) {
            super( itemView );

            ImgUsuario = itemView.findViewById(R.id.imagenPerfilComentario);
            tvCuerpo = itemView.findViewById(R.id.eDescripcionComentario );
        }
    }
    @Override
    public int getItemCount() {
        return ListaComentarios.size();
    }











}




