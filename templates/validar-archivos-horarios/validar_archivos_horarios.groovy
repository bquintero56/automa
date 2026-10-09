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

                stage('Notificación: enviar correo') {
                    if (resultados) {
                        enviar_correo_resultados(resultados)
                    } else {
                        echo "Sin validaciones en esta hora, no se envía correo"
                    }
                }

                stage('SIN_APROBADO: validar y procesar') {
                    resultadoSA = procesar_sin_aprobado(params.HORA)
                }

                stage('SIN_APROBADO: notificación') {
                    if (resultadoSA.estado == 'CSV_NO_ENCONTRADO' || resultadoSA.estado == 'PDF_SIN_REGISTRO') {
                        enviar_correo_sin_aprobado(resultadoSA)
                    } else {
                        echo "SIN_APROBADO: estado ${resultadoSA.estado}, no se envía correo"
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