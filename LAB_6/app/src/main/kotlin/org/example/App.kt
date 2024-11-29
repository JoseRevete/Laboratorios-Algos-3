package org.example // generado por Gradle como parte del esqueleto inicial
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.JFrame
import kotlin.concurrent.timer
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.DataFrame

class App {val greeting: String get(){return "Hello World!"}} // generado por Gradle como parte del esqueleto inicial

const val TIME_LM = 5 // tiempo límite en segundos
const val TSP_DIR = "C:/TEMP/TSP05/TSP" // carpeta con tests TSP (en caso que no pueda encontrarse en ../tsp)
val tspAlgoritmos = listOf("fuerza_bruta", "mejor_vecino", "insercion1", "insercion2") // algoritmos implementados

fun seleccionarTsp(): List<String> {
    var targetDirectory = Paths.get("").toAbsolutePath().resolve("../tsp").normalize()
    if (!Files.exists(targetDirectory) || !Files.isDirectory(targetDirectory)) {
        targetDirectory = Paths.get(TSP_DIR) 
    }
    val tspDir = targetDirectory.toFile()
    val fileChooser = JFileChooser(tspDir)
    fileChooser.dialogTitle = "Seleccionar archivos *.tsp de $tspDir"
    fileChooser.fileFilter = FileNameExtensionFilter("Archivos de tsp (*.tsp)", "tsp")
    fileChooser.isMultiSelectionEnabled = true
    val frame = JFrame()
    frame.isVisible = false
    val result = fileChooser.showOpenDialog(frame)
    frame.dispose()
    return if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFiles.map { it.absolutePath }
    } else {
        emptyList()
    }
}

fun <T> List<T>.permutations(): Sequence<List<T>> = sequence {
    if (this@permutations.isEmpty()) {
        yield(emptyList())
    } else {
        for (i in this@permutations.indices) {
            val element = this@permutations[i]
            val rest = this@permutations.take(i) + this@permutations.drop(i + 1)
            for (perm in rest.permutations()) {
                yield(listOf(element) + perm)
            }
        }
    }
}

class tspTest(filename: String){
    var name: String
    var optimo: Double
    var comentario: String
    var matriz: List<List<Double>>
    var n : Int
    init {
        val lines = File(filename).readLines()
        name = lines[0].trim()
        optimo = lines[1].trim().toDouble()
        comentario = lines[2].trim()
        matriz = lines.drop(3).map{it.split(" ").map(String::toDouble)}
        n = matriz.size
    }
}

class tspExec(testinst: tspTest, algname: String) {
    val algoritmos = mapOf(
        "fuerza_bruta" to ::fuerzaBruta,
        "mejor_vecino" to ::mejorVecino,
        "insercion1" to ::insercion1,
        "insercion2" to ::insercion2
    )
    var algoritmo: String
    var efic: Double
    var mejorDist: Double
    var mejorRuta: List<Int>?
    var salir : Boolean
    var test : tspTest
    var tiempoIni : Long
    var tiempoFin : Long
    var temporizador: Timer

    private fun interrupcion() {
        salir = true
    }

    init {
        if (algname in algoritmos) {
            algoritmo = algname
            efic = 0.0
            mejorDist = Double.POSITIVE_INFINITY
            mejorRuta = mutableListOf()
            salir = false
            test = testinst
            tiempoIni = System.currentTimeMillis()
            temporizador = timer("temporizador",false,TIME_LM*1000L,TIME_LM*1000L){interrupcion()}
            algoritmos[algname]?.invoke()
            tiempoFin = System.currentTimeMillis()
        } else {
            throw IllegalArgumentException("Algoritmo '$algname' no existe.")
        }
    }

    override fun toString(): String {
        val info = "${test.name} $algoritmo d=%.2f o=%.2f e=%.2f t=%.2fs".format(mejorDist,test.optimo,efic,(tiempoFin-tiempoIni)/1000.0)
        val localMejorRuta = mejorRuta
        if (localMejorRuta.isNullOrEmpty()){return info}
        val i = localMejorRuta.indexOf(0)
        val rutaNormal = localMejorRuta.subList(i, localMejorRuta.size) + localMejorRuta.subList(0, i)
        val rutaInvers = listOf(rutaNormal[0]) + rutaNormal.subList(1, rutaNormal.size).reversed()
        return "$info ${if (rutaNormal[1] < rutaInvers[1]) rutaNormal else rutaInvers}}"
    }

    private fun calcDist(ruta: List<Int>): Double {
        var dist = 0.0
        for (i in 0 until ruta.size - 1) dist += test.matriz[ruta[i]][ruta[i + 1]]
        dist += test.matriz[ruta.last()][ruta[0]]
        return dist
    }

    private fun updMejor(dist: Double, ruta: List<Int>) {
        mejorDist = dist
        mejorRuta = ruta
        efic = mejorDist / test.optimo
    }

    private fun fuerzaBruta() {
        try {
            val ciudades = (0 until test.n).toList()
            for (ruta in ciudades.permutations()) {
                val distancia = calcDist(ruta)
                if (distancia < mejorDist) updMejor(distancia, ruta)
                if(salir)break
            }
        }catch(e:Exception){println(e)}finally{temporizador.cancel()}
    }

