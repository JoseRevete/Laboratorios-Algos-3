import ve.usb.libGrafo.*
import Jama.Matrix
import java.io.File
import kotlin.reflect.full.primaryConstructor

fun getGrafo(rutaArchivo: String): Grafo? { // para leer el grafo de un archivo. De acuerdo al nombre en el archivo se crea el tipo de grafo
    val clases = arrayOf("GrafoNoDirigidoCosto","GrafoNoDirigido","GrafoDirigidoCosto","GrafoDirigido")
    for (tipoGrafo in clases) {
        if (rutaArchivo.contains(tipoGrafo)) {
            return try {
                val clase = Class.forName("ve.usb.libGrafo.${tipoGrafo}").kotlin
                val constructor = clase.constructors.find{it.parameters.size == 1 && it.parameters[0].type.classifier == String::class}
                val instancia = constructor?.call(rutaArchivo) as Grafo
                // feedback
                val V = instancia.obtenerNumeroDeVertices()
                val E = instancia.obtenerNumeroDeLados()
                for(i in 1..V) println("${File(rutaArchivo).name} $tipoGrafo V=$V E=$E $i:${instancia.adyacentes(i).map{it.elOtroVertice(i)}}")
                instancia
            } catch (e: Exception) {
                println("Error al instanciar la clase: ${e.message}")
                null
            }
        }
    }
    return null
}

fun getMatrizDeAdyacencia(g: Grafo): Matrix {
    val n = g.obtenerNumeroDeVertices()
    val A = Matrix(n, n) // el constructor por defecto inicializa en ceros
    for (i in 0 until n) {
        try {
            var adyacentes = g.adyacentes(i + 1)
            val lados = adyacentes.map{it.elOtroVertice(i+1)}
            for (lado in lados) {
                A[i, lado - 1] = 1.0
            }
        } catch (e: RuntimeException) {
            continue
        }
    }
    return A
}

fun getMatrizDeAlcance(A: Matrix): Matrix {
    val R = A.copy()
    val n = R.rowDimension
    val I = Matrix.identity(n, n)
    R.plusEquals(I)
    
    for (k in 0 until n) {
        for (i in 0 until n) {
            if (R[i, k] == 1.0 && (i != k)){
                for (j in 0 until n) {
                    if (R[i, j] != 1.0){
                        R[i, j] = R[i, j] + R[k, j]
                    }
                }
            }
            
        }
    }
    
    return R
}

fun getComponentesConexas(g: Grafo): List<List<Int>> { //Construir una lista de lista de los rótulos (Int) de los nodos con las componentes conexas
    val n = g.obtenerNumeroDeVertices()
    val A = getMatrizDeAdyacencia(g) // construir la matriz de adyacencia (A)
    val R = getMatrizDeAlcance(A) // construir la matriz de alcance (R)
    print("\nMatriz de adyacencia (A) $n x $n"); A.print(0,0); print("\nMatriz de alcance (R) $n x $n"); R.print(0,0) // feedback
    // Utilizar la matriz C (copia de R) para extraer las componentes conexas
    val C = R.copy()
    val esNoDirigido = g is GrafoNoDirigido || g is GrafoNoDirigidoCosto
    val visited = BooleanArray(n) // El constructor por defecto inicializa en False
    val components = mutableListOf<List<Int>>()
    for (i in 0 until n) {
        C.set(i, i, 1.0)
        if (!visited[i]) {
            val componente = mutableListOf<Int>()
            val cola = ArrayDeque<Int>()
            cola.add(i)
            visited[i] = true

            while (cola.isNotEmpty()) {
                val nodo = cola.removeFirst()
                componente.add(nodo + 1)

                for (j in 0 until n) {
                    val esPosibleAdyacente = if (esNoDirigido) {
                        C[nodo, j] == 1.0 && !visited[j]
                    } else {
                        C[nodo, j] == 1.0 && C[j, nodo] == 1.0 && !visited[j]
                    }
                    if (esPosibleAdyacente) {
                        cola.add(j)
                        visited[j] = true
                    }
                }
            }
            components.add(componente)
        }
    }
    return components
}

fun main(args: Array<String>) {
    File(".").listFiles{_,name->name.endsWith(".txt")}?.sortedBy{it.name}?.forEach{file->
        println("Archivo: ${file.absolutePath}")
        getGrafo(file.absolutePath)?.let{grafo->
            print("Componentes conexas: ${getComponentesConexas(grafo)}\n\n${"*".repeat(120)}\n")
        }?: print("No se pudo crear la instancia del grafo para el archivo ${file.name}.")
    }
 }