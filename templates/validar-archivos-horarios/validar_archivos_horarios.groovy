def resultados = []
def resultadoSA = [:]

programar_ejecuciones()

timeout(time: execution.time, unit: execution.units) {

    node(jenkins_agent_label) {
        withFolderProperties {
            try {
                stage('SSH: validar archivos') {
                    resultados = validar_archivos_horario(params.HORA)
                }

                stage('SIN_APROBADO: validar y procesar') {
                    try {
                        resultadoSA = procesar_sin_aprobado(params.HORA)
                    }
                    catch (Exception e) {
                        // Si falla, no se pierde el correo con los resultados anteriores
                        echo "Error en SIN_APROBADO: ${e.message}"
                        resultadoSA = [estado: 'ERROR_PROCESO', titulo: 'SIN_APROBADO', mensaje: e.message]
                        unstable("Error en SIN_APROBADO: ${e.message}")
                    }
                }

                stage('Notificación: enviar correo') {
                    def haySA = (resultadoSA.estado == 'CSV_NO_ENCONTRADO' ||
                                 resultadoSA.estado == 'PDF_SIN_REGISTRO' ||
                                 resultadoSA.estado == 'ERROR_PROCESO')

                    if (resultados || haySA) {
                        enviar_correo_resultados(resultados, resultadoSA)
                    } else {
                        echo "Nada que reportar en esta ejecución, no se envía correo"
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