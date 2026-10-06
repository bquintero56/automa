void call() {
    def host      = config.host
    def user      = config.user
    def runAsUser = config.runAsUser
    def faltantes = []
    def totalChecks = 0

    def fechaAyer = sh(
        returnStdout: true,
        script: "TZ='${config.zonaHoraria}' date -d 'yesterday' +'${config.formatoFecha}'"
    ).trim()

    echo "Fecha del día anterior: ${fechaAyer}"

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

    config.archivos.each { item ->
        def nombre  = item.nombre.replace('{fecha}', fechaAyer)
        def patron  = item.patronEspecifico.replace('{fecha}', fechaAyer)
        def rutaEsp = item.rutaEspecifica

        echo "------ ${nombre} ------"

        // 1) Ruta general (nombre exacto)
        totalChecks++
        def enGeneral = buscar(config.rutaGeneral, nombre)
        if (enGeneral == null) {
            echo "❌ [GENERAL] Ruta no accesible: ${config.rutaGeneral}"
            faltantes << "[GENERAL] ${config.rutaGeneral}/${nombre} (ruta no accesible)"
        } else if (enGeneral) {
            echo "✅ [GENERAL] ${config.rutaGeneral}/${enGeneral[0]}"
        } else {
            echo "❌ [GENERAL] No encontrado: ${config.rutaGeneral}/${nombre}"
            faltantes << "[GENERAL] ${config.rutaGeneral}/${nombre}"
        }

        // 2) Ruta específica (patrón con comodín)
        totalChecks++
        def enEspecifica = buscar(rutaEsp, patron)
        if (enEspecifica == null) {
            echo "❌ [ESPECÍFICA] Ruta no accesible: ${rutaEsp}"
            faltantes << "[ESPECÍFICA] ${rutaEsp}/${patron} (ruta no accesible)"
        } else if (enEspecifica) {
            echo "✅ [ESPECÍFICA] ${rutaEsp}/${enEspecifica.join(', ')}"
        } else {
            echo "❌ [ESPECÍFICA] No encontrado: ${rutaEsp}/${patron}"
            faltantes << "[ESPECÍFICA] ${rutaEsp}/${patron}"
        }
    }

    // Resumen
    echo "=========== RESUMEN ==========="
    echo "Validaciones OK: ${totalChecks - faltantes.size()} de ${totalChecks}"
    echo "Faltantes:       ${faltantes.size()} de ${totalChecks}"

    if (faltantes) {
        echo "No encontrados:\n - ${faltantes.join('\n - ')}"

        switch (config.estadoSiFaltan) {
            case 'FAILURE':
                error "Faltan ${faltantes.size()} validación(es)"
                break
            case 'UNSTABLE':
                unstable("Faltan ${faltantes.size()} validación(es)")
                break
            default:
                break
        }
    } else {
        echo "Todas las validaciones fueron exitosas"
    }
}
