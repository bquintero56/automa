libraries {
    validacion_enviocc {
        host        = '10.20.30.40'
        user        = 'svc_jenkins'
        runAsUser   = 'usr_datos'
        zonaHoraria = 'America/Bogota'
        hora        = '15:00'                        // hora de ejecución diaria (confirmar)

        // CSV: *_ENVIOCC_2026-10-09.csv en la carpeta del mes
        rutaBase        = '/Proyecto/M/BACKUP/{mes}'
        formatoMes      = '%Y%m'
        formatoFechaCsv = '%Y-%m-%d'                 // 2026-10-09
        textoCsv        = 'ENVIOCC'
        separador       = ','                        // separador del CSV (',' o ';')
        columna         = 'FILENAMEFIELD'

        // PDFs: *_CCTEST_*_09_10_2026.pdf
        rutaPdf         = '/Sistema/Proyecto/test/PDF'
        formatoFechaPdf = '%d_%m_%Y'                 // 09_10_2026
        textoPdf        = 'CCTEST'
        extensionPdf    = '.pdf'

        // Para pruebas: 0 = hoy, 1 = ayer
        diasAtras       = 0
    }

    notificacion_correo {
        destinatarios = 'persona1@empresa.com,persona2@empresa.com'
        asuntoPrefijo = 'Monitoreo activo Seguimiento'
        asuntoSufijo  = '360'
    }
}