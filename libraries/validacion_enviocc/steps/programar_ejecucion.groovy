void call() {
    def p = config.hora.split(':')

    properties([
        pipelineTriggers([
            cron("TZ=${config.zonaHoraria}\n${p[1].toInteger()} ${p[0].toInteger()} * * *")
        ])
    ])
}