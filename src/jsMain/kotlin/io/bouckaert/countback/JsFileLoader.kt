package io.bouckaert.countback


class JsFileLoader: FileLoader {
    override suspend fun loadFile(path: String): String {
        val fs = js("require('fs')")
        return fs.readFileSync("../../../../$path", "utf8") as String
    }
}