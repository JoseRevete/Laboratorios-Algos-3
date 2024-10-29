import ve.usb.libGrafo.*
import Jama.Matrix
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import java.util.ArrayDeque
import kotlin.reflect.full.primaryConstructor
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    fun seleccionaArchivoTxt(prompt:String): String?{
        val directory = File(".")
        val txtFiles = directory.listFiles{ _, name -> name.endsWith(".txt")}?.sortedBy{it.name}
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
            return selectedFile  as String?
        }
        return null
    }
    val amigos_txt = seleccionaArchivoTxt("Seleccionar archivo de amigos.txt")
    if(amigos_txt == null) exitProcess(1)
    val candidatos_txt = seleccionaArchivoTxt("Seleccionar archivo de candidatos.txt")
    if(candidatos_txt == null) exitProcess(1)
    /*
    *********************************************************************************
    *********************************************************************************
    CUALQUIER CODIGO PARA HACER TRAZA/SEGUIMIENTO INTERNO
    *********************************************************************************
    *********************************************************************************
    */
    println("amigos=$amigos_txt\ncandidatos=$candidatos_txt")
    /*
    *********************************************************************************
    *********************************************************************************
    A PARTIR AQUI EL CODIGO QUE SOPORTE LA GENERACION DEL REPORTE PARA I♥CATS
    *********************************************************************************
    *********************************************************************************
    */
    println("INFORME I♥CATS")
 }