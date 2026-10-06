void call() {
    def host      = config.host
    def user      = config.user
    def runAsUser = config.runAsUser
    def ruta      = config.ruta
    def faltantes = []

    // Fecha del día anterior
    def fechaAyer = sh(
        returnStdout: true,
        script: "TZ='${config.zonaHoraria}' date -d 'yesterday' +'${config.formatoFecha}'"
    ).trim()

    echo "Fecha del día anterior: ${fechaAyer}"

    // Reemplaza {fecha} en cada nombre
    def archivos = config.archivos.collect { it.replace('{fecha}', fechaAyer) }

    archivos.each { archivo ->
        def rc = sh(
            returnStatus: true,
            script: """
                ssh -o StrictHostKeyChecking=no ${user}@${host} \
                    "sudo -n -u ${runAsUser} test -f '${ruta}/${archivo}'"
            """
        )
        if (rc == 0) {
            echo "✅ Existe: ${ruta}/${archivo}"
        } else {
            echo "❌ No existe o sin acceso: ${ruta}/${archivo} (rc=${rc})"
            faltantes << archivo
        }
    }

    if (faltantes) {
        error "Faltan ${faltantes.size()} de ${archivos.size()} archivo(s) en ${host}:${ruta}:\n${faltantes.join('\n')}"
    }
}
