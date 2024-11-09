import ve.usb.libGrafo.*
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import kotlin.system.exitProcess

val clases=arrayOf("GrafoNoDirigidoCosto","GrafoNoDirigido","GrafoDirigidoCosto","GrafoDirigido")
fun formatDouble(costo: Double)=if(costo%1==0.0) costo.toInt().toString() else costo.toString()
fun formatLista(t:List<Triple<Int,Int,Double>>)=t.joinToString(separator=", "){(a,b,w)->"($a, $b, ${formatDouble(w)})"}

fun seleccionaArchivoTxt(prompt:String):String?{
	val directory = File(".")
	val txtFiles = directory.listFiles{ _, name -> clases.any { name.contains(it) } && name.endsWith(".txt")}?.sortedBy{it.name}
	if (txtFiles != null && txtFiles.isNotEmpty()) {
		val fileChooser = JFileChooser(directory)
		fileChooser.dialogTitle = "Selecciona un archivo TXT"
		fileChooser.isMultiSelectionEnabled = false
		fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
		val options = txtFiles.map{it.name}.toTypedArray()
		val selectedFile = JOptionPane.showInputDialog(
			null,
			prompt,
			"Selector de archivos *.txt",
			JOptionPane.PLAIN_MESSAGE,
			null,
			options,
			options[0]
		)
		return selectedFile  as String?
	}
	return null
}

fun getMSTbyPrim(g: GrafoNoDirigidoCosto): List<Pair<List<Int>, List<Triple<Int, Int, Double>>>> {
    val mst = mutableListOf<Triple<Int, Int, Double>>()
    val visited = mutableSetOf<Int>()
    val pq = java.util.PriorityQueue<Triple<Int, Int, Double>>(compareBy { it.third })

    fun visit(v: Int) {
        visited.add(v)
        for (arista in g.adyacentes(v)) {
            if (arista.b !in visited) {
                pq.add(Triple(v, arista.b, arista.costo))
            }
        }
    }

    // Start from vertex 1
    visit(1)
    while (pq.isNotEmpty() && visited.size < g.obtenerNumeroDeVertices()) {
        val (u, v, costo) = pq.poll()
        if (v !in visited) {
            mst.add(Triple(u, v, costo))
            visit(v)
        }
    }

    val componentesConexas = mutableListOf<List<Int>>()
    val aristasPorComponente = mutableListOf<List<Triple<Int, Int, Double>>>()
    componentesConexas.add(visited.toList())
    aristasPorComponente.add(mst)

    return listOf(Pair(componentesConexas.flatten(), aristasPorComponente.flatten()))
}

fun main(args: Array<String>) {
	val t = seleccionaArchivoTxt("Seleccionar archivo de grafo")?:exitProcess(1)
	val g = GrafoNoDirigidoCosto(t)?:exitProcess(1)
	println("$t $g")
	getMSTbyPrim(g).forEach{println("CC=${it.first} E=[${formatLista(it.second)}] W=${formatDouble(it.second.sumOf{it.third})}")}
 }