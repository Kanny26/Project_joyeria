package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/imagenes/*")
public class ImagenServlet extends HttpServlet {

	private static final String RUTA_IMAGENES = "C:\\imagenes-joyas\\";


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String nombreImagen = request.getPathInfo(); // /archivo.jpg

        if (nombreImagen == null || nombreImagen.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        File file = new File(RUTA_IMAGENES + nombreImagen.substring(1));

        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = getServletContext().getMimeType(file.getName());
        response.setContentType(
                contentType != null ? contentType : "image/jpg"
        );

        Files.copy(file.toPath(), response.getOutputStream());
    }
}
