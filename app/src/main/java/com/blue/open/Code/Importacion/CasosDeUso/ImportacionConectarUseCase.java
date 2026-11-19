package com.blue.open.Code.Importacion.CasosDeUso;

import com.blue.open.Code.Common.Cifrado.CifradorDeCredenciales;
import com.blue.open.Code.Common.Errores.CodigoDeErrorDelProyecto;
import com.blue.open.Code.Importacion.CatalogoDeJira;
import com.blue.open.Code.Importacion.ClienteDeJira;
import com.blue.open.Code.Importacion.CredencialesDeJira;
import com.blue.open.Code.Importacion.DatosDeJira;
import com.blue.open.Code.Importacion.Importacion;
import com.blue.open.Code.Importacion.ImportacionCatalogoDTO;
import com.blue.open.Code.Importacion.ImportacionConectarRequestDTO;
import com.blue.open.Code.Importacion.ImportacionRepository;
import com.blue.open.Code.Importacion.MapeosDeJira;
import com.blue.open.Code.Importacion.TipoDeEntidadJira;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Caso de uso: conecta con un proyecto de Jira (CU-IMP-01, T4.1 y Spec 7). La clave del proyecto de Jira va a ser
 * el prefijo en Shiro: tiene que tener de 2 a 6 letras y no la puede usar otro proyecto (salvo el que ya se importo
 * de esa misma clave, para reimportar). Comprueba las credenciales leyendo el proyecto, guarda la importacion
 * CONECTADA con el token cifrado y responde el catalogo para mapear. No es transaccional: no deja una transaccion
 * abierta mientras espera a Jira.
 * Dependencias externas: Jira (ClienteDeJira) y la base de datos.
 * Recibe: ImportacionConectarRequestDTO. Responde: ImportacionCatalogoDTO. Lanza CLAVE_DE_JIRA_NO_ES_PREFIJO,
 * PREFIJO_DE_PROYECTO_EN_USO, CIFRADO_NO_CONFIGURADO y los errores de Jira.
 */
@Service
@RequiredArgsConstructor
public class ImportacionConectarUseCase {

    private final ImportacionRepository importacionRepository;

    private final ProyectoRepository proyectoRepository;

    private final MapeosDeJira mapeos;

    private final ClienteDeJira jira;

    private final CatalogoDeJira catalogo;

    private final CifradorDeCredenciales cifrador;

    private final ProveedorDeUsuarioActual usuarioActual;

    public ImportacionCatalogoDTO ejecutar(ImportacionConectarRequestDTO dto) {
        String clave = dto.getClaveProyecto().trim().toUpperCase(Locale.ROOT);
        if (!clave.matches("[A-Z]{2,6}"))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.CLAVE_DE_JIRA_NO_ES_PREFIJO, "ImportacionConectarUseCase | clave " + clave);
        if (proyectoRepository.existsByPrefijo(clave) && mapeos.uuid(TipoDeEntidadJira.PROYECTO, clave).isEmpty())
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.PREFIJO_DE_PROYECTO_EN_USO,
                    "El prefijo " + clave + " ya lo usa un proyecto que no se importó de Jira.",
                    "ImportacionConectarUseCase | prefijo en uso: " + clave);
        CredencialesDeJira credenciales = new CredencialesDeJira(dto.getUrl(), dto.getCorreo().trim(), dto.getToken().trim());
        DatosDeJira.Proyecto proyecto = jira.proyecto(credenciales, clave);
        Importacion importacion = new Importacion();
        importacion.setUrlJira(credenciales.url());
        importacion.setCorreo(credenciales.correo());
        importacion.setTokenCifrado(cifrador.cifrar(credenciales.token()));
        importacion.setClaveProyectoJira(clave);
        importacion.setNombreProyectoJira(proyecto.nombre());
        importacion.setIdUsuarioDeCreacion(usuarioActual.getIdUsuarioActual());
        return catalogo.catalogo(importacionRepository.save(importacion), credenciales);
    }
}
