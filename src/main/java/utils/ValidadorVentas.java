package utils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Validador de registros de venta.
 * Cumple con RF22: Registro de Ventas.
 */
public class ValidadorVentas {

    /**
     * Valida todos los campos de un registro de venta.
     * @param nombreCliente nombre del cliente
     * @param telefono teléfono del cliente
     * @param fechaStr fecha de la venta
     * @param modalidad modalidad de pago (contado/anticipo)
     * @param montoAnticipo monto del anticipo si aplica
     * @param total total de la venta
     * @param req request para obtener productos
     * @return Map con errores encontrados (vacío si todo está correcto)
     */
    public static Map<String, String> validarRegistroVenta(
            String nombreCliente, String telefono, String fechaStr, 
            String modalidad, BigDecimal montoAnticipo, BigDecimal total,
            HttpServletRequest req) {
        
        Map<String, String> errores = new HashMap<>();

        // Validar cliente
        if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
            errores.put("nombreCliente", "El nombre del cliente es obligatorio");
        }
        
        if (telefono != null && !telefono.trim().isEmpty()) {
            if (!telefono.matches("^\\d{10,15}$")) {
                errores.put("telefono", "Teléfono inválido (10-15 dígitos)");
            }
        }

        // Validar fecha
        try {
            Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaStr);
            if (fecha.after(new Date())) {
                errores.put("fechaEmision", "La fecha no puede ser futura");
            }
        } catch (Exception e) {
            errores.put("fechaEmision", "Formato de fecha inválido");
        }

        // Validar modalidad y anticipo
        if ("anticipo".equals(modalidad)) {
            if (montoAnticipo == null || montoAnticipo.compareTo(BigDecimal.ZERO) <= 0) {
                errores.put("montoAnticipo", "El anticipo debe ser mayor a cero");
            }
            if (total != null && montoAnticipo != null && 
                montoAnticipo.compareTo(total) >= 0) {
                errores.put("montoAnticipo", "El anticipo debe ser menor al total");
            }
        }

        // Validar productos
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

    /**
     * Valida que el stock sea suficiente para la venta.
     * @param stockDisponible stock actual del producto
     * @param cantidadSolicitada cantidad que se quiere vender
     * @return boolean true si hay stock suficiente
     */
    public static boolean validarStock(int stockDisponible, int cantidadSolicitada) {
        return stockDisponible >= cantidadSolicitada && cantidadSolicitada > 0;
    }

    /**
     * Valida que el precio de venta sea mayor o igual al costo.
     * @param precioCosto precio de costo
     * @param precioVenta precio de venta
     * @return boolean true si la validación pasa
     */
    public static boolean validarPrecio(BigDecimal precioCosto, BigDecimal precioVenta) {
        return precioVenta != null && precioCosto != null && 
               precioVenta.compareTo(precioCosto) >= 0;
    }
}