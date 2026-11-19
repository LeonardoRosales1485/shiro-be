package com.blue.open.Code.Historial;

import com.blue.open.Code.Common.Canal.CanalDeLaPeticion;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Escribe filas en shr_historial dentro de la transaccion del llamador (Spec 3: toda escritura escribe su
 * historial en la misma transaccion). El usuario es el de la sesion y el canal, el del request.
 * Dependencias externas: la base de datos.
 */
@Component
@RequiredArgsConstructor
public class RegistradorDeHistorial {

    private final HistorialRepository repository;

    private final ProveedorDeUsuarioActual usuarioActual;

    private final CanalDeLaPeticion canal;

    // Escribe una fila. Recibe entidad, uuid, accion, campo (o null) y los valores como texto (o null)
    public void registrar(String entidad, String uuid, AccionDeHistorial accion, String campo, String anterior,
            String nuevo) {
        registrar(entidad, uuid, accion, campo, anterior, nuevo, null);
    }

    // Igual que registrar, con la operacion de archivo que produjo el cambio (T1.8)
    public void registrar(String entidad, String uuid, AccionDeHistorial accion, String campo, String anterior,
            String nuevo, Long idOperacion) {
        Historial fila = new Historial();
        fila.setEntidad(entidad);
        fila.setUuidEntidad(uuid);
        fila.setAccion(accion);
        fila.setCampo(campo);
        fila.setValorAnterior(anterior);
        fila.setValorNuevo(nuevo);
        fila.setCanal(canal.actual());
        fila.setIdOperacion(idOperacion);
        fila.setIdUsuarioDeCreacion(usuarioActual.getIdUsuarioActual());
        repository.save(fila);
    }

    // Escribe una fila EDITAR por cada campo cuyo valor cambio entre antes y despues (mismas claves, mismo orden)
    public void registrarCambios(String entidad, String uuid, Map<String, String> antes, Map<String, String> despues) {
        for (Map.Entry<String, String> campo : despues.entrySet()) {
            String anterior = antes.get(campo.getKey());
            if (!Objects.equals(anterior, campo.getValue())) {
                registrar(entidad, uuid, AccionDeHistorial.EDITAR, campo.getKey(), anterior, campo.getValue());
            }
        }
    }
}
