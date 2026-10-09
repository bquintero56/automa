void call(Map r) {
    def titulo = r.titulo ?: 'ENVIOCC'
    def fecha  = r.fecha ?: ''

    // Tabla simple de una columna
    def tabla = { String encabezado, List items ->
        def filas = items.collect { n ->
            """
            <tr>
                <td style="border:1px solid #999;padding:8px 12px;">${n}</td>
            </tr>"""
        }.join('')

        return """
        <table style="border-collapse:collapse;min-width:500px;">
            <tr style="background:#eeeeee;">
                <th style="border:1px solid #999;padding:8px 12px;text-align:left;">${encabezado}</th>
            </tr>
            ${filas}
        </table>"""
    }

    def cuerpo = ''

    if (r.estado == 'CSV_NO_ENCONTRADO') {
        def motivo = r.rutaInaccesible
            ? "La ruta <b>${r.ruta}</b> no es accesible."
            : "No se encontró el archivo <b>${r.patronCsv}</b> en la ruta <b>${r.ruta}</b>."
        cuerpo = """
        <p>${motivo}</p>
        <p>Se detiene la validación de ${titulo}.</p>"""

    } else if (r.estado == 'RUTA_PDF_NO_ACCESIBLE') {
        cuerpo = """
        <p>Archivo validado: <b>${r.nombreCsv}</b></p>
        <p>La ruta de PDFs <b>${r.rutaPdf}</b> no es accesible, no se pudo comparar.</p>"""

    } else if (r.estado == 'ERROR_PROCESO') {
        cuerpo = """
        <p>Ocurrió un error al procesar ${titulo}:</p>
        <p style="color:#c62828;">${r.mensaje}</p>"""

    } else {
        def faltan = ''
        if (r.faltanPdf) {
            faltan = """
            <p style="margin-top:20px;"><b>FALTAN PDF en la ruta</b> (${r.faltanPdf.size()}): están en la columna ${r.columna} del archivo, pero no existe el PDF.</p>
            ${tabla('NOMBRE', r.faltanPdf)}
            <p><b>Acción: generar ticket (faltan PDF).</b></p>"""
        }

        def sobran = ''
        if (r.sobranPdf) {
            sobran = """
            <p style="margin-top:20px;"><b>SOBRAN PDF en la ruta</b> (${r.sobranPdf.size()}): existen en la ruta, pero no están en la columna ${r.columna} del archivo (faltan en el archivo).</p>
            ${tabla('NOMBRE', r.sobranPdf)}
            <p><b>Acción: generar ticket solicitando el CSV para diferenciar.</b></p>"""
        }

        cuerpo = """
        <p>Archivo validado: <b>${r.nombreCsv}</b></p>
        <ul>
            <li>Registros en el archivo (${r.columna}): <b>${r.totalArchivo}</b></li>
            <li>PDFs en la ruta: <b>${r.totalRuta}</b></li>
        </ul>
        ${faltan}
        ${sobran}"""
    }

    def html = """
    <html>
    <body style="font-family:Arial,sans-serif;font-size:14px;color:#222;">
        <p>Estimados buenos días,</p>
        ${cuerpo}
        <p>Saludos.</p>
    </body>
    </html>"""

    emailext(
        to:       config.destinatarios,
        subject:  "${config.asuntoPrefijo} ${titulo} ${config.asuntoSufijo} - ${fecha}",
        mimeType: 'text/html',
        body:     html
    )

    echo "Correo enviado a: ${config.destinatarios}"
}