package Modelo;

public class Tienda extends Marcadores {

    private String direccion;
    private String idPublicidad;
    private String sitioWeb;

    public String getIdPublicidad() {
        return idPublicidad;
    }

    public Tienda setIdPublicidad(String idPublicidad) {
        this.idPublicidad = idPublicidad;
        return this;
    }

    public String getDireccion() {
        return direccion;
    }

    public Marcadores setDireccion(String direccion) {
        this.direccion = direccion;
        return this;
    }

    public String getSitioWeb() {
        return sitioWeb;
    }

    public Marcadores setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
        return this;
    }






}
