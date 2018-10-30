package actividades;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.whereismypet.whereismypet.R;
import java.util.ArrayList;

public class AdaptadorPublicidades extends RecyclerView.Adapter<AdaptadorPublicidades.ViewHolderPublicidades> implements View.OnClickListener{

    private View.OnClickListener listenerOfertas;
    private ArrayList<Publicidad> ofertas;

    public AdaptadorPublicidades(ArrayList<Publicidad> ofertas) {
        this.ofertas = ofertas;
    }

    public class ViewHolderPublicidades extends RecyclerView.ViewHolder {

        private TextView tvtitulo;
        private TextView tvprecio;
        private TextView tvdescripcion;
        private TextView tvdireccion;
        private TextView tvtelefono;
        private ImageView ivOferta;
        public ViewHolderPublicidades(View itemView) {
            super( itemView );

            tvtitulo=(TextView) itemView.findViewById( R.id.TvTitulo );
            tvprecio=(TextView) itemView.findViewById( R.id.TvPrecio );
            tvdescripcion=(TextView) itemView.findViewById( R.id.TvDescripcion );
            tvdireccion=(TextView) itemView.findViewById( R.id.TvDireccion );
            tvtelefono=(TextView) itemView.findViewById( R.id.TvTelefono );
            ivOferta=(ImageView)itemView.findViewById( R.id.IvOferta );
        }

    }


    @Override
    public ViewHolderPublicidades onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_oferta,null,false );
        return new ViewHolderPublicidades( view );
    }

    @Override
    public void onBindViewHolder(ViewHolderPublicidades holder, int pos) {
        holder.tvtitulo.setText( ofertas.get(pos).getTituloOferta() );
        holder.tvprecio.setText( ofertas.get(pos).getPrecio() );
        holder.tvdescripcion.setText( ofertas.get(pos).getDescripcionOferta() );
        holder.tvdireccion.setText( ofertas.get(pos).getDireccion() );
        holder.tvtelefono.setText( ofertas.get(pos).getTelefono() );
        holder.ivOferta.setImageResource( ofertas.get(pos).getImgOferta() );


    }

    @Override
    public int getItemCount() {
        return ofertas.size();
    }

    public void setOnClickListener(View.OnClickListener listener){
        listenerOfertas = listener;
    }
    @Override
    public void onClick(View v) {
        if(listenerOfertas!=null)
            listenerOfertas.onClick( v );
    }


}
