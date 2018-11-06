package Modelo;

import android.widget.ImageView;

/**
 * Created by dafunes on 28/09/2018.
 */

public class Comentario {


    public String getCuerpo() {
        return cuerpo;
    }

    public Comentario setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
        return this;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public Comentario setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
        return this;
    }

    String cuerpo;
    String idUsuario;
}
