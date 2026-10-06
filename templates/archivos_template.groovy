def resultados = []

timeout(time: execution.time, unit: execution.units) {

    node(jenkins_agent_label) {
        withFolderProperties {
            try {
                stage('SSH: validar archivos') {
                    resultados = validar_archivos()
                }

                stage('Notificación: enviar correo') {
                    enviar_correo_resultados(resultados)
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