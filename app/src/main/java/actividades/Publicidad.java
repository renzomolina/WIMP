package actividades;

/**
 * Created by dafunes on 12/10/2018.
 */

public class Publicidad {
    private String tituloOferta;
    private String precio;
    private String descripcionOferta;
    private String direccion;
    private String telefono;
    private int imgOferta;

    public Publicidad(String tituloOferta, String precio, String descripcionOferta, String direccion, String telefono, int imgOferta) {
        this.tituloOferta = tituloOferta;
        this.precio = precio;
        this.descripcionOferta = descripcionOferta;
        this.direccion = direccion;
        this.telefono = telefono;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getImgOferta() {
        return imgOferta;
    }

    public void setImgOferta(int imgOferta) {
        this.imgOferta = imgOferta;
    }
}
