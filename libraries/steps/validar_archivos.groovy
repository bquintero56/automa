void call() {
    def host      = config.host
    def user      = config.user
    def runAsUser = config.runAsUser
    def ruta      = config.ruta
    def faltantes = []
    def encontrados = []

    def fechaAyer = sh(
        returnStdout: true,
        script: "TZ='${config.zonaHoraria}' date -d 'yesterday' +'${config.formatoFecha}'"
    ).trim()

    echo "Fecha del día anterior: ${fechaAyer}"

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
            encontrados << archivo
        } else {
            echo "❌ No encontrado: ${ruta}/${archivo} (rc=${rc})"
            faltantes << archivo
        }
    }

    // Resumen final
    echo "=========== RESUMEN ==========="
    echo "Encontrados: ${encontrados.size()} de ${archivos.size()}"
    echo "Faltantes:   ${faltantes.size()} de ${archivos.size()}"

    if (faltantes) {
        echo "Archivos no encontrados en ${host}:${ruta}:\n - ${faltantes.join('\n - ')}"

        switch (config.estadoSiFaltan) {
            case 'FAILURE':
                error "Faltan ${faltantes.size()} archivo(s)"
                break
            case 'UNSTABLE':
                unstable("Faltan ${faltantes.size()} archivo(s)")
                break
            default:
                // SUCCESS: solo informa, el pipeline queda en verde
                break
        }
    } else {
        echo "Todos los archivos fueron encontrados"
    }
}
