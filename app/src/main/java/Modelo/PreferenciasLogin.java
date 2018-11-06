package Modelo;

public class PreferenciasLogin {
    String SignOut;
    boolean recordarUsuario;


    public String getTipoSignOut() {
        return SignOut;

    }

    public PreferenciasLogin setTipoSignOut(String tipoSignOut) {
        this.SignOut = tipoSignOut;
        return this;
    }

    public boolean isRecordarUsuario() {
        return recordarUsuario;
    }

    public PreferenciasLogin setRecordarUsuario(boolean recordarUsuario) {
        this.recordarUsuario = recordarUsuario;
        return this;
    }


}
