package com.blue.open.Code.Version;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Arma el changelog de una version en Markdown (T5.3 y adenda 3 del DAF): titulo con el nombre, la fecha de
 * publicacion (o "Sin publicar.") y las tareas terminadas agrupadas por tipo, en el orden de los tipos. Sin datos
 * externos.
 */
public final class ChangelogDeVersion {

    private ChangelogDeVersion() {
    }

    // Changelog de la version con las lineas recibidas, ya ordenadas por tipo y numero
    public static String armar(String nombre, LocalDate fechaDePublicacion, List<LineaDeChangelog> lineas) {
        StringBuilder markdown = new StringBuilder("# ").append(nombre).append("\n\n")
                .append(fechaDePublicacion == null ? "Sin publicar." : "Publicada el " + fechaDePublicacion + ".").append('\n');
        if (lineas.isEmpty()) {
            return markdown.append("\nSin tareas terminadas.\n").toString();
        }
        String tipoActual = null;
        for (LineaDeChangelog linea : lineas) {
            if (!Objects.equals(tipoActual, linea.tipo())) {
                tipoActual = linea.tipo();
                markdown.append("\n## ").append(tipoActual).append("\n\n");
            }
            markdown.append("- ").append(linea.clave()).append(' ').append(linea.titulo()).append('\n');
        }
        return markdown.toString();
    }
}
