package com.plataforma_lc.adminKafka.config;

import java.util.Set;

/**
 * Constantes con los nombres de todos los tópicos Kafka de la plataforma y sus DLT.
 * Centraliza la definición de nombres para evitar cadenas mágicas en productores,
 * consumidores y clases de configuración.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Clase utilitaria de constantes; no instanciable
    }

    /**
     * Tópico de auditoría para registro cronológico de acciones de usuarios en microservicios.
     */
    public static final String PLATAFORMA_AUDITORIA_EVENTOS = "plataforma.auditoria.eventos";

    /**
     * Tópico Dead Letter (DLT) para eventos de auditoría fallidos o defectuosos.
     */
    public static final String PLATAFORMA_AUDITORIA_EVENTOS_DLT = "plataforma.auditoria.eventos.DLT";

    /**
     * Tópico para el flujo continuo (stream) de datos de analítica de asistencia escolar.
     */
    public static final String ASISTENCIA_ANALITICA_STREAM = "asistencia.analitica.stream";

    /**
     * Tópico Dead Letter (DLT) para eventos de analítica de asistencia defectuosos.
     */
    public static final String ASISTENCIA_ANALITICA_STREAM_DLT = "asistencia.analitica.stream.DLT";

    /**
     * Tópico de alertas críticas del sistema (inasistencias reiteradas, fallos operativos).
     */
    public static final String PLATAFORMA_ALERTAS_CRITICAS = "plataforma.alertas.criticas";

    /**
     * Tópico Dead Letter (DLT) para eventos de alerta crítica defectuosos.
     */
    public static final String PLATAFORMA_ALERTAS_CRITICAS_DLT = "plataforma.alertas.criticas.DLT";

    /**
     * Conjunto de tópicos protegidos del núcleo de la plataforma que requieren confirmación para eliminarse.
     */
    public static final Set<String> TOPICOS_PROTEGIDOS = Set.of(
            PLATAFORMA_AUDITORIA_EVENTOS,
            PLATAFORMA_AUDITORIA_EVENTOS_DLT,
            ASISTENCIA_ANALITICA_STREAM,
            ASISTENCIA_ANALITICA_STREAM_DLT,
            PLATAFORMA_ALERTAS_CRITICAS,
            PLATAFORMA_ALERTAS_CRITICAS_DLT
    );
}
