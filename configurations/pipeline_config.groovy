libraries {
    validacion_archivos {
        host         = '10.20.30.40'
        user         = 'svc_jenkins'
        runAsUser    = 'usr_datos'
        ruta         = '/data/entrada'
        marcador     = 'XXXXXXX'            // texto que se reemplaza por la fecha
        formatoFecha = '%Y%m%d'             // formato de `date`, ajústalo al de tus archivos
        zonaHoraria  = 'America/Bogota'
        archivos     = [
            'REPORTE_BIENVENIDAS_XXXXXXX.csv',
            'ARCHIVO_DOS_XXXXXXX.csv',
            'ARCHIVO_TRES_XXXXXXX.csv',
            'ARCHIVO_CUATRO_XXXXXXX.csv',
            'ARCHIVO_CINCO_XXXXXXX.csv'
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
