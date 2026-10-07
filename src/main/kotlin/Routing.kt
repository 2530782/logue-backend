package com.example

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Servidor Ktor de Logue Activo")
        }

        post("/api/auth/login") {
            val request = call.receive<LoginRequest>()

            // Lee el archivo users.json de la carpeta resources
            val jsonStream = object {}.javaClass.getResourceAsStream("/users.json")
            val jsonString = jsonStream?.bufferedReader()?.use { it.readText() }

            if (jsonString == null) {
                call.respond(HttpStatusCode.InternalServerError, "Error al cargar users.json")
                return@post
            }

            val users = Json.decodeFromString<List<User>>(jsonString)
            val foundUser = users.find { it.username == request.username && it.password == request.password }

            if (foundUser != null) {
                call.respond(
                    HttpStatusCode.OK,
                    LoginResponse(
                        token = "session-token-${foundUser.role.name}",
                        username = foundUser.username,
                        fullName = foundUser.fullName,
                        role = foundUser.role
                    )
                )
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Credenciales incorrectas")
            }
        }
    }
}