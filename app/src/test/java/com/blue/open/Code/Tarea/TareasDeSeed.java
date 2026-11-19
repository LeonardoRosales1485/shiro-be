package com.blue.open.Code.Tarea;

/**
 * Datos de los seeds de Liquibase que usan las pruebas de contrato de tareas: los tableros del proyecto Shiro
 * (2026-11-18-insert-proyectos-seed.yaml) y la tarea SHI-1 (2026-11-18-insert-tareas-seed.yaml).
 */
final class TareasDeSeed {

    // Tableros del proyecto Shiro (SHI)
    static final String TABLERO_PRINCIPAL = "b2000000-0000-4000-8000-000000000001";

    static final String TABLERO_QA = "b2000000-0000-4000-8000-000000000002";

    // Tablero principal de otro proyecto que no existe en los seeds
    static final String TABLERO_INEXISTENTE = "ffffffff-ffff-4fff-bfff-ffffffffffff";

    // Tarea SHI-1 del tablero principal, en "Por hacer"
    static final String SHI_1 = "b3000000-0000-4000-8000-000000000001";

    static final String SHI_1_CLAVE = "SHI-1";

    private TareasDeSeed() {
    }
}
