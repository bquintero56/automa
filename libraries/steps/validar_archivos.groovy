void call() {
    def host      = config.host
    def user      = config.user
    def runAsUser = config.runAsUser
    def ruta      = config.ruta
    def faltantes = []

    config.archivos.each { archivo ->
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
        error "Faltan ${faltantes.size()} archivo(s) en ${host}:${ruta} -> ${faltantes.join(', ')}"
    }
}