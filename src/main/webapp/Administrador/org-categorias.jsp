<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, model.Categoria, model.Administrador, model.Material, model.Producto" %>

<%
    Administrador admin = (Administrador) session.getAttribute("admin");
    if (admin == null) {
        response.sendRedirect(request.getContextPath() + "/Administrador/inicio-sesion.jsp");
        return;
    }

    List<Categoria> categorias = (List<Categoria>) request.getAttribute("categorias");
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Gestionar Categorías</title>

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/org-categorias.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
</head>

<body>

<nav class="navbar-admin">
    <div class="navbar-admin__catalogo">
        <img src="<%= request.getContextPath() %>/assets/Imagenes/iconos/admin.png" alt="Admin">
    </div>
    <h1 class="navbar-admin__title">AAC27</h1>
    
    <a href="<%=request.getContextPath()%>/Administrador/admin-principal.jsp"
   class="navbar-admin__home-link">
	   
	    <span class="navbar-admin__home-icon-wrap">
	    
	        <i class="fa-solid fa-arrow-left"></i>
	        
		    <span class="navbar-admin__home-text">Volver atras</span>
		    
		    <i class="fa-solid fa-house-chimney"></i>
	    </span>
    </a>
</nav>

<main class="titulo">
    <h2 class="titulo__encabezado">Gestionar Categorías</h2>

    <section class="iconos-contenedor">
		
		<% if (categorias != null && !categorias.isEmpty()) {
		   for (Categoria c : categorias) { %>
		
		    <article class="iconos-item">
		        <a href="<%=request.getContextPath()%>/CategoriaServlet?id=<%= c.getCategoriaId() %>"
		           class="icono-boton">
		
		            <div class="icono-boton__circulo">
		                <img class="icono-boton__img"
		                     src="<%=request.getContextPath()%>/assets/Imagenes/iconos/<%= c.getIcono() %>"
		                     alt="<%= c.getNombre() %>">
		            </div>
		
		            <h3 class="icono-boton__titulo"><%= c.getNombre() %></h3>
		
		        </a>
		        
		    </article>
	
		<% } } else { %>
		    <p>No hay categorías registradas</p>
		<% } %>
				<div style="text-align: center; margin-bottom: 20px;">
				    <button type="button" onclick="document.getElementById('modalCategoria').style.display='block'" class="icono-boton">
				        <div class="icono-boton__circulo" style="background-color: #28a745;">
				            <i class="fa-solid fa-plus" style="color: white; font-size: 2rem;"></i>
				        </div>
				        <h3 class="icono-boton__titulo">Nueva Categoría</h3>
				    </button>
				</div>
				
				<div id="modalCategoria" style="display:none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.4); backdrop-filter: blur(3px); align-items: center; justify-content: center;">
    
		    <div style="background-color: #f8f7ff; padding: 30px; width: 450px; border-radius: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); border: 1px solid #e0d7ff; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">
		        
		        <h3 style="color: #6c5ce7; margin-top: 0; border-bottom: 2px solid #e0d7ff; padding-bottom: 10px; text-align: center;">Crear Nueva Categoría</h3>
		        
		        <form action="<%=request.getContextPath()%>/CategoriaServlet" method="post" enctype="multipart/form-data" style="margin-top: 20px;">
		            <input type="hidden" name="action" value="guardar">
		            
		            <div style="margin-bottom: 15px;">
		                <label style="color: #5e5e5e; font-weight: 600;">Nombre de la Categoría</label>
		                <input type="text" name="nombre" required placeholder="Ej. Anillos, Pulseras..." 
		                       style="width: 100%; padding: 12px; margin-top: 8px; border: 1px solid #d1d1d1; border-radius: 8px; outline: none; box-sizing: border-box; focus: border-color: #a29bfe;">
		            </div>
		            
		            <div style="margin-bottom: 25px;">
		                <label style="color: #5e5e5e; font-weight: 600;">Icono de Categoría</label>
		                <input type="file" name="archivoIcono" accept="image/*" required 
		                       style="width: 100%; margin-top: 8px; color: #7f8c8d;">
		            </div>
		            
		            <div style="display: flex; gap: 10px; justify-content: flex-end;">
		                <button type="button" onclick="document.getElementById('modalCategoria').style.display='none'" 
		                        style="background: #efecff; color: #6c5ce7; border: none; padding: 12px 20px; border-radius: 8px; cursor: pointer; font-weight: bold;">
		                    Cancelar
		                </button>
		                
		                <button type="submit" 
		                        style="background: #6c5ce7; color: white; border: none; padding: 12px 25px; border-radius: 8px; cursor: pointer; font-weight: bold; box-shadow: 0 4px 10px rgba(108, 92, 231, 0.2);">
		                    Guardar Categoría
		                </button>
		            </div>
		        </form>
		    </div>
</div>
		</section>

</main>

</body>
</html>

