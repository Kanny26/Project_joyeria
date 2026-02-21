<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String usuarioId = request.getParameter("usuarioId");
%>

<!DOCTYPE html>
<html>
<head>
    <title>Nueva Compra</title>
</head>

<body>

<h2>Nueva Compra</h2>

<form action="<%=request.getContextPath()%>/CompraServlet" method="post">

    <input type="hidden" name="action" value="guardarCompra">
    <input type="hidden" name="usuarioId" value="<%=usuarioId%>">

    <div>
        <label>Fecha:</label>
        <input type="date" name="fechaCompra" required>
    </div>

    <br>

    <table border="1" width="100%">
        <thead>
        <tr>
            <th>ID Producto</th>
            <th>Precio Unitario</th>
            <th>Cantidad</th>
            <th>Subtotal</th>
            <th>Acción</th>
        </tr>
        </thead>

        <tbody id="bodyProductos">
        </tbody>
    </table>

    <br>

    <button type="button" onclick="agregarFila()">
        + Agregar Producto
    </button>

    <br><br>

    <h3>Total: S/ <span id="totalGeneral">0.00</span></h3>

    <input type="hidden" name="total" id="inputTotal">

    <br>

    <button type="submit">
        Guardar Compra
    </button>

    <button type="button"
        onclick="window.location.href='<%=request.getContextPath()%>/ProveedorServlet?action=verCompras&id=<%=usuarioId%>'">
        Cancelar
    </button>

</form>

<script>

function agregarFila() {

    let fila = `
        <tr>
            <td>
                <input type="number" name="productoId" required>
            </td>

            <td>
                <input type="number" step="0.01" name="precioUnitario"
                       oninput="calcularFila(this)" required>
            </td>

            <td>
                <input type="number" name="cantidad"
                       oninput="calcularFila(this)" required>
            </td>

            <td>
                <span class="subtotal">0.00</span>
            </td>

            <td>
                <button type="button" onclick="eliminarFila(this)">
                    Eliminar
                </button>
            </td>
        </tr>
    `;

    document.getElementById("bodyProductos")
        .insertAdjacentHTML("beforeend", fila);
}

function eliminarFila(btn) {
    btn.closest("tr").remove();
    calcularTotal();
}

function calcularFila(input) {

    let fila = input.closest("tr");

    let precio = parseFloat(
        fila.querySelector("input[name='precioUnitario']").value
    ) || 0;

    let cantidad = parseInt(
        fila.querySelector("input[name='cantidad']").value
    ) || 0;

    let subtotal = precio * cantidad;

    fila.querySelector(".subtotal").innerText =
        subtotal.toFixed(2);

    calcularTotal();
}

function calcularTotal() {

    let subtotales =
        document.querySelectorAll(".subtotal");

    let total = 0;

    subtotales.forEach(function(s) {
        total += parseFloat(s.innerText) || 0;
    });

    document.getElementById("totalGeneral").innerText =
        total.toFixed(2);

    document.getElementById("inputTotal").value =
        total.toFixed(2);
}

// Agrega una fila inicial automáticamente
agregarFila();

</script>

</body>
</html>