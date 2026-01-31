package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet encargado de servir imágenes almacenadas en el servidor.
 *
 * Ruta:
 *  - /imagenes/*
 *
 * Ejemplo:
 *  - /imagenes/foto.jpg
 *
 * Las imágenes se leen directamente desde el sistema de archivos
 * y se envían como respuesta HTTP.
 */
@WebServlet("/imagenes/*")
public class ImagenServlet extends HttpServlet {

    /**
     * Ruta física donde se almacenan las imágenes.
     * IMPORTANTE: debe existir y ser accesible por el servidor.
     */
    private static final String RUTA_IMAGENES = "C:\\imagenes-joyas\\";

    /**
     * Maneja peticiones GET para servir imágenes.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Obtener el nombre del archivo solicitado (/imagen.jpg)
        String nombreImagen = request.getPathInfo();

        // Validación básica de la ruta
        if (nombreImagen == null || nombreImagen.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Construir el archivo físico
        File file = new File(RUTA_IMAGENES + nombreImagen.substring(1));

        // Verificar existencia del archivo
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Detectar el tipo MIME de la imagen
        String contentType = getServletContext().getMimeType(file.getName());
        response.setContentType(
                contentType != null ? contentType : "image/jpeg"
        );

        // Enviar la imagen al cliente
        Files.copy(file.toPath(), response.getOutputStream());
    }
}
