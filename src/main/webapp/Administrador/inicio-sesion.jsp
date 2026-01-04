<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Iniciar sesión</title>

    <!-- CSS -->
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/main.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/pages/Administrador/inicio-sesion.css" />

    <!-- Font Awesome -->
    <link
        rel="stylesheet"
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css"
    />
</head>
<body>
<div class="inicio-sesion">
    <aside class="inicio-sesion__panel">
        <div class="inicio-sesion__panel-caja"> 
            <img 
                class="inicio-sesion__panel-logo" 
                src="<%=request.getContextPath()%>/assets/Imagenes/Logo.png" 
                alt="Logo" 
            /> 
        </div> 
    </aside> 

    <main class="inicio-sesion__contenido"> 
        <section class="inicio-sesion__formulario"> 
            <div class="inicio-sesion__formulario-contenido"> 
                <header> 
                    <h1>Iniciar Sesión</h1> 
                </header> 

                <!-- Formulario apunta al servlet /login -->
                <form action="<%=request.getContextPath()%>/login" method="post"> 
                    <div class="inicio-sesion__input-grupo"> 
                        <i class="fas fa-user icon-left"></i> 
                        <input 
                            type="text" 
                            name="usuario"
                            id="usuario"
                            placeholder="Nombre de usuario" 
                            required 
                        /> 
                    </div> 

                    <div class="inicio-sesion__input-grupo"> 
                        <i class="fas fa-lock icon-left"></i> 
                        <input 
                            type="password"
                            name="password" 
                            id="password" 
                            placeholder="Contraseña" 
                            required 
                        /> 
                        <i class="fas fa-eye icon-right"></i>
                    </div> 

                    <a 
                        href="<%=request.getContextPath()%>/Recuperar_pass/ing-codigo.jsp" 
                        class="inicio-sesion__link-recuperar" 
                    > 
                        ¿Olvidaste tu contraseña? 
                    </a> 

                    <button type="submit" class="btn"> 
                        Iniciar Sesión
                    </button> 
                </form> 

                <footer class="inicio-sesion__link-registro"> 
                    <p>
                        Diseñado por Stephany Moreno
                    </p>
                </footer>
            </div>
        </section>
    </main>
</div>
</body>
</html>


