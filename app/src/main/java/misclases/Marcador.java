package misclases;

public class Marcador {

    private String titulo;
    private String descripcion;
    private String latitud;
    private String longitud;
    private int id_Ubicacion;

    public String getTitulo() {
        return titulo;
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
    public int getId_Ubicacion() {
        return id_Ubicacion;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    public void setLatitud(String posicion) {
        this.latitud = posicion;
    }
    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }
    public void setId_Ubicacion(int id_Ubicacion) {
        this.id_Ubicacion = id_Ubicacion;
    }

}
