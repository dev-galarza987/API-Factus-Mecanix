# 🎨 UI de Bienvenida - Factus API

## Interfaz de Usuario con Thymeleaf

La aplicación ahora cuenta con una interfaz de usuario minimalista y elegante en la ruta raíz `/`.

---

## 🌐 Acceso a la UI

### URL Principal
```
http://localhost:8080/
```

**Acceso:** ✅ **PÚBLICO** (No requiere autenticación)

---

## 🎨 Características del Diseño

### Estilo Visual
- **Colores:** Gradiente azul-púrpura (#667eea → #764ba2)
- **Diseño:** Minimalista y moderno
- **Responsive:** Adaptable a dispositivos móviles
- **Animaciones:** Efectos suaves y transiciones elegantes

### Elementos de la UI

1. **Header**
   - Logo "Factus" con gradiente
   - Subtítulo descriptivo
   - Indicador de estado activo (con animación)

2. **Cards Informativos**
   - Empresa: Galarza TechCorp
   - Versión: 1.0
   - Fecha y hora actual

3. **Información Fiscal**
   - IVA: 13%
   - IT: 3%

4. **Enlaces Rápidos**
   - 📚 Documentación API (Swagger)
   - 🔐 Autenticación
   - 👥 Clientes
   - 📄 Facturas
   - 📖 OpenAPI Docs
   - ❤️ Health Check

5. **Footer**
   - Copyright
   - Información del sistema

---

## 🔧 Tecnologías Utilizadas

- **Thymeleaf** - Motor de templates para Java
- **CSS3** - Estilos modernos con gradientes y animaciones
- **Spring Boot** - Framework backend
- **Spring Security** - Configuración de acceso público

---

## 🔐 Configuración de Seguridad

### Rutas Públicas (Sin Autenticación)

La siguiente configuración permite acceso público a la UI:

```java
// SecurityConfig.java
.requestMatchers("/", "/index", "/home").permitAll()
.requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
.requestMatchers("/favicon.ico", "/webjars/**").permitAll()
.requestMatchers("/error").permitAll()
```

### Filtro JWT Excluido

El filtro JWT **NO se aplica** a las siguientes rutas:

- `/` - Página principal
- `/index` - Alias de la página principal
- `/home` - Alias de la página principal
- `/api/auth/**` - Endpoints de autenticación
- `/swagger-ui/**` - Documentación Swagger
- `/actuator/health` - Health check
- Recursos estáticos (CSS, JS, imágenes)

---

## 🚀 Cómo Ejecutar

### 1. Iniciar la Aplicación

```bash
mvnw.cmd spring-boot:run
```

### 2. Abrir en el Navegador

```
http://localhost:8080/
```

### 3. Navegar por la UI

Desde la página principal puedes acceder a:
- Documentación completa de la API
- Endpoints de autenticación
- Gestión de clientes
- Gestión de facturas
- Estado del sistema

---

## 📱 Responsive Design

La UI es completamente responsive y se adapta a diferentes tamaños de pantalla:

- **Desktop:** Vista completa con grid de 3 columnas
- **Tablet:** Vista adaptada con grid de 2 columnas
- **Mobile:** Vista de columna única

---

## 🎯 Rutas Disponibles

| Ruta | Acceso | Descripción |
|------|--------|-------------|
| `/` | Público | Página principal con UI |
| `/swagger-ui.html` | Público | Documentación interactiva |
| `/api-docs` | Público | Especificación OpenAPI |
| `/actuator/health` | Público | Estado de la aplicación |
| `/api/auth/login` | Público | Endpoint de login |
| `/api/auth/register` | Público | Endpoint de registro |
| `/api/clients/**` | Privado | Endpoints de clientes |
| `/api/invoices/**` | Privado | Endpoints de facturas |

---

## 🔄 Actualizaciones Futuras

Posibles mejoras para la UI:

- [ ] Dashboard con estadísticas
- [ ] Gráficos de reportes
- [ ] Formularios de login integrados
- [ ] Panel de administración
- [ ] Modo oscuro
- [ ] Internacionalización (i18n)

---

## 📝 Personalización

### Cambiar Colores

Edita el archivo `index.html` en la sección `<style>`:

```css
/* Cambiar gradiente principal */
background: linear-gradient(135deg, #TU_COLOR1 0%, #TU_COLOR2 100%);
```

### Modificar Contenido

Edita el controlador `HomeController.java`:

```java
model.addAttribute("appName", "Tu Nombre");
model.addAttribute("empresa", "Tu Empresa");
```

---

## 🐛 Troubleshooting

### La UI no carga

1. Verificar que el servidor esté corriendo en el puerto 8080
2. Verificar que no haya errores en los logs
3. Limpiar cache del navegador (Ctrl + F5)

### Error 403 Forbidden

Si recibes este error al acceder a `/`:
1. Verificar configuración en `SecurityConfig.java`
2. Verificar que el filtro JWT excluya la ruta raíz
3. Reiniciar la aplicación

### Recursos estáticos no cargan

Si CSS o JS no cargan:
1. Verificar que los archivos estén en `src/main/resources/static/`
2. Verificar permisos de acceso público en SecurityConfig
3. Verificar consola del navegador para errores 404

---

## 💡 Mejores Prácticas

1. **Seguridad:** Mantener rutas públicas al mínimo necesario
2. **Performance:** Minimizar CSS y JS en producción
3. **SEO:** Agregar meta tags apropiados
4. **Accesibilidad:** Usar etiquetas semánticas HTML5
5. **Responsive:** Probar en múltiples dispositivos

---

## 📞 Soporte

Para problemas con la UI:
- **Email:** ui-support@galarzatechcorp.com
- **Documentación:** Ver comentarios en `index.html`
- **Issues:** Reportar en el repositorio del proyecto

---

**Desarrollado con ❤️ por Galarza TechCorp**

