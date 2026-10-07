package com.example.logue_frontend.shared

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
enum class Role { ADMIN, SUPERVISOR, DOCTOR, CLIENTE, NEGOCIO }

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val token: String, val username: String, val fullName: String, val role: Role)

sealed class Screen {
    object Login : Screen()
    data class Dashboard(val fullName: String, val role: Role) : Screen()
}

object AuthClient {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private const val BASE_URL = "http://192.168.100.209:8085/api/auth/login"

    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = client.post(BASE_URL) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<LoginResponse>())
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message}"))
        }
    }
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            when (val screen = currentScreen) {
                is Screen.Login -> LoginScreen(
                    onLoginSuccess = { response ->
                        currentScreen = Screen.Dashboard(response.fullName, response.role)
                    }
                )
                is Screen.Dashboard -> DashboardScreen(
                    fullName = screen.fullName,
                    role = screen.role,
                    onLogout = { currentScreen = Screen.Login }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (LoginResponse) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Centro Polígono Guadalupe", style = MaterialTheme.typography.headlineSmall)
        Text("Sistema COP / BTP", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    errorMessage = ""
                    val result = AuthClient.login(LoginRequest(username, password))
                    isLoading = false
                    result.onSuccess { response ->
                        onLoginSuccess(response)
                    }.onFailure { ex ->
                        errorMessage = ex.message ?: "Error de autenticación"
                    }
                }
            },
            enabled = !isLoading && username.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Ingresando..." else "Iniciar Sesión")
        }
    }
}

@Composable
fun DashboardScreen(fullName: String, role: Role, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido(a),", style = MaterialTheme.typography.titleMedium)
        Text(fullName, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rol: ${role.name}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                when (role) {
                    Role.ADMIN -> Text("Módulo ADMIN: Gestión global de talleres, usuarios y reportes.")
                    Role.SUPERVISOR -> Text("Módulo SUPERVISOR: Control de agendas y monitoreo de servicios.")
                    Role.DOCTOR -> Text("Módulo DOCTOR: Consultas médicas, expedientes y citas del día.")
                    Role.CLIENTE -> Text("Módulo CLIENTE: Agenda de citas y bolsa de trabajo BTP.")
                    Role.NEGOCIO -> Text("Módulo NEGOCIO BTP: Publicación de vacantes laborales.")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLogout) {
            Text("Cerrar Sesión")
        }
    }
}