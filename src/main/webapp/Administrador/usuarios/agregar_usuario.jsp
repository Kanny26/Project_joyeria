<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Agregar usuario</title>

    <!-- Estilos -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/main.css" />
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/pages/Administrador/usuarios/agregar_usuario.css" />

    <!-- ICONOS -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css" />
</head>

<body>
    <!-- NAV -->
    <nav class="navbar-admin">
        <div class="navbar-admin__catalogo">
            <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
        </div>

        <h1 class="navbar-admin__title">AAC27</h1>
        <a href="<%= request.getContextPath() %>/Administrador/usuarios.jsp">
            <i class="fa-solid fa-house-chimney navbar-admin__home-icon"></i>
        </a>
    </nav>

    <!-- CONTENIDO PRINCIPAL -->
    <main class="titulo">
        <h1 class="titulo__encabezado">Añadir usuario</h1>

        <section class="registro-Usuario__panel">
            <div class="registro-Usuario__formulario">
                <div class="registro-Usuario__formulario-contenido">

                    <header>
                        <h1>Datos del Usuario</h1>
                    </header>

                    <form class="registro-Usuario__form-grid" method="post" action="<%= request.getContextPath() %>/UsuarioServlet">
                        <input type="hidden" name="accion" value="agregar" />

                        <!-- Nombre -->
                        <div class="registro-Usuario__grupo-campo">
                            <label>Nombre</label>
                            <div class="registro-Usuario__input-grupo">
                                <i class="fas fa-user icon-left"></i>
                                <input type="text" name="nombre" placeholder="Nombre de Usuario" required />
                            </div>
                        </div>

                        <!-- Correo -->
                        <div class="registro-Usuario__grupo-campo">
                            <label>Correo</label>
                            <div class="registro-Usuario__input-grupo">
                                <i class="fas fa-envelope icon-left"></i>
                                <input type="email" name="correo" placeholder="Correo electrónico" required />
                            </div>
                        </div>

                        <!-- Teléfono -->
                        <div class="registro-Usuario__grupo-campo">
                            <label>Teléfono</label>
                            <div class="registro-Usuario__input-grupo">
                                <i class="fas fa-phone icon-left"></i>
                                <input type="tel" name="telefono" placeholder="Teléfono del Usuario" required />
                            </div>
                        </div>

                        <!-- Estado -->
                        <div class="registro-Usuario__grupo-campo">
                            <label>Estado</label>
                            <div class="registro-Usuario__input-grupo">
                                <label class="radio-op">
                                    <input type="radio" name="estado" value="Activo" required> Activo
                                </label>
                                <label class="radio-op">
                                    <input type="radio" name="estado" value="Inactivo" required> Inactivo
                                </label>
                            </div>
                        </div>

                        <!-- Fecha de inicio -->
                        <div class="registro-Usuario__grupo-campo">
                            <label>Fecha de inicio</label>
                            <div class="registro-Usuario__input-grupo">
                                <i class="fas fa-calendar icon-left"></i>
                                <input type="date" name="fechaInicio" required />
                            </div>
                        </div>
                        
                        <!-- rol de usuario -->
                        <div class="registro-Usuario__grupo-campo">
							<label>Rol:
						        <select name="rol">
						            <option value="vendedor">Vendedor</option>
						            <option value="administrador">Administrador</option>
						            <option value="proveedor">Proveedor</option>
						            <option value="cliente">Cliente</option>
						        </select>
						    </label>
                        </div>

                        <!-- Botón -->
                        <div class="registro-usuario__boton-contenedor">
                            <button type="submit" class="btn">Añadir</button>
                        </div>
                    </form>

                </div>
            </div>
        </section>
    </main>
</body>
</html>
