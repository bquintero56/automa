libraries {
    validacion_archivos {
        host      = '10.20.30.40'
        user      = 'svc_jenkins'      // usuario con el que entra por SSH
        runAsUser = 'usr_datos'        // usuario al que se cambia para ver los archivos
        ruta      = '/data/entrada'
        archivos  = ['clientes.csv', 'ventas.csv', 'productos.csv']
        nodo      = 'nodo-ssh'         // label del nodo que ya tiene el acceso SSH
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