    // Función privada para el algoritmo de mejor vecino
    private fun mejorVecino() {
        try {
            // Aplicamos un ciclo para cada ciudad de inicio
            for (start in 0 until test.n) {
                val ruta = mutableListOf(start)
                val visitadas = mutableSetOf(start)
                var actual = start
                // Mientras no se hayan visitado todas las ciudades, nos movemos a la ciudad más cercana
                while (ruta.size < test.n) {
                    val siguiente = (0 until test.n)
                        .filter { it !in visitadas }
                        .minByOrNull { test.matriz[actual][it] } ?: break
                    ruta.add(siguiente)
                    visitadas.add(siguiente)
                    actual = siguiente
                }
                // Calculamos la distancia de la ruta y actualizamos si es la mejor
                val distancia = calcDist(ruta)
                if (distancia < mejorDist) updMejor(distancia, ruta)
                if (salir) break
            }
        }catch(e: Exception){println(e)} finally {temporizador.cancel()}
    }

    // Función privada para el algoritmo de inserción 1
    private fun insercion1() {
        try {
            // Inicializamos la ruta con las dos primeras ciudades
            val ruta = mutableListOf(0, 1)
            // Aplicamos un ciclo para cada ciudad restante
            for (ciudad in 2 until test.n) {
                var mejorPos = 0
                var menorIncremento = Double.POSITIVE_INFINITY
                // Calculamos el incremento de distancia al insertar la ciudad en cada posición
                for (i in 1 until ruta.size) {
                    val incremento = test.matriz[ruta[i - 1]][ciudad] + test.matriz[ciudad][ruta[i]] - test.matriz[ruta[i - 1]][ruta[i]]
                    // Actualizamos la mejor posición si el incremento es menor
                    if (incremento < menorIncremento) {
                        menorIncremento = incremento
                        mejorPos = i
                    }
                }
                ruta.add(mejorPos, ciudad)
            }
            // Calculamos la distancia de la ruta y actualizamos si es la mejor
            val distancia = calcDist(ruta)
            if (distancia < mejorDist) updMejor(distancia, ruta)
        }catch(e:Exception){println(e)}finally{temporizador.cancel()}
    }

    // Función privada para el algoritmo de inserción 2
    private fun insercion2() {
        try {
            // Inicializamos la ruta con las dos primeras ciudades
            val ruta = mutableListOf(0, 1)
            val restantes = (2 until test.n).toMutableList()
            // Aplicamos un ciclo mientras queden ciudades por visitar
            while (restantes.isNotEmpty()) {
                // Seleccionamos la ciudad que minimiza la distancia al insertarla en la ruta
                val ciudad = restantes.minByOrNull { c ->
                    ruta.minOf { r -> test.matriz[c][r] }
                } ?: break
                restantes.remove(ciudad)
                // Insertamos la ciudad en la mejor posición
                var mejorPos = 0
                var menorIncremento = Double.POSITIVE_INFINITY
                // Calculamos el incremento de distancia al insertar la ciudad en cada posición
                for (i in 1 until ruta.size) {
                    val incremento = test.matriz[ruta[i - 1]][ciudad] + test.matriz[ciudad][ruta[i]] - test.matriz[ruta[i - 1]][ruta[i]]
                    if (incremento < menorIncremento) {
                        menorIncremento = incremento
                        mejorPos = i
                    }
                }
                ruta.add(mejorPos, ciudad)
            }
            // Calculamos la distancia de la ruta y actualizamos si es la mejor
            val distancia = calcDist(ruta)
            if (distancia < mejorDist) updMejor(distancia, ruta)
        }catch(e:Exception){println(e)}finally{temporizador.cancel()}
    }
}

fun main() {
    val tspData = seleccionarTsp().flatMap { tspFile ->
        val test = tspTest(tspFile)
        tspAlgoritmos.map{algoritmo -> tspExec(test,algoritmo).also{println(it)}}
    }
    if (tspData.isNotEmpty()) {
        val df = dataFrameOf(
            "Test" to tspData.map { it.test.name },
            "Algoritmo" to tspData.map { it.algoritmo },
            "Mejor_Dist" to tspData.map { it.mejorDist },
            "Optimo" to tspData.map { it.test.optimo },
            "Eficiencia" to tspData.map { it.efic },
            "Tiempo" to tspData.map {(it.tiempoFin-it.tiempoIni)/1000.0}
        )
        df.print(rowsLimit=Int.MAX_VALUE,borders=true)
        val reporte = df.groupBy("Algoritmo").aggregate {
            mean("Eficiencia") into "Eficiencia_AVG"
            std("Eficiencia") into "Eficiencia_STD"
            mean("Tiempo") into "Tiempo_AVG"
            std("Tiempo") into "Tiempo_STD"
        }
        reporte.print(rowsLimit=Int.MAX_VALUE,borders=true)
    } else {
        println("No se seleccionaron archivos TSP.")
    }
}