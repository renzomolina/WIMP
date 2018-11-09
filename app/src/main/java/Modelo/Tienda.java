package Modelo;

public class Tienda extends Marcadores {

    private String direccion;
    private String idPublicidad;




    public Tienda setIdPublicidad(String idPublicidad) {
         this.idPublicidad = idPublicidad;
         return this;
    }

    public String getIdPublicidad() {
        return idPublicidad;
    }



    public String getDireccion() {
        return direccion;
    }

    public Marcadores setDireccion(String direccion) {
        this.direccion = direccion;
        return this;
    }










}
