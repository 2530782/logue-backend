package com.example.backend

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
enum class Role { ADMIN, SUPERVISOR, DOCTOR, CLIENTE, NEGOCIO }

@Serializable
data class User(val id: Int, val username: String, val password: String, val fullName: String, val role: Role)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val username: String, val fullName: String, val role: Role)

fun main() {
    embeddedServer(CIO, port = 8085, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("/") {
                call.respondText("Servidor Ktor corriendo desde Android Studio")
            }
            post("/api/auth/login") {
                val request = call.receive<LoginRequest>()
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
    }.start(wait = true)
}