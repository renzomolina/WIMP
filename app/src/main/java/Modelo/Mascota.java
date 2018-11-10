package Modelo;




public class Mascota extends Marcadores{
    private String idComentario;
    private String topic;

    public String getTopic() {
        return topic;
    }

    public Mascota setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getIdComentario() {
        return idComentario;
    }

    public Mascota setIdComentario(String idComentario) {
        this.idComentario = idComentario;
        return this;
    }
}
