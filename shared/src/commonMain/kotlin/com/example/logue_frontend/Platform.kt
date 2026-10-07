package com.example.logue_frontend

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform