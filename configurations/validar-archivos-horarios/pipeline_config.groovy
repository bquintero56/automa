libraries {
    validacion_horarios {
        host           = '10.20.30.40'
        user           = 'svc_jenkins'
        runAsUser      = 'usr_datos'
        zonaHoraria    = 'America/Bogota'
        estadoSiFaltan = 'SUCCESS'              // SUCCESS, UNSTABLE o FAILURE

        // Ruta con parte variable: {mes} se reemplaza por AAAAMM (202610, 202611...)
        rutaBase       = '/Proyecto/M/BACKUP/{mes}'
        formatoMes     = '%Y%m'

        // Fecha que va en el nombre del archivo
        formatoFecha   = '%Y-%m-%d'             // 2026-10-07
        diasAtras      = 0                      // 0 = hoy, 1 = ayer

        // Tolerancia: el job puede arrancar hasta N minutos después de la hora
        ventanaMinutos = 20

        archivos = [
            [nombre: 'DEVOLUCION_{fecha}.csv', hora: '15:00'],
            [nombre: 'CONTADOR_{fecha}.csv',   hora: '10:00'],
            [nombre: 'ARCHIVO_3_{fecha}.csv',  hora: '08:30'],
            [nombre: 'ARCHIVO_4_{fecha}.csv',  hora: '08:30'],
            [nombre: 'ARCHIVO_5_{fecha}.csv',  hora: '12:00'],
            [nombre: 'ARCHIVO_6_{fecha}.csv',  hora: '12:00'],
            [nombre: 'ARCHIVO_7_{fecha}.csv',  hora: '15:00'],
            [nombre: 'ARCHIVO_8_{fecha}.csv',  hora: '17:00'],
            [nombre: 'ARCHIVO_9_{fecha}.csv',  hora: '17:00'],
            [nombre: 'ARCHIVO_10_{fecha}.csv', hora: '18:00']
            // Opcional por archivo: ruta: '/OTRA/RUTA/{mes}' para sobrescribir rutaBase
        ]
    }

    notificacion_correo {
        destinatarios    = 'persona1@empresa.com,persona2@empresa.com'
        asuntoPrefijo    = 'Monitoreo activo Seguimiento'
        asuntoSufijo     = '360'
    }
}