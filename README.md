# ServidorNubeJava

Este proyecto es una implementación de un sistema cliente-servidor desarrollado en Java que utiliza Sockets TCP para permitir la transferencia y visualización de archivos de forma remota. La aplicación cuenta con una interfaz gráfica de usuario (GUI) construida con Swing que simula un explorador de archivos, así como un modo de operación por consola.

Este proyecto fue desarrollado como parte de la práctica para la materia de Redes de Computadoras II, con el objetivo de reforzar los conceptos de comunicación en red y la programación de sockets.

-----

## ✨ Características Principales

  * **Conexión Cliente-Servidor:** Comunicación robusta y fiable basada en el protocolo TCP.
  * **Interfaz Gráfica de Usuario (GUI):** Un explorador de archivos visual e intuitivo construido con Java Swing.
  * **Modo Consola:** Operación dual que permite ejecutar el cliente a través de la terminal, ideal para entornos sin interfaz gráfica.
  * **Vista en Mosaico:** Los archivos en el servidor se muestran en una cuadrícula con íconos representativos según su tipo (imagen, PDF, Word, etc.).
  * **Vista Previa de Imágenes:** Previsualización de archivos de imagen (JPG, PNG, etc.) directamente en la interfaz sin necesidad de descargarlos primero.
  * **Funcionalidad "Abrir Archivo":** Capacidad para abrir archivos del servidor (como PDFs o documentos de Word) con las aplicaciones predeterminadas del sistema operativo del cliente. El archivo se descarga automáticamente si es necesario.
  * **Conexión Remota:** Totalmente capaz de establecer conexiones a través de una red local (LAN) o Internet, no solo en `localhost`.

-----

## 🛠️ Tecnologías Utilizadas

  * **Lenguaje:** Java
  * **Interfaz Gráfica:** Java Swing
  * **Comunicación en Red:** Sockets TCP (`java.net.Socket`, `java.net.ServerSocket`)
  * **Serialización de Objetos:** `ObjectInputStream` y `ObjectOutputStream` para el envío de datos y archivos.

-----

## 🚀 Instalación y Ejecución

Sigue estos pasos para poner en marcha el proyecto.

### Prerrequisitos

  * Tener instalado el JDK (Java Development Kit) en su versión 8 o superior.

### Pasos

1.  **Clonar el Repositorio**

    ```sh
    git clone <URL-de-tu-repositorio>
    cd <nombre-de-la-carpeta>
    ```

2.  **Compilar el Proyecto**

      * Abre una terminal en la raíz del proyecto y ejecuta el siguiente comando para compilar todos los archivos `.java`:

    <!-- end list -->

    ```sh
    javac *.java
    ```

3.  **Ejecutar el Servidor**

      * En la misma terminal, inicia el servidor. Este se quedará esperando conexiones de clientes.
      * Crea una carpeta llamada `archivos_servidor` y coloca dentro los archivos que quieras compartir.

    <!-- end list -->

    ```sh
    java Servidor
    ```

4.  **Ejecutar el Cliente**

      * Abre una **nueva terminal** en la misma carpeta. Tienes dos opciones para ejecutar el cliente:

      * **Modo Gráfico (GUI):**

        ```sh
        java Cliente
        ```

        Aparecerá la ventana del explorador de archivos. Ingresa la dirección IP del servidor (o déjala como `localhost` si se ejecuta en la misma máquina) y conéctate.

      * **Modo Consola:**

        ```sh
        java Cliente --console
        ```

        El cliente se ejecutará en la terminal, mostrando un menú basado en texto para interactuar con el servidor.
