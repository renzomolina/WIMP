package actividades;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import com.whereismypet.whereismypet.R;
public class AdaptadorComentarios extends RecyclerView.Adapter<AdaptadorComentarios.ViewHolderComentarios> implements View.OnClickListener{

    private View.OnClickListener listener;
    private ArrayList<Comentario> datos;

    public class ViewHolderComentarios extends RecyclerView.ViewHolder {

        private ImageView ImgUsuario;
        private TextView tvCuerpo;
        public ViewHolderComentarios(View itemView) {
            super( itemView );

            ImgUsuario= itemView.findViewById(R.id.ImagenPerfilComentario);
            tvCuerpo=(TextView)itemView.findViewById(R.id.LblCuerpo );
        }

        public void asignarDatos(Comentario c) {
          //  ImgUsuario.( c.getUsuario() );
            tvCuerpo.setText( c.getCuerpo() );
        }
    }

    public AdaptadorComentarios(ArrayList<Comentario> datos){
        this.datos=datos;
    }

    @Override
    public ViewHolderComentarios onCreateViewHolder(ViewGroup vg, int viewType) {

        View itemView = LayoutInflater.from( vg.getContext() )
                .inflate( R.layout.item_list,vg,false );
        itemView.setOnClickListener( this );

        ViewHolderComentarios vhc = new ViewHolderComentarios( itemView );

        return vhc;
    }

    @Override
    public void onBindViewHolder(ViewHolderComentarios holder, int pos) {
        Comentario item=datos.get( pos );
        holder.asignarDatos(item);

    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    public void setOnClickListener(View.OnClickListener listener){
        this.listener = listener;
    }
    @Override
    public void onClick(View v) {
        if(listener!=null)
            listener.onClick( v );

    }


}