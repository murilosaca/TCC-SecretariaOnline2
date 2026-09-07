package br.ufpr.sept.so2.modules.iam.infrastructure.security

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

internal class RepeatableBodyRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private val body: ByteArray = StreamUtils.copyToByteArray(request.inputStream)

    fun bodyAsString(): String = String(body, StandardCharsets.UTF_8)

    override fun getInputStream(): ServletInputStream {
        val input = ByteArrayInputStream(body)
        return object : ServletInputStream() {
            override fun isFinished(): Boolean = input.available() == 0

            override fun isReady(): Boolean = true

            override fun setReadListener(readListener: ReadListener?) {
                // não usado — leitura síncrona do body
            }

            override fun read(): Int = input.read()
        }
    }

    override fun getReader(): BufferedReader =
        BufferedReader(InputStreamReader(getInputStream(), StandardCharsets.UTF_8))
}
