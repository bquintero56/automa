def resultados = []

programar_ejecuciones()

timeout(time: execution.time, unit: execution.units) {

    node(jenkins_agent_label) {
        withFolderProperties {
            try {
                stage('SSH: validar archivos') {
                    resultados = validar_archivos_horario(params.HORA)
                }

                stage('Notificación: enviar correo') {
                    if (resultados) {
                        enviar_correo_resultados(resultados)
                    } else {
                        echo "Sin validaciones en esta hora, no se envía correo"
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