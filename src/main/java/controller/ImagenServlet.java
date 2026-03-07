package controller;

import dao.ProductoDAO;
import model.Producto;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

@WebServlet("/imagen-producto/*")
public class ImagenServlet extends HttpServlet {

    private ProductoDAO productoDAO;

    @Override
    public void init() {
        productoDAO = new ProductoDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            servirImagenDefault(response);
            return;
        }

        // Limpia el path por si viene con espacios o caracteres raros
        String idStr = pathInfo.substring(1).trim();

        if (!idStr.matches("\\d+")) {
            servirImagenDefault(response);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Producto producto = productoDAO.obtenerPorId(id);

            if (producto != null
                    && producto.getImagenData() != null
                    && producto.getImagenData().length > 0) {

                // Valida que el tipo MIME sea seguro, evita headers maliciosos
                String tipo = resolverTipoMime(producto.getImagenTipo());

                response.setContentType(tipo);
                response.setContentLengthLong(producto.getImagenData().length);

                // Cache de 1 año para imágenes que no cambian seguido
                response.setHeader("Cache-Control", "public, max-age=31536000");

                // ✅ Mejora: usa BufferedOutputStream para escribir más rápido
                try (BufferedOutputStream out =
                             new BufferedOutputStream(response.getOutputStream(), 8192)) {
                    out.write(producto.getImagenData());
                    out.flush();
                }

            } else {
                servirImagenDefault(response);
            }

        } catch (NumberFormatException e) {
            // ID demasiado grande para int, igual lo manejamos
            servirImagenDefault(response);
        } catch (Exception e) {
            // Log del error real para debugging, no lo silencies completamente
            log("Error al servir imagen para id=" + pathInfo, e);
            servirImagenDefault(response);
        }
    }

    /**
     * Valida y normaliza el tipo MIME para evitar valores inesperados en el header.
     * Solo permite tipos de imagen conocidos.
     */
    private String resolverTipoMime(String tipoGuardado) {
        if (tipoGuardado == null) return "image/jpeg";

        switch (tipoGuardado.toLowerCase().trim()) {
            case "image/jpeg":
            case "image/jpg":  return "image/jpeg";
            case "image/png":  return "image/png";
            case "image/webp": return "image/webp";
            case "image/gif":  return "image/gif";
            default:           return "image/jpeg"; // fallback seguro
        }
    }

    private void servirImagenDefault(HttpServletResponse response) throws IOException {
        if (response.isCommitted()) return;
        String path = getServletContext().getRealPath("/assets/Imagenes/default.png");
        File f = new File(path);

        if (!f.exists()) {
            // fallback gif 1x1 si tampoco encuentra el default
            response.setContentType("image/gif");
            byte[] gif1x1 = {
                0x47,0x49,0x46,0x38,0x39,0x61,0x01,0x00,
                0x01,0x00,(byte)0x80,0x00,0x00,(byte)0xff,
                (byte)0xff,(byte)0xff,0x00,0x00,0x00,0x21,
                (byte)0xf9,0x04,0x00,0x00,0x00,0x00,0x00,
                0x2c,0x00,0x00,0x00,0x00,0x01,0x00,0x01,
                0x00,0x00,0x02,0x02,0x44,0x01,0x00,0x3b
            };
            response.setContentLength(gif1x1.length);
            response.getOutputStream().write(gif1x1);
            return;
        }

        response.setContentType("image/png"); // ← png, no jpeg
        response.setContentLengthLong(f.length());
        try (InputStream  in  = new BufferedInputStream(new FileInputStream(f), 8192);
             OutputStream out = new BufferedOutputStream(response.getOutputStream(), 8192)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            out.flush();
        }
    }
}