package Modelo;

public class Marcadores  {

    private String nombre;
    private String descripcion;
    private String imagen;
    private String latitud;
    private String longitud;
    private String telefono;


    public String getTelefono() { return telefono; }

    public void setTelefono(String telefono) { this.telefono = telefono; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setLatitud(String posicion) { this.latitud = posicion; }
    public void setLongitud(String longitud) { this.longitud = longitud; }
    public void setImagen(String imagen) { this.imagen = imagen; }


    public String getNombre() {
        return nombre;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public String getLatitud() {
        return latitud;
    }
    public String getLongitud() {
        return longitud;
    }
    public String getImagen() {
        return imagen;
    }
}
