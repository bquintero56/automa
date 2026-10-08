void call() {
    def horas = config.archivos.collect { it.hora }.unique()

    def lineas = horas.collect { h ->
        def p = h.split(':')
        "${p[1].toInteger()} ${p[0].toInteger()} * * *"
    }

    def expresion = "TZ=${config.zonaHoraria}\n" + lineas.join('\n')

    properties([
        pipelineTriggers([cron(expresion)]),
        parameters([
            string(
                name: 'HORA',
                defaultValue: '',
                description: 'Vacío = automático según la hora actual. TODOS = valida los 10 archivos. HH:mm = simula esa hora (ej. 15:00).'
            )
        ])
    ])
}