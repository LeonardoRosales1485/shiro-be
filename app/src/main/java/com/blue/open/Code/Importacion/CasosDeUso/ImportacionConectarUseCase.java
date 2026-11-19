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
import com.blue.open.Code.Importacion.ValidadorDeUrlDeJira;
import com.blue.open.Code.Proyecto.Proyecto;
import com.blue.open.Code.Proyecto.ProyectoRepository;
import com.blue.open.Core.Kernel.Excepciones.ExcepcionDeApi;
import com.blue.open.Core.Kernel.Puertos.ProveedorDeUsuarioActual;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

/**
 * Caso de uso: conecta con un proyecto de Jira (CU-IMP-01, T4.1, Spec 7 y resoluciones A3 y A4 del 19/11). Solo
 * acepta sitios https de Jira Cloud. El administrador elige el prefijo del proyecto en Shiro: no lo puede usar otro
 * proyecto, y si la clave ya se importo tiene que ser el prefijo de ese proyecto (para reimportar). Comprueba las
 * credenciales leyendo el proyecto, guarda la importacion
 * CONECTADA con el token cifrado y responde el catalogo para mapear. No es transaccional: no deja una transaccion
 * abierta mientras espera a Jira.
 * Dependencias externas: Jira (ClienteDeJira) y la base de datos.
 * Recibe: ImportacionConectarRequestDTO. Responde: ImportacionCatalogoDTO. Lanza JIRA_URL_NO_PERMITIDA,
 * IMPORTACION_CON_OTRO_PREFIJO, PREFIJO_DE_PROYECTO_EN_USO, CIFRADO_NO_CONFIGURADO y los errores de Jira.
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

    private final ValidadorDeUrlDeJira validadorDeUrl;

    public ImportacionCatalogoDTO ejecutar(ImportacionConectarRequestDTO dto) {
        validadorDeUrl.validar(dto.getUrl());
        String clave = dto.getClaveProyecto().trim().toUpperCase(Locale.ROOT);
        String prefijo = dto.getPrefijo().trim().toUpperCase(Locale.ROOT);
        Optional<String> yaImportado = mapeos.uuid(TipoDeEntidadJira.PROYECTO, clave)
                .flatMap(proyectoRepository::findFirstByUuid).map(Proyecto::getPrefijo);
        if (yaImportado.isPresent() && !yaImportado.get().equals(prefijo))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.IMPORTACION_CON_OTRO_PREFIJO,
                    "El proyecto " + clave + " de Jira ya se importó con el prefijo " + yaImportado.get() + ". Usá ese prefijo para reimportarlo.",
                    "ImportacionConectarUseCase | clave " + clave + " ya importada con " + yaImportado.get());
        if (yaImportado.isEmpty() && proyectoRepository.existsByPrefijo(prefijo))
            throw new ExcepcionDeApi(CodigoDeErrorDelProyecto.PREFIJO_DE_PROYECTO_EN_USO,
                    "El prefijo " + prefijo + " ya lo usa otro proyecto. Elegí otro.",
                    "ImportacionConectarUseCase | prefijo en uso: " + prefijo);
        CredencialesDeJira credenciales = new CredencialesDeJira(dto.getUrl(), dto.getCorreo().trim(), dto.getToken().trim());
        DatosDeJira.Proyecto proyecto = jira.proyecto(credenciales, clave);
        Importacion importacion = new Importacion();
        importacion.setUrlJira(credenciales.url());
        importacion.setCorreo(credenciales.correo());
        importacion.setTokenCifrado(cifrador.cifrar(credenciales.token()));
        importacion.setClaveProyectoJira(clave);
        importacion.setPrefijo(prefijo);
        importacion.setNombreProyectoJira(proyecto.nombre());
        importacion.setIdUsuarioDeCreacion(usuarioActual.getIdUsuarioActual());
        return catalogo.catalogo(importacionRepository.save(importacion), credenciales);
    }
}
