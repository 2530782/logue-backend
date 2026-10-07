package com.example

import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.Serializable

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}


@Serializable
enum class Role {
    ADMIN,
    SUPERVISOR,
    DOCTOR,
    CLIENTE,
    NEGOCIO
}

@Serializable
data class User(
    val id: Int,
    val username: String,
    val password: String,
    val fullName: String,
    val role: Role
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val username: String,
    val fullName: String,
    val role: Role
)