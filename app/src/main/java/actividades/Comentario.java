package actividades;

import android.widget.ImageView;

/**
 * Created by dafunes on 28/09/2018.
 */

public class Comentario {
    public void setUsuario(ImageView usuario) {
        this.usuario = usuario;
    }

    public ImageView getUsuario() {
        return usuario;
    }

    private ImageView usuario;
    private String cuerpo;

    public Comentario(ImageView usuario, String cuerpo) {
        this.usuario = usuario;
        this.cuerpo = cuerpo;
    }



    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }
}
