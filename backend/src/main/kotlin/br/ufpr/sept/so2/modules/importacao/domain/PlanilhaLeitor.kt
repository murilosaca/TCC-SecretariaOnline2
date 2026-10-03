package br.ufpr.sept.so2.modules.importacao.domain

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

object PlanilhaLeitor {
    const val MAX_BYTES: Int = 20 * 1024 * 1024
    const val MAX_LINHAS: Int = 10_000

    fun ler(nome: String, bytes: ByteArray): List<Map<String, String>> {
        if (bytes.size > MAX_BYTES) {
            throw DadoInvalidoException("Arquivo excede 20 MB.")
        }
        if (bytes.isEmpty()) {
            throw DadoInvalidoException("Arquivo vazio.")
        }
        val linhas = if (ehXlsx(nome, bytes)) lerXlsx(bytes) else lerCsv(bytes)
        if (linhas.size > MAX_LINHAS) {
            throw DadoInvalidoException("A planilha excede 10.000 linhas.")
        }
        if (linhas.isEmpty()) {
            throw DadoInvalidoException("A planilha não tem linhas de dados.")
        }
        return linhas
    }

    private fun ehXlsx(nome: String, bytes: ByteArray): Boolean {
        val ext = nome.substringAfterLast('.', "").lowercase()
        if (ext == "xlsx") {
            return true
        }
        return bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() && ext != "csv"
    }

    private fun lerCsv(bytes: ByteArray): List<Map<String, String>> {
        val texto = bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF")
        val registros = registrosCsv(texto)
        if (registros.isEmpty()) {
            return emptyList()
        }
        val cabecalho = registros.first().map { it.trim() }
        return registros.drop(1).mapNotNull { campos ->
            if (campos.all { it.isBlank() }) {
                null
            } else {
                cabecalho.mapIndexed { indice, coluna ->
                    coluna to campos.getOrElse(indice) { "" }.trim()
                }.toMap()
            }
        }
    }

    private fun registrosCsv(texto: String): List<List<String>> {
        val registros = mutableListOf<List<String>>()
        val campo = StringBuilder()
        val atual = mutableListOf<String>()
        var aspas = false
        var i = 0
        while (i < texto.length) {
            val char = texto[i]
            if (aspas) {
                if (char == '"') {
                    if (i + 1 < texto.length && texto[i + 1] == '"') {
                        campo.append('"')
                        i++
                    } else {
                        aspas = false
                    }
                } else {
                    campo.append(char)
                }
            } else {
                when (char) {
                    '"' -> aspas = true
                    ',' -> {
                        atual.add(campo.toString())
                        campo.setLength(0)
                    }
                    '\n' -> {
                        atual.add(campo.toString())
                        campo.setLength(0)
                        registros.add(atual.toList())
                        atual.clear()
                    }
                    '\r' -> Unit
                    else -> campo.append(char)
                }
            }
            i++
        }
        if (campo.isNotEmpty() || atual.isNotEmpty()) {
            atual.add(campo.toString())
            registros.add(atual.toList())
        }
        return registros
    }

    private fun lerXlsx(bytes: ByteArray): List<Map<String, String>> {
        var shared: List<String> = emptyList()
        var sheet: ByteArray? = null
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val nome = entry.name
                if (nome == "xl/sharedStrings.xml") {
                    shared = textos(zip.readBytes())
                } else if (nome == "xl/worksheets/sheet1.xml") {
                    sheet = zip.readBytes()
                }
                entry = zip.nextEntry
            }
        }
        val xml = sheet ?: throw DadoInvalidoException("Não foi possível ler a planilha.")
        return linhasXlsx(xml, shared)
    }

    private fun linhasXlsx(xml: ByteArray, shared: List<String>): List<Map<String, String>> {
        val doc = documento(xml)
        val rows = doc.getElementsByTagName("row")
        val matriz = mutableListOf<List<String>>()
        for (i in 0 until rows.length) {
            val row = rows.item(i)
            val cells = row.childNodes
            val valores = mutableListOf<String>()
            for (c in 0 until cells.length) {
                val node = cells.item(c)
                if (node.nodeName != "c") {
                    continue
                }
                val tipo = node.attributes?.getNamedItem("t")?.nodeValue
                val texto = textoDaCelula(node, tipo, shared)
                valores.add(texto)
            }
            if (valores.any { it.isNotBlank() }) {
                matriz.add(valores)
            }
        }
        if (matriz.isEmpty()) {
            return emptyList()
        }
        val cabecalho = matriz.first().map { it.trim() }
        return matriz.drop(1).map { campos ->
            cabecalho.mapIndexed { indice, coluna ->
                coluna to campos.getOrElse(indice) { "" }.trim()
            }.toMap()
        }
    }

    private fun textoDaCelula(node: org.w3c.dom.Node, tipo: String?, shared: List<String>): String {
        if (tipo == "inlineStr") {
            return textosDoNo(node).joinToString("")
        }
        val valor = primeiro(node, "v")?.textContent?.trim().orEmpty()
        if (tipo == "s") {
            val indice = valor.toIntOrNull() ?: return ""
            return shared.getOrElse(indice) { "" }
        }
        return valor
    }

    private fun textos(xml: ByteArray): List<String> {
        val doc = documento(xml)
        val items = doc.getElementsByTagName("si")
        return (0 until items.length).map { textosDoNo(items.item(it)).joinToString("") }
    }

    private fun textosDoNo(node: org.w3c.dom.Node): List<String> {
        val saida = mutableListOf<String>()
        val nos = node.childNodes
        for (i in 0 until nos.length) {
            val filho = nos.item(i)
            if (filho.nodeName == "t") {
                saida.add(filho.textContent ?: "")
            } else {
                saida.addAll(textosDoNo(filho))
            }
        }
        return saida
    }

    private fun primeiro(node: org.w3c.dom.Node, nome: String): org.w3c.dom.Node? {
        val nos = node.childNodes
        for (i in 0 until nos.length) {
            if (nos.item(i).nodeName == nome) {
                return nos.item(i)
            }
        }
        return null
    }

    private fun documento(xml: ByteArray): org.w3c.dom.Document {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = false
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        return factory.newDocumentBuilder().parse(ByteArrayInputStream(xml))
    }
}
