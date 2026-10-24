
package cl.duoc.ms_pedidos360_notify.service;

import cl.duoc.ms_pedidos360_notify.dto.DetalleFactura;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class FacturaPdfService {

    private static final Color AZUL = new Color(12, 59, 69);
    private static final Color TURQUESA = new Color(32, 102, 107);
    private static final Color GRIS = new Color(90, 105, 112);
    private static final Color FONDO = new Color(235, 245, 244);
    private static final Color BLANCO = Color.WHITE;

    private static final float MARGEN = 45;
    private static final float ANCHO = PDRectangle.A4.getWidth();

    private final NumberFormat moneda =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("es-CL")
            );

    public byte[] generarFactura(
            long pedidoId,
            String nombreCliente,
            String emailCliente,
            String estado,
            String fecha,
            double subtotalNeto,
            double iva,
            double totalConIva,
            List<DetalleFactura> detalles
    ) {
        try (
                PDDocument documento = new PDDocument();
                ByteArrayOutputStream salida = new ByteArrayOutputStream()
        ) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            try (PDPageContentStream contenido =
                         new PDPageContentStream(documento, pagina)) {

                float y = 797;

                // Encabezado.
                contenido.setNonStrokingColor(AZUL);
                contenido.addRect(0, 735, ANCHO, 107);
                contenido.fill();

                texto(
                        contenido, "PEDIDOS360",
                        MARGEN, y, 23, true, BLANCO
                );

                texto(
                        contenido,
                        "DOCUMENTO COMERCIAL DE DEMOSTRACION",
                        MARGEN, y - 22, 9, false, BLANCO
                );

                texto(
                        contenido, "FACTURA",
                        415, y - 2, 19, true, BLANCO
                );

                texto(
                        contenido,
                        String.format("DEMO-%06d", pedidoId),
                        415, y - 22, 10, false, BLANCO
                );

                // Datos del emisor y del cliente.
                y = 700;

                texto(
                        contenido, "EMISOR",
                        MARGEN, y, 9, true, TURQUESA
                );

                texto(
                        contenido, "Pedidos360 SpA",
                        MARGEN, y - 19, 11, true, AZUL
                );

                texto(
                        contenido, "Concepcion, Chile",
                        MARGEN, y - 35, 9, false, GRIS
                );

                texto(
                        contenido, "RUT: NO APLICA (DEMO)",
                        MARGEN, y - 49, 9, false, GRIS
                );

                texto(
                        contenido, "CLIENTE",
                        310, y, 9, true, TURQUESA
                );

                texto(
                        contenido,
                        limitar(nombreCliente, 36),
                        310, y - 19, 11, true, AZUL
                );

                texto(
                        contenido,
                        limitar(emailCliente, 38),
                        310, y - 35, 9, false, GRIS
                );

                texto(
                        contenido,
                        "Pedido: #" + pedidoId,
                        310, y - 49, 9, false, GRIS
                );

                linea(
                        contenido,
                        45, 625, ANCHO - 45, 625,
                        FONDO, 2
                );

                texto(
                        contenido, "RESUMEN DEL PEDIDO",
                        MARGEN, 605, 10, true, TURQUESA
                );

                texto(
                        contenido, "Fecha: " + fecha,
                        MARGEN, 585, 9, false, GRIS
                );

                texto(
                        contenido, "Estado: " + estado,
                        310, 585, 9, false, GRIS
                );

                // Tabla de productos.
                float tablaY = 545;

                contenido.setNonStrokingColor(AZUL);
                contenido.addRect(
                        MARGEN, tablaY - 7,
                        ANCHO - 2 * MARGEN, 25
                );
                contenido.fill();

                texto(
                        contenido, "PRODUCTO",
                        53, tablaY + 1, 8, true, BLANCO
                );

                texto(
                        contenido, "CANT.",
                        325, tablaY + 1, 8, true, BLANCO
                );

                texto(
                        contenido, "PRECIO NETO",
                        375, tablaY + 1, 8, true, BLANCO
                );

                texto(
                        contenido, "SUBTOTAL",
                        480, tablaY + 1, 8, true, BLANCO
                );

                float filaY = tablaY - 28;
                int filas = 0;

                if (detalles != null) {
                    for (DetalleFactura detalle : detalles) {
                        // Mantener el documento de demostración en una página.
                        if (filas >= 14) {
                            texto(
                                    contenido,
                                    "Hay mas productos en el pedido. Consulte el detalle digital.",
                                    MARGEN, filaY, 8, false, GRIS
                            );
                            break;
                        }

                        if (filas % 2 == 0) {
                            contenido.setNonStrokingColor(FONDO);
                            contenido.addRect(
                                    MARGEN, filaY - 7,
                                    ANCHO - 2 * MARGEN, 22
                            );
                            contenido.fill();
                        }

                        texto(
                                contenido,
                                limitar(detalle.producto(), 43),
                                53, filaY, 8, false, AZUL
                        );

                        texto(
                                contenido,
                                String.valueOf(detalle.cantidad()),
                                330, filaY, 8, false, AZUL
                        );

                        texto(
                                contenido,
                                formatoMoneda(detalle.precioUnitario()),
                                375, filaY, 8, false, AZUL
                        );

                        texto(
                                contenido,
                                formatoMoneda(detalle.subtotal()),
                                480, filaY, 8, false, AZUL
                        );

                        filaY -= 22;
                        filas++;
                    }
                }

                if (filas == 0) {
                    texto(
                            contenido,
                            "Sin detalles de productos registrados",
                            53, filaY, 9, false, GRIS
                    );
                }

                // Resumen tributario.
                float totalY = Math.max(filaY - 45, 160);

                linea(
                        contenido,
                        340, totalY + 22,
                        ANCHO - MARGEN, totalY + 22,
                        FONDO, 2
                );

                texto(
                        contenido, "Subtotal neto",
                        345, totalY, 9, false, AZUL
                );

                texto(
                        contenido,
                        formatoMoneda(subtotalNeto),
                        455, totalY, 9, false, AZUL
                );

                texto(
                        contenido, "IVA (19%)",
                        345, totalY - 18, 9, false, AZUL
                );

                texto(
                        contenido,
                        formatoMoneda(iva),
                        455, totalY - 18, 9, false, AZUL
                );

                texto(
                        contenido, "TOTAL CON IVA",
                        345, totalY - 43, 10, true, AZUL
                );

                texto(
                        contenido,
                        formatoMoneda(totalConIva),
                        455, totalY - 45, 15, true, TURQUESA
                );

                // Aviso de documento ficticio.
                contenido.setNonStrokingColor(FONDO);
                contenido.addRect(
                        MARGEN, 72,
                        ANCHO - 2 * MARGEN, 35
                );
                contenido.fill();

                texto(
                        contenido,
                        "DOCUMENTO FICTICIO - SIN VALIDEZ TRIBUTARIA",
                        150, 91, 10, true, AZUL
                );

                texto(
                        contenido,
                        "Documento generado automaticamente por Pedidos360 para demostracion.",
                        100, 48, 8, false, GRIS
                );
            }

            documento.save(salida);
            return salida.toByteArray();

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "No fue posible generar la factura PDF del pedido " + pedidoId,
                    ex
            );
        }
    }

    private void texto(
            PDPageContentStream contenido,
            String valor,
            float x,
            float y,
            float tamano,
            boolean negrita,
            Color color
    ) throws IOException {

        PDType1Font fuente = new PDType1Font(
                negrita
                        ? Standard14Fonts.FontName.HELVETICA_BOLD
                        : Standard14Fonts.FontName.HELVETICA
        );

        contenido.beginText();
        contenido.setFont(fuente, tamano);
        contenido.setNonStrokingColor(color);
        contenido.newLineAtOffset(x, y);
        contenido.showText(limpiarTexto(valor));
        contenido.endText();
    }

    private void linea(
            PDPageContentStream contenido,
            float x1,
            float y1,
            float x2,
            float y2,
            Color color,
            float grosor
    ) throws IOException {
        contenido.setStrokingColor(color);
        contenido.setLineWidth(grosor);
        contenido.moveTo(x1, y1);
        contenido.lineTo(x2, y2);
        contenido.stroke();
    }

    private String formatoMoneda(double valor) {
        return moneda.format(valor).replace('\u00A0', ' ');
    }

    private String limitar(String valor, int maximo) {
        if (valor == null || valor.isBlank()) {
            return "No especificado";
        }

        String limpio = limpiarTexto(valor);

        return limpio.length() <= maximo
                ? limpio
                : limpio.substring(0, maximo - 3) + "...";
    }

    private String limpiarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        // Las fuentes estándar del PDF no incluyen todos los caracteres Unicode.
        String normalizado = Normalizer.normalize(
                valor.replace('\u00A0', ' '),
                Normalizer.Form.NFD
        );

        return normalizado
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x20-\\x7E]", "?");
    }
}
