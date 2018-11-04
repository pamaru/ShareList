package es.udc.psi1617.trabajotutelado.sharelist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Sergio on 13/01/2017.
 */

public class Elemento {

    private String creador;
    private String fecha_creacion;
    private String estado;
    private String fecha_estado;
    private String comentario;
    private String nombre;

    public Elemento () {

    };

    public Elemento (String creador, String nombre) {
        this.creador = creador;
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        TimeZone timezone = TimeZone.getTimeZone("Europe/Madrid");
        df.setTimeZone(timezone);
        Date today = Calendar.getInstance().getTime();
        this.fecha_creacion = df.format(today);
        this.estado = "SIN TACHAR";
        this.fecha_estado = df.format(today);
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getComentario() {
        return comentario;
    }

    public String getCreador() {
        return creador;
    }

    public String getFecha_creacion() {
        return fecha_creacion;
    }

    public String getFecha_estado() {
        return fecha_estado;
    }

}
