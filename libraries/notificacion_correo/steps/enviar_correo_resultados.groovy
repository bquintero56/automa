void call(List resultados, Map sa = [:]) {
    def haySA = (sa.estado == 'CSV_NO_ENCONTRADO' || sa.estado == 'PDF_SIN_REGISTRO' || sa.estado == 'ERROR_PROCESO')

    def fecha   = resultados ? resultados[0].fecha : (sa.fecha ?: '')
    def nombres = resultados ? resultados.collect { it.archivo }.join(', ') : (sa.titulo ?: '')

    // ---------- Sección 1: tabla de las validaciones ----------
    def seccion1 = ''
    if (resultados) {
        def filas = resultados.collect { r ->
            def color = (r.estatus == 'OK') ? '#2e7d32' : '#c62828'
            """
            <tr>
                <td style="border:1px solid #999;padding:8px 12px;">${r.proceso}</td>
                <td style="border:1px solid #999;padding:8px 12px;text-align:center;font-weight:bold;color:${color};">${r.estatus}</td>
            </tr>"""
        }.join('')

        seccion1 = """
        <p>Se adjunta el status de los archivos de ${nombres}</p>

        <table style="border-collapse:collapse;min-width:500px;">
            <tr style="background:#eeeeee;">
                <th style="border:1px solid #999;padding:8px 12px;text-align:left;">PROCESO</th>
                <th style="border:1px solid #999;padding:8px 12px;">ESTATUS</th>
            </tr>
            ${filas}
        </table>"""
    }

    // ---------- Sección 2: SIN_APROBADO (solo si hay algo que reportar) ----------
    def seccion2 = ''
    if (haySA) {
        def detalle = ''

        if (sa.estado == 'CSV_NO_ENCONTRADO') {
            def motivo = sa.rutaInaccesible
                ? "La ruta <b>${sa.ruta}</b> no es accesible."
                : "No se encontró el archivo <b>${sa.patronCsv}</b> en la ruta <b>${sa.ruta}</b>."
            detalle = """
            <p>${motivo}</p>
            <p>Se detiene la validación de ${sa.titulo}.</p>"""

        } else if (sa.estado == 'ERROR_PROCESO') {
            detalle = """
            <p>Ocurrió un error al procesar ${sa.titulo}:</p>
            <p style="color:#c62828;">${sa.mensaje}</p>"""

        } else {
            def filasSA = sa.faltantes.collect { f ->
                """
                <tr>
                    <td style="border:1px solid #999;padding:8px 12px;">${f.numero}</td>
                    <td style="border:1px solid #999;padding:8px 12px;">${f.pdf}</td>
                </tr>"""
            }.join('')

            def avisoRuta = sa.rutaPdfInaccesible ? "<p><b>Atención:</b> la ruta de PDFs ${sa.rutaPdf} no es accesible.</p>" : ''

            detalle = """
            <p>Archivo validado: <b>${sa.nombreCsv}</b></p>
            <ul>
                <li>PDFs encontrados: <b>${sa.totalPdf}</b></li>
                <li>Filas eliminadas por "${sa.valorExcluir}": <b>${sa.eliminadas}</b></li>
            </ul>
            ${avisoRuta}
            <p>Los siguientes PDFs no se encontraron en la columna ${sa.columnaSiniestro} del archivo:</p>

            <table style="border-collapse:collapse;min-width:500px;">
                <tr style="background:#eeeeee;">
                    <th style="border:1px solid #999;padding:8px 12px;text-align:left;">NÚMERO</th>
                    <th style="border:1px solid #999;padding:8px 12px;text-align:left;">PDF</th>
                </tr>
                ${filasSA}
            </table>"""
        }

        seccion2 = """
        <p style="margin-top:28px;"><b>Validación ${sa.titulo ?: 'SIN_APROBADO'}</b></p>
        ${detalle}"""
    }

    def html = """
    <html>
    <body style="font-family:Arial,sans-serif;font-size:14px;color:#222;">
        <p>Estimados buenos días,</p>
        ${seccion1}
        ${seccion2}
        <p>Saludos.</p>
    </body>
    </html>"""

    emailext(
        to:       config.destinatarios,
        subject:  "${config.asuntoPrefijo} ${nombres} ${config.asuntoSufijo} - ${fecha}",
        mimeType: 'text/html',
        body:     html
    )

    echo "Correo enviado a: ${config.destinatarios}"
}