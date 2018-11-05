package Modelo;

import android.net.Uri;

public class Usuario {


    private String nombre;
    private String apellido;
    private String facebook;
    private String email;
    private String contraseña;



    private String imagenPerfilBase;



    public Usuario(String nombre, String apellido, String email, String contraseña) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contraseña = contraseña;
    }

    public String getImagenPerfilBase() {
        return imagenPerfilBase;
    }

    public void setImagenPerfilBase(String imagenPerfilBase) {
        this.imagenPerfilBase = imagenPerfilBase;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }



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
