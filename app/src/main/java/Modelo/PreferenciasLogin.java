package Modelo;

public class PreferenciasLogin {
    private String tipoSignOut;
    private String tipoSignIn;
    public String getTipoSignIn() {
        return tipoSignIn;
    }

    public PreferenciasLogin setTipoSignIn(String tipoSignIn) {
        this.tipoSignIn = tipoSignIn;
        return this;
    }


    private boolean recordarUsuario;

    public boolean isRecordarUsuario() {
        return recordarUsuario;
    }

    public PreferenciasLogin setRecordarUsuario(boolean recordarUsuario) {
        this.recordarUsuario = recordarUsuario;
        return this;
    }



    public String getTipoSignOut() {
        return tipoSignOut;

    }

    public PreferenciasLogin setTipoSignOut(String tipoSignOut) {
        this.tipoSignOut = tipoSignOut;
        return this;
    }



}
