package controller;

import dao.ProductoDAO;
import model.Producto;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;

/**
 * Sirve las imágenes de productos directamente desde la base de datos.
 * Se accede mediante la URL /imagen-producto/{id}, donde {id} es el ID del producto.
 * Esta URL se usa en categoria.jsp dentro de las tarjetas de cada producto.
 */
@WebServlet("/imagen-producto/*")
public class ImagenServlet extends HttpServlet {

    private ProductoDAO productoDAO;

    /**
     * Inicializa el servlet instanciando el objeto de acceso a datos de productos.
     * Este método es llamado automáticamente por el contenedor de servlets
     * cuando el servlet es cargado por primera vez.
     */
    @Override
    public void init() {
        productoDAO = new ProductoDAO();
    }

    /**
     * Maneja las peticiones GET para servir imágenes de productos.
     * Extrae el ID del producto de la URL, consulta la imagen en la base de datos
     * y la envía al cliente con los encabezados HTTP apropiados.
     * Si el producto no existe o no tiene imagen, sirve una imagen por defecto.
     *
     * @param request objeto HttpServletRequest que contiene la petición del cliente
     * @param response objeto HttpServletResponse para enviar la imagen al cliente
     * @throws ServletException si ocurre un error en el procesamiento del servlet
     * @throws IOException si ocurre un error de entrada/salida durante el manejo
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // getPathInfo() retorna la parte de la URL después del patrón del servlet.
        // Ejemplo: si la URL es /imagen-producto/42, pathInfo = "/42"
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            servirImagenDefault(response);
            return;
        }

        // Se extrae el ID eliminando la barra inicial. trim() limpia espacios o caracteres raros.
        String idStr = pathInfo.substring(1).trim();

        // matches("\\d+") verifica que el ID sea solo dígitos antes de convertirlo.
        // Esto evita un NumberFormatException y también previene peticiones malformadas.
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

                // Seguridad: resolverTipoMime valida el tipo de imagen antes de enviarlo
                // en la cabecera HTTP. Evita que un tipo inválido o malicioso llegue al navegador.
                String tipo = resolverTipoMime(producto.getImagenTipo());

                response.setContentType(tipo);
                response.setContentLengthLong(producto.getImagenData().length);

                // Cache de 1 año para imágenes que raramente cambian,
                // reduciendo peticiones innecesarias al servidor.
                response.setHeader("Cache-Control", "public, max-age=31536000");

                // BufferedOutputStream mejora el rendimiento escribiendo en bloques de 8KB
                // en lugar de byte por byte.
                try (BufferedOutputStream out =
                             new BufferedOutputStream(response.getOutputStream(), 8192)) {
                    out.write(producto.getImagenData());
                    out.flush();
                }

            } else {
                servirImagenDefault(response);
            }

        } catch (NumberFormatException e) {
            // El ID era demasiado grande para un int; se sirve la imagen por defecto.
            servirImagenDefault(response);
        } catch (Exception e) {
            // Se registra el error en el log del servidor para facilitar el diagnóstico,
            // sin exponer detalles técnicos al navegador.
            log("Error al servir imagen para id=" + pathInfo, e);
            servirImagenDefault(response);
        }
    }

    /**
     * Valida y normaliza el tipo MIME recibido de la BD.
     * Solo permite tipos de imagen conocidos para evitar valores inesperados en la cabecera HTTP.
     * Si el tipo no es reconocido, retorna image/jpeg como valor seguro por defecto.
     *
     * @param tipoGuardado el tipo MIME almacenado en la base de datos para la imagen
     * @return el tipo MIME normalizado y validado para usar en la respuesta HTTP
     */
    private String resolverTipoMime(String tipoGuardado) {
        if (tipoGuardado == null) return "image/jpeg";

        switch (tipoGuardado.toLowerCase().trim()) {
            case "image/jpeg":
            case "image/jpg":  return "image/jpeg";
            case "image/png":  return "image/png";
            case "image/webp": return "image/webp";
            case "image/gif":  return "image/gif";
            default:           return "image/jpeg";
        }
    }

    /**
     * Sirve la imagen por defecto (default.png) cuando no hay imagen del producto.
     * Si tampoco existe el archivo default.png, retorna un GIF transparente de 1x1 pixel
     * como último recurso para que la etiqueta img del HTML no quede rota.
     *
     * @param response el objeto HttpServletResponse donde se escribirá la imagen por defecto
     * @throws IOException si ocurre un error al leer el archivo o escribir en la respuesta
     */
    private void servirImagenDefault(HttpServletResponse response) throws IOException {
        if (response.isCommitted()) return;
        String path = getServletContext().getRealPath("/assets/Imagenes/default.png");
        File f = new File(path);

        if (!f.exists()) {
            // GIF mínimo de 1x1 píxel transparente codificado en bytes.
            // Es el fallback más pequeño posible para no mostrar imagen rota.
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

        response.setContentType("image/png");
        response.setContentLengthLong(f.length());
        // Se usan streams con buffer para leer y escribir el archivo en bloques eficientes.
        try (InputStream  in  = new BufferedInputStream(new FileInputStream(f), 8192);
             OutputStream out = new BufferedOutputStream(response.getOutputStream(), 8192)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            out.flush();
        }
    }
}