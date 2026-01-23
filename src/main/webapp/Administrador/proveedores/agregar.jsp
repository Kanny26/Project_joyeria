<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Object admin = session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Agregar proveedor</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/proveedores/agregar.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>
<body>
<nav class="navbar-admin">
    <div class="navbar-admin__catalogo"> 
        <img src="<%=request.getContextPath()%>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div> 
    <h1 class="navbar-admin__title">AAC27</h1>
    <a href="<%=request.getContextPath()%>/Administrador/proveedores.jsp">
        <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
    </a>
</nav>

<main class="titulo">
    <h1 class="titulo__encabezado">Añadir un proveedor</h1>

    <section class="registro-proveedor__panel">
        <div class="registro-proveedor__formulario">
            <div class="registro-proveedor__formulario-contenido">
                <header><h1>Datos del proveedor</h1></header>

                <form action="<%=request.getContextPath()%>/Administrador/proveedores" method="post">
                    <input type="hidden" name="action" value="guardar">

                    <div class="registro-proveedor__grupo-campo">
                        <label>Nombre</label>
                        <input type="text" name="nombre" required />
                    </div>

                    <div class="registro-proveedor__grupo-campo">
                        <label>Correo</label>
                        <input type="email" name="correo" required />
                    </div>

                    <div class="registro-proveedor__grupo-campo">
                        <label>Teléfono</label>
                        <input type="tel" name="telefono" required />
                    </div>

                    <div class="registro-proveedor__grupo-campo">
                        <label>Estado</label>
                        <label><input type="radio" name="estado" value="activo" checked> Activo</label>
                        <label><input type="radio" name="estado" value="inactivo"> Inactivo</label>
                    </div>

                    <!-- Materiales (debes cargarlos desde BD, pero aquí ejemplo estático) -->
                    <div class="registro-proveedor__grupo-campo">
                        <label>Materiales</label>
                        <div>
                            <label><input type="checkbox" name="materiales" value="Acero inoxidable"> Acero inoxidable</label><br>
                            <label><input type="checkbox" name="materiales" value="Plata"> Plata</label><br>
                            <label><input type="checkbox" name="materiales" value="Oro laminado"> Oro laminado</label>
                        </div>
                    </div>

                    <div class="registro-proveedor__grupo-campo">
                        <label>Fecha de inicio</label>
                        <input type="date" name="fechaInicio" required />
                    </div>

                    <div class="registro-proveedor__boton-contenedor">
                        <button type="submit" class="btn">Añadir</button>
                    </div>
                </form>
            </div>
        </div>
    </section>
</main>
</body>
</html>
