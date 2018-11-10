package Modelo;

/**
 * Created by dafunes on 12/10/2018.
 */

public class Publicidad {


    private String idPublicidad;
    private String tituloOferta;
    private String precio;
    private String descripcionOferta;

    private String imgOferta;

    public void setIdPublicidad(String idPublicidad) {
        this.idPublicidad = idPublicidad;
    }

    public void setImgOferta(String imgOferta) {
        this.imgOferta = imgOferta;
    }

    public String getTituloOferta() {
        return tituloOferta;
    }

    public void setTituloOferta(String tituloOferta) {
        this.tituloOferta = tituloOferta;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getDescripcionOferta() {
        return descripcionOferta;
    }

    public void setDescripcionOferta(String descripcionOferta) {
        this.descripcionOferta = descripcionOferta;
    }



    public String getIdPublicidad() {
        return idPublicidad;
    }

    public String getImgOferta() {
        return imgOferta;
    }

}
