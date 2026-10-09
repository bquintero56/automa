Map call() {
    def host      = config.host
    def user      = config.user
    def runAsUser = config.runAsUser
    def titulo    = config.textoCsv

    // Fechas y mes (respetan diasAtras)
    def datos = sh(
        returnStdout: true,
        script: """#!/bin/sh
export TZ='${config.zonaHoraria}'
date -d '${config.diasAtras} days ago' +'${config.formatoFechaCsv}'
date -d '${config.diasAtras} days ago' +'${config.formatoFechaPdf}'
date -d '${config.diasAtras} days ago' +'${config.formatoMes}'
"""
    ).trim().readLines()

    def fechaCsv = datos[0]
    def fechaPdf = datos[1]
    def mes      = datos[2]

    def rutaCsv   = config.rutaBase.replace('{mes}', mes)
    def patronCsv = "*_${config.textoCsv}_${fechaCsv}.csv"
    def patronPdf = "*_${config.textoPdf}_*_${fechaPdf}${config.extensionPdf}"

    // Busca en una ruta los archivos que coinciden con el patrón.
    // Devuelve null si la ruta no es accesible, o la lista de coincidencias (vacía si no hay).
    def buscar = { String ruta, String patron ->
        def rcRuta = sh(
            returnStatus: true,
            script: """
                ssh -o StrictHostKeyChecking=no ${user}@${host} \
                    "sudo -n -u ${runAsUser} test -d '${ruta}'"
            """
        )
        if (rcRuta != 0) {
            return null
        }

        def salida = sh(
            returnStdout: true,
            script: """#!/bin/sh
ssh -o StrictHostKeyChecking=no ${user}@${host} \\
    "sudo -n -u ${runAsUser} ls -1 '${ruta}'" \\
| while IFS= read -r f; do
    case "\$f" in
        ${patron}) echo "\$f" ;;
    esac
  done
"""
        ).trim()

        return salida ? salida.readLines() : []
    }

    // Quita los números iniciales y la extensión: 2131_CCTEST_123_09_10_2026.pdf -> CCTEST_123_09_10_2026
    def normalizar = { String nombre ->
        def x = nombre.trim()
        def i = x.indexOf("_${config.textoPdf}_")
        if (i >= 0) {
            x = x.substring(i + 1)
        }
        def ext = config.extensionPdf
        if (ext && x.toLowerCase().endsWith(ext.toLowerCase())) {
            x = x.substring(0, x.length() - ext.length())
        }
        return x
    }

    // ---------- 1) ¿Existe el CSV? ----------
    echo "Buscando CSV: ${rutaCsv}/${patronCsv}"
    def csvs = buscar(rutaCsv, patronCsv)

    if (!csvs) {
        echo "❌ CSV no encontrado${csvs == null ? ' (ruta no accesible)' : ''}: ${rutaCsv}/${patronCsv}"
        echo "Se detiene el proceso de ${titulo} (el pipeline no falla)"
        return [
            estado:          'CSV_NO_ENCONTRADO',
            titulo:          titulo,
            fecha:           fechaCsv,
            ruta:            rutaCsv,
            patronCsv:       patronCsv,
            rutaInaccesible: (csvs == null)
        ]
    }

    def nombreCsv = csvs[0]
    echo "✅ CSV encontrado: ${rutaCsv}/${nombreCsv}"
    if (csvs.size() > 1) {
        echo "⚠️ Hay ${csvs.size()} CSV que coinciden, se usa el primero: ${csvs.join(', ')}"
    }

    // ---------- 2) Copia local del CSV ----------
    def rcCopia = sh(
        returnStatus: true,
        script: """#!/bin/sh
ssh -o StrictHostKeyChecking=no ${user}@${host} "sudo -n -u ${runAsUser} cat '${rutaCsv}/${nombreCsv}'" > enviocc_original.csv
"""
    )
    if (rcCopia != 0) {
        error "No se pudo leer el CSV ${rutaCsv}/${nombreCsv} (rc=${rcCopia})"
    }

    // ---------- 3) Extraer la columna FILENAMEFIELD ----------
    def awk = '''
BEGIN { FS = sep }
{ sub(/[[:space:]]+$/, "") }
NR == 1 {
    for (i = 1; i <= NF; i++) {
        h = $i
        gsub(/[^A-Za-z0-9_]/, "", h)
        if (h == col) c = i
    }
    if (!c) {
        print "No se encontró la columna " col " en el encabezado" > "/dev/stderr"
        exit 3
    }
    next
}
{
    v = $c
    gsub(/^[[:space:]"]+|[[:space:]"]+$/, "", v)
    if (v != "") print v
}
'''
    writeFile file: 'enviocc_extraer.awk', text: awk

    def rcAwk = sh(
        returnStatus: true,
        script: """#!/bin/sh
awk -v sep='${config.separador}' -v col='${config.columna}' -f enviocc_extraer.awk enviocc_original.csv > enviocc_nombres.txt
"""
    )
    if (rcAwk != 0) {
        error "No se pudo leer la columna ${config.columna}: revisa que exista en el encabezado y que el separador sea '${config.separador}'"
    }

    def enArchivo = readFile('enviocc_nombres.txt').readLines()
        .collect { normalizar(it) }.findAll { it }.unique()

    echo "Registros en la columna ${config.columna}: ${enArchivo.size()}"

    // ---------- 4) PDFs de la ruta ----------
    echo "Buscando PDFs: ${config.rutaPdf}/${patronPdf}"
    def pdfs = buscar(config.rutaPdf, patronPdf)

    if (pdfs == null) {
        echo "❌ Ruta de PDFs no accesible: ${config.rutaPdf}"
        return [
            estado:    'RUTA_PDF_NO_ACCESIBLE',
            titulo:    titulo,
            fecha:     fechaCsv,
            nombreCsv: nombreCsv,
            rutaPdf:   config.rutaPdf
        ]
    }

    def enRuta = pdfs.collect { normalizar(it) }.findAll { it }.unique()
    echo "PDFs encontrados en la ruta: ${enRuta.size()}"

    // ---------- 5) Comparar ----------
    def faltanPdf = enArchivo.findAll { !enRuta.contains(it) }     // están en el archivo, no hay PDF
    def sobranPdf = enRuta.findAll   { !enArchivo.contains(it) }   // hay PDF, no están en el archivo

    if (faltanPdf) {
        echo "❌ FALTAN PDF en la ruta (generar ticket): ${faltanPdf.size()}"
        faltanPdf.each { echo "   - ${it}" }
    }
    if (sobranPdf) {
        echo "❌ SOBRAN PDF en la ruta, no están en el archivo (generar ticket solicitando CSV): ${sobranPdf.size()}"
        sobranPdf.each { echo "   - ${it}" }
    }
    if (!faltanPdf && !sobranPdf) {
        echo "✅ Los nombres del archivo y los PDF de la ruta coinciden"
    }

    return [
        estado:       (faltanPdf || sobranPdf) ? 'DIFERENCIAS' : 'OK',
        titulo:       titulo,
        fecha:        fechaCsv,
        nombreCsv:    nombreCsv,
        columna:      config.columna,
        rutaPdf:      config.rutaPdf,
        totalArchivo: enArchivo.size(),
        totalRuta:    enRuta.size(),
        faltanPdf:    faltanPdf,
        sobranPdf:    sobranPdf
    ]
}