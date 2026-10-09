void call(Map r) {
    def asunto = "${config.asuntoPrefijo} ${r.titulo} ${config.asuntoSufijo} - ${r.fecha}"
    def cuerpo = ''

    if (r.estado == 'CSV_NO_ENCONTRADO') {
        def motivo = r.rutaInaccesible
            ? "La ruta <b>${r.ruta}</b> no es accesible."
            : "No se encontró el archivo <b>${r.patronCsv}</b> en la ruta <b>${r.ruta}</b>."

        cuerpo = """
        <p>${motivo}</p>
        <p>Se detiene la validación de ${r.titulo}.</p>"""
    } else {
        def filas = r.faltantes.collect { f ->
            """
            <tr>
                <td style="border:1px solid #999;padding:8px 12px;">${f.numero}</td>
                <td style="border:1px solid #999;padding:8px 12px;">${f.pdf}</td>
            </tr>"""
        }.join('')

        def avisoRuta = r.rutaPdfInaccesible ? "<p><b>Atención:</b> la ruta de PDFs ${r.rutaPdf} no es accesible.</p>" : ''

        cuerpo = """
        <p>Archivo validado: <b>${r.nombreCsv}</b></p>
        <ul>
            <li>PDFs encontrados: <b>${r.totalPdf}</b></li>
            <li>Filas eliminadas por "${r.valorExcluir}": <b>${r.eliminadas}</b></li>
        </ul>
        ${avisoRuta}
        <p>Los siguientes PDFs no se encontraron en la columna ${r.columnaSiniestro} del archivo:</p>

        <table style="border-collapse:collapse;min-width:500px;">
            <tr style="background:#eeeeee;">
                <th style="border:1px solid #999;padding:8px 12px;text-align:left;">NÚMERO</th>
                <th style="border:1px solid #999;padding:8px 12px;text-align:left;">PDF</th>
            </tr>
            ${filas}
        </table>"""
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
        subject:  asunto,
        mimeType: 'text/html',
        body:     html
    )

    echo "Correo enviado a: ${config.destinatarios}"
}