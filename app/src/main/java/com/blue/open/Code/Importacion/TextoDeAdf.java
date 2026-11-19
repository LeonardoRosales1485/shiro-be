package com.blue.open.Code.Importacion;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Pasa a texto (Markdown simple) el Atlassian Document Format en que Jira devuelve descripciones y comentarios: los
 * parrafos y titulos separados por una linea en blanco, los items de lista con guion y los saltos de linea. Lo que no
 * reconoce lo aplana a su texto. Si el valor ya es texto (API v2), lo devuelve tal cual.
 * Dependencias externas: ninguna.
 */
public final class TextoDeAdf {

    private TextoDeAdf() {
    }

    // Texto del documento ADF recibido; null si el nodo es null o vacio
    public static String convertir(JsonNode documento) {
        if (documento == null || documento.isNull() || documento.isMissingNode())
            return null;
        if (documento.isString())
            return documento.asString();
        List<String> bloques = new ArrayList<>();
        for (JsonNode bloque : documento.path("content")) {
            String texto = bloque(bloque);
            if (!texto.isBlank())
                bloques.add(texto);
        }
        return bloques.isEmpty() ? null : String.join("\n\n", bloques);
    }

    // Texto de un bloque: las listas van item por item con guion; el resto se aplana
    private static String bloque(JsonNode bloque) {
        String tipo = bloque.path("type").asString("");
        if (tipo.equals("bulletList") || tipo.equals("orderedList")) {
            List<String> items = new ArrayList<>();
            for (JsonNode item : bloque.path("content"))
                items.add("- " + plano(item).trim());
            return String.join("\n", items);
        }
        return plano(bloque);
    }

    // Texto de un nodo y sus hijos, en orden
    private static String plano(JsonNode nodo) {
        String tipo = nodo.path("type").asString("");
        if (tipo.equals("text"))
            return nodo.path("text").asString("");
        if (tipo.equals("hardBreak"))
            return "\n";
        StringBuilder texto = new StringBuilder();
        for (JsonNode hijo : nodo.path("content")) {
            if (!texto.isEmpty() && (hijo.path("type").asString("").equals("paragraph")))
                texto.append('\n');
            texto.append(plano(hijo));
        }
        return texto.toString();
    }
}
