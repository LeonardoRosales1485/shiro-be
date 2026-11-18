package com.blue.open;

import com.blue.open.Core.Test.TestConBaseDeDatos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * T0.3 del plan: "nada se borra" lo garantiza la base (spec, Spec 1). El usuario de la aplicacion lo crea Liquibase
 * con SELECT, INSERT y UPDATE sobre las tablas shr_ y sin DELETE ni TRUNCATE; shr_historial es solo insercion. Sobre
 * las tablas del Core conserva DELETE (core-protocolo-jwt borra refresh tokens).
 * Crea una tabla shr_ de prueba como duenio, le aplica los permisos con la funcion que deja Liquibase y despues se
 * conecta como el usuario de la aplicacion. Corre sin la transaccion del test: la otra conexion tiene que ver la tabla.
 * Dependencias externas: ninguna (PostgreSQL embebido de core-test).
 */
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PermisosDeBaseDeDatosTest extends TestConBaseDeDatos {

    private static final String TABLA_SHR = "shr_prueba_de_permisos";

    private static final String TABLA_HISTORIAL = "shr_historial";

    // SQLSTATE de PostgreSQL para "permission denied"
    private static final String PERMISO_DENEGADO = "42501";

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbc;

    @Value("${spring.liquibase.parameters.shiro_usuario_app}")
    private String usuarioDeLaApp;

    @Value("${spring.liquibase.parameters.shiro_contrasena_app}")
    private String contrasenaDeLaApp;

    // Tablas creadas por el test: si shr_historial ya existe (T1.6 en adelante) no se toca
    private boolean historialCreadoPorElTest;

    // Crea las tablas de prueba como duenio y les aplica los permisos del usuario de la aplicacion
    @BeforeEach
    void crearTablasDePrueba() {
        jdbc.execute("CREATE TABLE " + TABLA_SHR + " (id SERIAL PRIMARY KEY, texto VARCHAR(50))");
        jdbc.execute("INSERT INTO " + TABLA_SHR + " (texto) VALUES ('inicial')");
        Integer existeHistorial = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pg_tables WHERE schemaname = 'public' AND tablename = ?", Integer.class,
                TABLA_HISTORIAL);
        historialCreadoPorElTest = existeHistorial != null && existeHistorial == 0;
        if (historialCreadoPorElTest) {
            jdbc.execute("CREATE TABLE " + TABLA_HISTORIAL + " (id SERIAL PRIMARY KEY, campo VARCHAR(50))");
        }
        jdbc.queryForObject("SELECT shr_aplicar_permisos_app(?)", Object.class, usuarioDeLaApp);
    }

    // Borra las tablas de prueba (como duenio)
    @AfterEach
    void borrarTablasDePrueba() {
        jdbc.execute("DROP TABLE IF EXISTS " + TABLA_SHR);
        if (historialCreadoPorElTest) {
            jdbc.execute("DROP TABLE IF EXISTS " + TABLA_HISTORIAL);
        }
    }

    @Test
    void elUsuarioDeLaAppLeeInsertaYActualizaEnTablasShr() throws SQLException {
        try (Connection conexion = conectarComoApp(); Statement sentencia = conexion.createStatement()) {
            assertEquals(1, sentencia.executeUpdate("INSERT INTO " + TABLA_SHR + " (texto) VALUES ('nuevo')"));
            assertEquals(1, sentencia.executeUpdate("UPDATE " + TABLA_SHR + " SET texto = 'editado' WHERE id = 1"));
            try (var filas = sentencia.executeQuery("SELECT COUNT(*) FROM " + TABLA_SHR)) {
                filas.next();
                assertEquals(2, filas.getInt(1));
            }
        }
    }

    @Test
    void elUsuarioDeLaAppNoPuedeBorrarEnTablasShr() throws SQLException {
        try (Connection conexion = conectarComoApp(); Statement sentencia = conexion.createStatement()) {
            SQLException borrar = assertThrows(SQLException.class,
                    () -> sentencia.executeUpdate("DELETE FROM " + TABLA_SHR + " WHERE id = 1"));
            assertEquals(PERMISO_DENEGADO, borrar.getSQLState());
        }
        try (Connection conexion = conectarComoApp(); Statement sentencia = conexion.createStatement()) {
            SQLException vaciar = assertThrows(SQLException.class,
                    () -> sentencia.executeUpdate("TRUNCATE " + TABLA_SHR));
            assertEquals(PERMISO_DENEGADO, vaciar.getSQLState());
        }
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM " + TABLA_SHR, Integer.class));
    }

    @Test
    void elHistorialEsSoloInsercion() throws SQLException {
        try (Connection conexion = conectarComoApp(); Statement sentencia = conexion.createStatement()) {
            assertEquals(1, sentencia.executeUpdate("INSERT INTO " + TABLA_HISTORIAL + " (campo) VALUES ('titulo')"));
            SQLException actualizar = assertThrows(SQLException.class,
                    () -> sentencia.executeUpdate("UPDATE " + TABLA_HISTORIAL + " SET campo = 'otro'"));
            assertEquals(PERMISO_DENEGADO, actualizar.getSQLState());
        }
    }

    @Test
    void elUsuarioDeLaAppConservaDeleteEnTablasDelCore() throws SQLException {
        try (Connection conexion = conectarComoApp(); Statement sentencia = conexion.createStatement()) {
            assertEquals(0, sentencia.executeUpdate("DELETE FROM auth_jwt_refresh_tokens WHERE 1 = 0"));
        }
    }

    // Abre una conexion a la base del test con el usuario de la aplicacion
    private Connection conectarComoApp() throws SQLException {
        String url;
        try (Connection duenio = dataSource.getConnection()) {
            url = duenio.getMetaData().getURL();
        }
        return DriverManager.getConnection(url, usuarioDeLaApp, contrasenaDeLaApp);
    }
}
