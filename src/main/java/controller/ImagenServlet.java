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

        String idStr = pathInfo.substring(1);
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

                String tipo = producto.getImagenTipo() != null
                        ? producto.getImagenTipo() : "image/jpeg";

                response.setContentType(tipo);
                response.setContentLengthLong(producto.getImagenData().length);
                response.setHeader("Cache-Control", "public, max-age=31536000");
                response.getOutputStream().write(producto.getImagenData());

            } else {
                servirImagenDefault(response);
            }
        } catch (Exception e) {
            servirImagenDefault(response);
        }
    }

    private void servirImagenDefault(HttpServletResponse response) throws IOException {
        String path = getServletContext().getRealPath("/imagenes/default.jpg");
        File f = new File(path);

        if (!f.exists()) {
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

        response.setContentType("image/jpeg");
        response.setContentLengthLong(f.length());

        try (InputStream in  = new FileInputStream(f);
             OutputStream out = response.getOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            out.flush();
        }
    }
}