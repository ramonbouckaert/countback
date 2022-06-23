package io.bouckaert.countback

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.json.JsonPropertySource

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val configLoader = ConfigLoaderBuilder.default()

        args.firstOrNull()?.let { port -> configLoader.addSource(JsonPropertySource(""" { "port": $port } """)) }

        val config = configLoader
            .addResourceSource("/config.json")
            .build()
            .loadConfigOrThrow<ServerConfig.Config>()

        val server = WebServer(
            config
        )

        try {
            server.start()

            Runtime.getRuntime().addShutdownHook(Thread {
                server.stop()
            })
        } catch (e: Exception) {
            throw e
        }
    }
}