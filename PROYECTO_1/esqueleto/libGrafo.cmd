@echo off
kotlinc -d . ve/usb/libGrafo/Arco.kt ve/usb/libGrafo/ArcoCosto.kt ve/usb/libGrafo/Arista.kt ve/usb/libGrafo/AristaCosto.kt ve/usb/libGrafo/Grafo.kt ve/usb/libGrafo/GrafoDirigido.kt ve/usb/libGrafo/GrafoDirigidoCosto.kt ve/usb/libGrafo/GrafoNoDirigido.kt ve/usb/libGrafo/GrafoNoDirigidoCosto.kt ve/usb/libGrafo/Lado.kt && jar cf libs/libGrafo.jar -C . . && rmdir /s /q META-INF