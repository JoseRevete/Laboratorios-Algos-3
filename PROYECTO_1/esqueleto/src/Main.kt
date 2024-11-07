
import ve.usb.libGrafo.*
import java.io.File
import java.util.ArrayDeque
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    fun seleccionaArchivoTxt(prompt: String): String? {
        val directory = File(".")
        val txtFiles = directory.listFiles { _, name -> name.endsWith(".txt") }?.sortedBy { it.name }
        if (txtFiles != null && txtFiles.isNotEmpty()) {
            val fileChooser = JFileChooser(directory)
            fileChooser.dialogTitle = "Selecciona un archivo TXT"
            fileChooser.isMultiSelectionEnabled = false
            fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
            val options = txtFiles.map { it.name }.toTypedArray()
            val selectedFile = JOptionPane.showInputDialog(
                null,
                prompt,
                "Selector de archivos *.txt",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
            )
            return selectedFile as String?
        }
        return null
    }
    val amigos_txt = seleccionaArchivoTxt("Seleccionar archivo de amigos.txt")
    if (amigos_txt == null) exitProcess(1)
    val candidatos_txt = seleccionaArchivoTxt("Seleccionar archivo de candidatos.txt")
    if (candidatos_txt == null) exitProcess(1)

    // Función para obtener los vecinos de un vértice
    fun getVecinos(g: Grafo, u: Int): List<Int> {
        return g.adyacentes(u).map { it.elOtroVertice(u) }.sorted()
    }

    // Función para obtener un grafo a partir de un archivo
    fun getGrafo(rutaArchivo: String): Grafo? {
        return try {
            val clase = Class.forName("ve.usb.libGrafo.GrafoNoDirigido").kotlin
            val constructor = clase.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == String::class }
            val instancia = constructor?.call(rutaArchivo) as Grafo
            // feedback
            val V = instancia.obtenerNumeroDeVertices()
            val E = instancia.obtenerNumeroDeLados()
            for (i in 1..V) println("${File(rutaArchivo).name} GrafoNoDirigidoCosto V=$V E=$E $i:${getVecinos(instancia, i)}")
            instancia
        } catch (e: Exception) {
            println("Error al instanciar la clase: ${e.message}")
            null
        }
    }

    // Cargar los grafos de amigos y candidatos
    var amigos = getGrafo(File(amigos_txt).absolutePath)
    var candidatos = getGrafo(File(candidatos_txt).absolutePath)

    // Funcion para obtener los usuarios con más amigos
    fun obtenerUsuariosMasAmigos(g: Grafo): List<Int> {
        val V = g.obtenerNumeroDeVertices()
        var maxAmigos = 0
        var usuarios = mutableListOf<Int>()
        // Recorremos todos los vértices para obtener el máximo número de amigos
        for (i in 1..V) {
            val amigos = g.adyacentes(i).count()
            if (amigos > maxAmigos) {
                maxAmigos = amigos
                usuarios = mutableListOf(i)
            } else if (amigos == maxAmigos) {
                usuarios.add(i)
            }
        }
        return usuarios
    }

    // Funcion para obtener los usuarios con menos amigos
    fun obtenerUsuariosMenosAmigos(g: Grafo): List<Int> {
        val V = g.obtenerNumeroDeVertices()
        var minAmigos = Int.MAX_VALUE
        var usuarios = mutableListOf<Int>()
        // Recorremos todos los vértices para obtener el mínimo número de amigos
        for (i in 1..V) {
            val amigos = g.adyacentes(i).count()
            if (amigos < minAmigos) {
                minAmigos = amigos
                usuarios = mutableListOf(i)
            } else if (amigos == minAmigos) {
                usuarios.add(i)
            }
        }
        return usuarios
    }

    // Funcion para obtener la comunidad de un usuario
    fun obtenerComunidad(g: Grafo, u: Int): List<Int> {
        // Recorremos el grafo con un BFS para obtener la comunidad
        val V = g.obtenerNumeroDeVertices()
        val visitado = BooleanArray(V + 1)
        val comunidad = mutableListOf<Int>()
        val cola = ArrayDeque<Int>()
        cola.add(u)
        visitado[u] = true
        while (cola.isNotEmpty()) {
            val v = cola.poll()
            comunidad.add(v)
            for (w in getVecinos(g, v)) {
                if (!visitado[w]) {
                    visitado[w] = true
                    cola.add(w)
                }
            }
        }
        return comunidad
    }

    // Funcion para obtener el total de comunidades
    fun obtenerTotalDeComunidades(g: Grafo): Int {
        // Recorremos el grafo para obtener el total de comunidades
        val V = g.obtenerNumeroDeVertices()
        val visitado = BooleanArray(V + 1)
        var comunidades = 0
        for (i in 1..V) {
            if (!visitado[i]) {
                val comunidad = obtenerComunidad(g, i)
                for (v in comunidad) {
                    visitado[v] = true
                }
                comunidades++
            }
        }
        return comunidades
    }

    // Funcion para calcular el grado de cercanía
    fun calcularGradoDeCercania(amigos: Grafo, candidatos: Grafo): Map<Int, List<Pair<Int, Int>>> {
        val V = amigos.obtenerNumeroDeVertices()
        val cercania = mutableMapOf<Int, MutableList<Pair<Int, Int>>>()

        // Funcion para realizar un BFS para obtener el grado de cercanía entre dos usuarios
        fun bfs(u: Int, v: Int): Int {
            val visitado = BooleanArray(V + 1)
            val distancia = IntArray(V + 1) { Int.MAX_VALUE }
            val cola = ArrayDeque<Int>()
            cola.add(u)
            visitado[u] = true
            distancia[u] = 0
            // Realizamos el BFS, buscando el vértice v y retornando su distancia
            while (cola.isNotEmpty()) {
                val actual = cola.poll()
                for (vecino in getVecinos(amigos, actual)) {
                    if (!visitado[vecino]) {
                        visitado[vecino] = true
                        distancia[vecino] = distancia[actual] + 1
                        cola.add(vecino)
                    }
                }
            }
            return distancia[v]
        }

        // Recorremos todos los usuarios para calcular el grado de cercanía
        for (i in 1..V) {
            cercania[i] = mutableListOf()
            for (j in getVecinos(candidatos, i)) {
                val grado = bfs(i, j)
                cercania[i]!!.add(Pair(j, grado))
            }
            cercania[i]!!.sortWith(compareBy({ it.second }, { it.first }))
        }
        return cercania
    }

    // Funcion para imprimir los integranes de las comunidades
    fun imprimirCandidatos(cercania: Map<Int, List<Pair<Int, Int>>>) {
        for ((usuario, candidatos) in cercania) {
            println("             USUARIO $usuario")
            var index = 1
            for ((candidato, grado) in candidatos) {
                val gradoStr = if (grado == Int.MAX_VALUE) "∞" else (grado - 1).toString()
                println("                     $index:$candidato:$gradoStr")
                index++
            }
        }
    }

    // Comienza el informe
    val usuariosMasAmigos = obtenerUsuariosMasAmigos(amigos!!)
    val usuariosMenosAmigos = obtenerUsuariosMenosAmigos(amigos!!)
    println("")
    println("--------------------")
    println("")
    println("INFORME I♥CATS")
    println("      USUARIOS CON MAS AMIGOS=" + usuariosMasAmigos.size)
    var i = 1
    for (vertice in usuariosMasAmigos) {
        println("             $i:$vertice:${amigos!!.adyacentes(vertice).count()}:${getVecinos(amigos!!, vertice)}")
        i++
    }
    i = 1
    println("      USUARIOS CON MENOS AMIGOS=" + usuariosMenosAmigos.size)
    for (vertice in usuariosMenosAmigos) {
        println("             $i:$vertice:${amigos!!.adyacentes(vertice).count()}:${getVecinos(amigos!!, vertice)}")
        i++
    }

    println("      COMUNIDADES DE AMIGOS=" + obtenerTotalDeComunidades(amigos!!))
    val V = amigos!!.obtenerNumeroDeVertices()
    val visitado = BooleanArray(V + 1)
    var j = 1
    for (i in 1..V) {
        if (!visitado[i]) {
            val comunidad = obtenerComunidad(amigos!!, i)
            println("             COMUNIDAD $j")
            var k = 1
            for (v in comunidad) {
                println("                     $k:$v:${amigos!!.adyacentes(v).count()}:${getVecinos(amigos!!, v)}")
                visitado[v] = true
                k++
            }
            j++
        }
    }

    println("       LISTA DE <<CANDIDATOS A AMIGOS>> POR USUARIO")
    val cercania = calcularGradoDeCercania(amigos!!, candidatos!!)
    imprimirCandidatos(cercania)
}
