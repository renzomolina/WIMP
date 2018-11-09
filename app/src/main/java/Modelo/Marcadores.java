package Modelo;

public class Marcadores {



    private String idMarcador;
    private String nombre;
    private String descripcion;
    private String imagen;
    private String latitud;
    private String longitud;
    private String telefono;


    public String getTelefono() { return telefono; }

    public Marcadores setTelefono(String telefono) { this.telefono = telefono;
        return this;
    }

    public Marcadores setNombre(String nombre) { this.nombre = nombre; return this;}
    public Marcadores setDescripcion(String descripcion) { this.descripcion = descripcion; return this;}
    public Marcadores setLatitud(String posicion) { this.latitud = posicion; return this;}
    public Marcadores setLongitud(String longitud) { this.longitud = longitud; return this;}
    public Marcadores setImagen(String imagen) { this.imagen = imagen; return this;}
    public String getIdMarcador() {
        return idMarcador;
    }

    public Marcadores setIdMarcador(String idMarcador) {
        this.idMarcador = idMarcador;
        return this;
    }

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
