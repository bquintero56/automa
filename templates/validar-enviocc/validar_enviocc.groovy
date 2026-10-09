def resultado = [:]

programar_ejecucion()

timeout(time: execution.time, unit: execution.units) {

    node(jenkins_agent_label) {
        withFolderProperties {
            try {
                stage('ENVIOCC: validar y comparar') {
                    try {
                        resultado = procesar_enviocc()
                    }
                    catch (Exception e) {
                        echo "Error en ENVIOCC: ${e.message}"
                        resultado = [estado: 'ERROR_PROCESO', titulo: 'ENVIOCC', mensaje: e.message]
                        unstable("Error en ENVIOCC: ${e.message}")
                    }
                }

                stage('Notificación: enviar correo') {
                    if (resultado.estado != 'OK') {
                        enviar_correo_enviocc(resultado)
                    } else {
                        echo "ENVIOCC: todo coincide, no se envía correo"
                    }
                }
            }
            catch (Exception e) {
                error("${e.message}")
            }
            finally {
                clean_job_workspace()
            }
        }
    }
}