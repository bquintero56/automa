libraries {
    validacion_archivos {
        host         = '10.20.30.40'
        user         = 'svc_jenkins'
        runAsUser    = 'usr_datos'
        ruta         = '/data/entrada'
        formatoFecha = '%Y%m%d'             // formato de la fecha en el nombre del archivo
        zonaHoraria  = 'America/Bogota'
        estadoSiFaltan = 'SUCCESS'      // SUCCESS (verde), UNSTABLE (amarillo) o FAILURE (rojo)
        archivos     = [
            'REPORTE_BIENVENIDAS_{fecha}.csv',
            'ARCHIVO_DOS_{fecha}.csv',
            'ARCHIVO_TRES_{fecha}.csv',
            'ARCHIVO_CUATRO_{fecha}.csv',
            'ARCHIVO_CINCO_{fecha}.csv'
        ]
    }
}

jte{
    allow_scm_jenkinsfile= false
    pipeline_template = 'automation-pipelines/autIN/autIN_template.groovy'
}

keywords {

    execution {
        time = 10
        units = 'MINUTES' }
        jenkins_agent_label = 'linux-pro'
        job_environments = '''
        JAVA_OPTS=-Xmx512m
        MAVEN_OPTS=-Dmaven.wagon.http.ssl.insecure=true -Dmaven.resolver.transport=wagon
        '''
        log_rotator = '20'
}
