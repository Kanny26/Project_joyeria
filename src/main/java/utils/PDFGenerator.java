package utils;

import model.Venta;
import model.DetalleVenta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;

public class PDFGenerator {

    private static final BaseColor COLOR_PRIMARIO = new BaseColor(153, 150, 197);
    private static final BaseColor COLOR_SECUNDARIO = new BaseColor(92, 61, 46);
    private static final BaseColor COLOR_EXITO = new BaseColor(47, 138, 47);
    private static final BaseColor COLOR_PENDIENTE = new BaseColor(236, 93, 93);

    public static byte[] generarFacturaPDF(Venta venta) throws Exception {
        if (venta == null || venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new Exception("Datos de venta inválidos para generar PDF");
        }

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
        Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE);

        // Encabezado
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell cellLogo = new PdfPCell(new Phrase("Abby.accesorios\nTu lugar favorito", fontTitulo));
        cellLogo.setBorder(Rectangle.NO_BORDER);
        header.addCell(cellLogo);

        PdfPCell cellInfo = new PdfPCell();
        cellInfo.setBorder(Rectangle.NO_BORDER);
        cellInfo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellInfo.addElement(new Phrase("No. Factura: " + venta.getVentaId(), fontNormal));
        cellInfo.addElement(Chunk.NEWLINE);
        cellInfo.addElement(new Phrase("Fecha: " + formatearFecha(venta.getFechaEmision()), fontNormal));
        cellInfo.addElement(new Phrase("Método: " + venta.getMetodoPago(), fontNormal));
        header.addCell(cellInfo);

        document.add(header);
        document.add(new Chunk(new DottedLineSeparator()));
        document.add(Chunk.NEWLINE);

        // Cliente
        document.add(new Phrase("Factura para:", fontSubtitulo));
        document.add(new Phrase(venta.getClienteNombre(), fontTitulo));
        if (venta.getTelefonoCliente() != null) {
            document.add(new Phrase("Tel: " + venta.getTelefonoCliente(), fontNormal));
        }
        document.add(new Phrase("Vendedor: " + venta.getVendedorNombre(), fontNormal));
        document.add(Chunk.NEWLINE);

        // Tabla de productos
        PdfPTable productosTable = new PdfPTable(5);
        productosTable.setWidthPercentage(100);
        productosTable.setWidths(new int[]{1, 5, 2, 1, 2});

        String[] headers = {"#", "Descripción", "P. Unitario", "Cant.", "Total"};
        for (String h : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(h, 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.WHITE)));
            headerCell.setBackgroundColor(COLOR_SECUNDARIO);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            productosTable.addCell(headerCell);
        }

        int contador = 1;
        for (DetalleVenta d : venta.getDetalles()) {
            productosTable.addCell(new PdfPCell(new Phrase(String.valueOf(contador++), fontNormal)));
            productosTable.addCell(new PdfPCell(new Phrase(d.getProductoNombre(), fontNormal)));
            
            PdfPCell cellPrecio = new PdfPCell(new Phrase(formatoMoneda(d.getPrecioUnitario()), fontNormal));
            cellPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
            productosTable.addCell(cellPrecio);
            
            PdfPCell cellCant = new PdfPCell(new Phrase(String.valueOf(d.getCantidad()), fontNormal));
            cellCant.setHorizontalAlignment(Element.ALIGN_CENTER);
            productosTable.addCell(cellCant);
            
            PdfPCell cellSubtotal = new PdfPCell(new Phrase(formatoMoneda(d.getSubtotal()), fontNormal));
            cellSubtotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            productosTable.addCell(cellSubtotal);
        }

        document.add(productosTable);
        document.add(Chunk.NEWLINE);

        // Totales
        PdfPTable totales = new PdfPTable(2);
        totales.setWidthPercentage(50);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL:", fontTotal));
        totalLabel.setBackgroundColor(COLOR_SECUNDARIO);
        totalLabel.setBorder(Rectangle.NO_BORDER);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase(formatoMoneda(venta.getTotal()), fontTotal));
        totalValue.setBackgroundColor(COLOR_SECUNDARIO);
        totalValue.setBorder(Rectangle.NO_BORDER);
        totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.addCell(totalValue);

        document.add(totales);

        // Anticipo si aplica
        if ("anticipo".equals(venta.getModalidad()) && venta.getMontoAnticipo() != null) {
            document.add(Chunk.NEWLINE);
            PdfPTable anticipo = new PdfPTable(2);
            anticipo.setWidthPercentage(50);
            anticipo.setHorizontalAlignment(Element.ALIGN_RIGHT);
            anticipo.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            anticipo.addCell(new PdfPCell(new Phrase("Anticipo:", fontNormal)));
            anticipo.addCell(new PdfPCell(new Phrase(formatoMoneda(venta.getMontoAnticipo()), fontNormal)));

            if (venta.getSaldoPendiente() != null && venta.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
                anticipo.addCell(new PdfPCell(new Phrase("Saldo Pendiente:", 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PENDIENTE))));
                anticipo.addCell(new PdfPCell(new Phrase(formatoMoneda(venta.getSaldoPendiente()), 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PENDIENTE))));
            }
            document.add(anticipo);
        }

        document.add(Chunk.NEWLINE);
        document.add(new Phrase("Términos y condiciones", fontSubtitulo));
        document.add(new Phrase("Gracias por su compra. Esta factura corresponde a los servicios prestados y debe conservarse como comprobante.", fontNormal));

        document.close();
        return baos.toByteArray();
    }

    private static String formatearFecha(java.util.Date fecha) {
        if (fecha == null) return "N/A";
        return new SimpleDateFormat("dd/MM/yyyy").format(fecha);
    }

    private static String formatoMoneda(BigDecimal monto) {
        if (monto == null) return "$0.00";
        return "$" + String.format("%,.2f", monto.doubleValue());
    }
}