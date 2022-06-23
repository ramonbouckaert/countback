package io.bouckaert.countback

class ServerConfig {
    data class Config (
        val defaultResource: String = "index.html",
        val host: String = "localhost",
        val port: Int = 80
    )
}