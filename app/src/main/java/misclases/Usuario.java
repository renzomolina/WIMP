package misclases;

import android.net.Uri;

public class Usuario {


    private String nombre;
    private String apellido;
    private String facebook;
    private String email;

    public Uri getImagenPerfil() {
        return imagenPerfil;
    }

    public void setImagenPerfil(Uri imagenPerfil) {
        this.imagenPerfil = imagenPerfil;
    }

    private Uri imagenPerfil;

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
