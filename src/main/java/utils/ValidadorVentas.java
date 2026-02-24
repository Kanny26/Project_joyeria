package utils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ValidadorVentas {

    public static Map<String, String> validarRegistroVenta(
            String nombreCliente, String telefono, String fechaStr, 
            String modalidad, BigDecimal montoAnticipo, HttpServletRequest req) {

        Map<String, String> errores = new HashMap<>();

        if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
            errores.put("nombreCliente", "El nombre del cliente es obligatorio");
        }
        if (telefono == null || !telefono.matches("^\\d{10,15}$")) {
            errores.put("telefono", "Teléfono inválido (10-15 dígitos)");
        }

        try {
            Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);
            if (fecha.after(new Date())) {
                errores.put("fechaEmision", "La fecha no puede ser futura");
            }
        } catch (Exception e) {
            errores.put("fechaEmision", "Formato de fecha inválido");
        }

        if ("anticipo".equals(modalidad)) {
            if (montoAnticipo == null || montoAnticipo.compareTo(BigDecimal.ZERO) <= 0) {
                errores.put("montoAnticipo", "El anticipo debe ser mayor a cero");
            }
        }

        String[] productoIds = req.getParameterValues("productoId");
        String[] cantidades = req.getParameterValues("cantidad");

        if (productoIds == null || productoIds.length == 0) {
            errores.put("productos", "Debe seleccionar al menos un producto");
        } else {
            for (int i = 0; i < productoIds.length; i++) {
                try {
                    int cant = Integer.parseInt(cantidades[i]);
                    if (cant <= 0) {
                        errores.put("cantidad_" + i, "Cantidad inválida");
                    }
                } catch (NumberFormatException e) {
                    errores.put("cantidad_" + i, "Cantidad debe ser numérica");
                }
            }
        }

        return errores;
    }
}