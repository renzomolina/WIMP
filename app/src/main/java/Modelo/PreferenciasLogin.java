package Modelo;

public class PreferenciasLogin {
    private String SignOut;
    //boolean recordarUsuario;


    public String getTipoSignOut() {
        return SignOut;

    }

    public PreferenciasLogin setTipoSignOut(String tipoSignOut) {
        this.SignOut = tipoSignOut;
        return this;
    }



}
