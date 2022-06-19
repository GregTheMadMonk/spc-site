job("Deploy") {
    startOn {
        gitPush { enabled = false }
    }

    container(image = "gradle:jdk17-alpine") {
        env["SPC_HOST"] = Params("spc-host")
        env["SPC_USER"] = Secrets("spc-webmaster.key-user")
        env["SPC_ID"] = Secrets("spc-webmaster.key-id")
        kotlinScript { api ->
            api.gradlew("uploadDistribution")
            api.gradlew("reloadDistribution")
        }
    }
}

job("Restart service"){
    startOn {
        gitPush { enabled = false }
    }

    container(image = "gradle:jdk17-alpine") {
        env["SPC_HOST"] = Params("spc-host")
        env["SPC_USER"] = Secrets("spc-webmaster.key-user")
        env["SPC_ID"] = Secrets("spc-webmaster.key-id")
        kotlinScript { api ->
            api.gradlew("reloadDistribution")
        }
    }
}