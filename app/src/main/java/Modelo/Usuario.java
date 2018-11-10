package Modelo;

import android.net.Uri;

public class Usuario {



    private String email;
    private String contraseña;
    private String idUsuario;


    public String getIdUsuario() {
        return idUsuario;
    }

    public Usuario setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
        return this;
    }



    public String getContraseña() {
        return contraseña;
    }

    public Usuario setContraseña(String contraseña) {
        this.contraseña = contraseña;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Usuario setEmail(String email) {
        this.email = email;
        return this;
    }




    public static class UsuarioPublico{
        private String nombre;
        private String apellido;
        private String imagen;

        public String getApellido() {
            return apellido;
        }

        public UsuarioPublico setApellido(String apellido) {
            this.apellido = apellido;
            return UsuarioPublico.this;
        }

        public String getNombre() {
            return nombre;
        }

        public UsuarioPublico setNombre(String nombre) {
            this.nombre = nombre;
            return UsuarioPublico.this;
        }

        public String getImagen() {
            return imagen;
        }

        public UsuarioPublico setImagen(String imagen) {
            this.imagen = imagen;
            return UsuarioPublico.this;
        }

    }

}
