package Modelo;

/**
 * Created by dafunes on 28/09/2018.
 */

public class Comentario {
    private String cuerpo;
    private String idUsuario;
    private String urlFoto;
    private String fechaHora;
    private String idComentario;



    public String getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(String cuerpo) {
        this.cuerpo = cuerpo;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public String getFechaHora() {
        return fechaHora;
    }
    public void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
    }

    public String getIdComentario() {

        return idComentario;
    }








}
