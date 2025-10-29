# 📘 Documentación de Endpoints - Factus API

## Sistema de Facturación Electrónica

**Base URL:** `http://localhost:8080`

**Versión:** 1.0

**Impuestos Configurados:**
- IVA: 13%
- IT: 3%

---

## 🔐 Autenticación

Todos los endpoints (excepto `/api/auth/*`) requieren autenticación JWT mediante el header:

```
Authorization: Bearer {token}
```

---

## 📑 Tabla de Contenidos

1. [Autenticación](#1-autenticación)
2. [Clientes](#2-clientes)
3. [Facturas](#3-facturas)
4. [Detalles](#4-detalles)
5. [Home](#5-home)

---

## 1. Autenticación

### 1.1 Login

**Endpoint:** `POST /api/auth/login`

**Descripción:** Autenticar usuario y obtener token JWT

**Autenticación:** No requiere

**Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Esquema Request:**
| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| username | string | Sí | Nombre de usuario |
| password | string | Sí | Contraseña |

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "admin",
  "email": "admin@factus.com"
}
```

**Esquema Response:**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| token | string | Token JWT para autenticación |
| type | string | Tipo de token (siempre "Bearer") |
| username | string | Nombre de usuario |
| email | string | Email del usuario |

**Errores:**
- `400 Bad Request` - Credenciales inválidas
- `401 Unauthorized` - Usuario no encontrado

---

### 1.2 Registro

**Endpoint:** `POST /api/auth/register`

**Descripción:** Registrar nuevo usuario en el sistema

**Autenticación:** No requiere

**Request Body:**
```json
{
  "username": "usuario123",
  "password": "miPassword123",
  "email": "usuario@example.com",
  "nombre": "Juan",
  "apellido": "Pérez",
  "roles": ["ROLE_USER"]
}
```

**Esquema Request:**
| Campo | Tipo | Requerido | Validación | Descripción |
|-------|------|-----------|------------|-------------|
| username | string | Sí | Min 3, Max 50 | Nombre de usuario único |
| password | string | Sí | Min 6 | Contraseña |
| email | string | Sí | Formato email | Email único |
| nombre | string | No | Max 100 | Nombre del usuario |
| apellido | string | No | Max 100 | Apellido del usuario |
| roles | array[string] | No | - | Roles del usuario (por defecto: ROLE_USER) |

**Roles Disponibles:**
- `ROLE_ADMIN` - Acceso total
- `ROLE_USER` - Crear y editar
- `ROLE_VIEWER` - Solo lectura

**Response 201 Created:**
```json
"Usuario registrado exitosamente"
```

**Errores:**
- `400 Bad Request` - Username o email ya en uso

---

## 2. Clientes

### 2.1 Crear Cliente

**Endpoint:** `POST /api/clients`

**Descripción:** Crear un nuevo cliente

**Autenticación:** Requerida (ROLE_USER, ROLE_ADMIN)

**Request Body:**
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "nit": 1234567890,
  "email": "juan.perez@example.com",
  "telefono": "77123456",
  "direccion": "Av. Principal #123",
  "ciudad": "La Paz",
  "departamento": "La Paz"
}
```

**Esquema Request:**
| Campo | Tipo | Requerido | Validación | Descripción |
|-------|------|-----------|------------|-------------|
| nombre | string | Sí | Max 100 | Nombre del cliente |
| apellido | string | Sí | Max 100 | Apellido del cliente |
| nit | long | Sí | 10 dígitos (1000000000-9999999999) | NIT único de 10 dígitos |
| email | string | No | Formato email | Email único |
| telefono | string | No | Max 20 | Teléfono |
| direccion | string | No | Max 200 | Dirección |
| ciudad | string | No | Max 100 | Ciudad |
| departamento | string | No | Max 100 | Departamento |

**Response 201 Created:**
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "nit": 1234567890,
  "email": "juan.perez@example.com",
  "telefono": "77123456",
  "direccion": "Av. Principal #123",
  "ciudad": "La Paz",
  "departamento": "La Paz",
  "activo": true,
  "createdAt": "2025-10-28T10:30:00",
  "updatedAt": "2025-10-28T10:30:00"
}
```

**Errores:**
- `400 Bad Request` - NIT o email duplicado
- `401 Unauthorized` - Token inválido
- `403 Forbidden` - Sin permisos

---

### 2.2 Obtener Cliente por ID

**Endpoint:** `GET /api/clients/{id}`

**Descripción:** Obtener información de un cliente específico

**Autenticación:** Requerida (Todos los roles)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID del cliente |

**Ejemplo:** `GET /api/clients/1`

**Response 200 OK:**
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "nit": 1234567890,
  "email": "juan.perez@example.com",
  "telefono": "77123456",
  "direccion": "Av. Principal #123",
  "ciudad": "La Paz",
  "departamento": "La Paz",
  "activo": true,
  "createdAt": "2025-10-28T10:30:00",
  "updatedAt": "2025-10-28T10:30:00"
}
```

**Errores:**
- `404 Not Found` - Cliente no encontrado

---

### 2.3 Obtener Cliente por NIT

**Endpoint:** `GET /api/clients/nit/{nit}`

**Descripción:** Buscar cliente por su NIT

**Autenticación:** Requerida (Todos los roles)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| nit | long | NIT del cliente (10 dígitos) |

**Ejemplo:** `GET /api/clients/nit/1234567890`

**Response 200 OK:** (Mismo esquema que 2.2)

---

### 2.4 Listar Todos los Clientes

**Endpoint:** `GET /api/clients`

**Descripción:** Obtener lista de todos los clientes (activos e inactivos)

**Autenticación:** Requerida (Todos los roles)

**Response 200 OK:**
```json
[
  {
    "id": 1,
    "nombre": "Juan",
    "apellido": "Pérez",
    "nit": 1234567890,
    "email": "juan.perez@example.com",
    "telefono": "77123456",
    "direccion": "Av. Principal #123",
    "ciudad": "La Paz",
    "departamento": "La Paz",
    "activo": true,
    "createdAt": "2025-10-28T10:30:00",
    "updatedAt": "2025-10-28T10:30:00"
  }
]
```

---

### 2.5 Listar Clientes Activos

**Endpoint:** `GET /api/clients/active`

**Descripción:** Obtener lista de clientes activos únicamente

**Autenticación:** Requerida (Todos los roles)

**Response 200 OK:** (Array con mismo esquema que 2.2)

---

### 2.6 Buscar Clientes

**Endpoint:** `GET /api/clients/search?query={busqueda}`

**Descripción:** Buscar clientes por nombre, apellido o NIT

**Autenticación:** Requerida (Todos los roles)

**Query Parameters:**
| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| query | string | Sí | Texto a buscar |

**Ejemplo:** `GET /api/clients/search?query=Juan`

**Response 200 OK:** (Array con mismo esquema que 2.2)

---

### 2.7 Actualizar Cliente

**Endpoint:** `PUT /api/clients/{id}`

**Descripción:** Actualizar información de un cliente

**Autenticación:** Requerida (ROLE_USER, ROLE_ADMIN)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID del cliente |

**Request Body:** (Mismo esquema que 2.1)

**Response 200 OK:** (Mismo esquema que 2.2)

**Errores:**
- `404 Not Found` - Cliente no encontrado
- `400 Bad Request` - NIT o email duplicado

---

### 2.8 Desactivar Cliente (Soft Delete)

**Endpoint:** `DELETE /api/clients/{id}`

**Descripción:** Desactivar cliente (no se elimina de la BD)

**Autenticación:** Requerida (ROLE_ADMIN)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID del cliente |

**Response 204 No Content**

---

### 2.9 Eliminar Cliente Permanentemente

**Endpoint:** `DELETE /api/clients/{id}/hard`

**Descripción:** Eliminar cliente de la base de datos

**Autenticación:** Requerida (ROLE_ADMIN)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID del cliente |

**Response 204 No Content**

---

## 3. Facturas

### 3.1 Crear Factura

**Endpoint:** `POST /api/invoices`

**Descripción:** Crear una nueva factura con sus detalles

**Autenticación:** Requerida (ROLE_USER, ROLE_ADMIN)

**Request Body:**
```json
{
  "serie": "FAC",
  "fechaEmision": "2025-10-28",
  "clientId": 1,
  "tipoComprobante": "FACTURA",
  "detalles": [
    {
      "descripcion": "Producto A",
      "cantidad": 2,
      "precioUnitario": 100.00,
      "descuento": 0.00,
      "unidadMedida": "UND",
      "codigoProducto": "PROD-001"
    },
    {
      "descripcion": "Servicio B",
      "cantidad": 1,
      "precioUnitario": 150.00,
      "descuento": 10.00,
      "unidadMedida": "SERV",
      "codigoProducto": "SERV-001"
    }
  ],
  "observaciones": "Factura por servicios prestados"
}
```

**Esquema Request:**
| Campo | Tipo | Requerido | Validación | Descripción |
|-------|------|-----------|------------|-------------|
| serie | string | Sí | Max 20 | Serie de la factura |
| fechaEmision | date | Sí | Formato ISO | Fecha de emisión |
| clientId | long | Sí | - | ID del cliente |
| tipoComprobante | enum | Sí | - | Tipo de comprobante |
| detalles | array | Sí | Min 1 | Lista de detalles |
| observaciones | string | No | Max 500 | Observaciones |

**Tipos de Comprobante:**
- `FACTURA`
- `BOLETA`
- `NOTA_CREDITO`
- `NOTA_DEBITO`
- `RECIBO`

**Esquema Detalle:**
| Campo | Tipo | Requerido | Validación | Descripción |
|-------|------|-----------|------------|-------------|
| descripcion | string | Sí | Max 255 | Descripción del item |
| cantidad | integer | Sí | Min 1 | Cantidad |
| precioUnitario | decimal | Sí | Min 0.01 | Precio unitario |
| descuento | decimal | No | - | Descuento aplicado |
| unidadMedida | string | No | Max 50 | Unidad de medida |
| codigoProducto | string | No | Max 50 | Código del producto |

**Response 201 Created:**
```json
{
  "id": 1,
  "numeroFactura": "FAC-00000001",
  "serie": "FAC",
  "fechaEmision": "2025-10-28",
  "client": {
    "id": 1,
    "nombre": "Juan",
    "apellido": "Pérez",
    "nit": 1234567890,
    "email": "juan.perez@example.com",
    "telefono": "77123456",
    "direccion": "Av. Principal #123",
    "ciudad": "La Paz",
    "departamento": "La Paz",
    "activo": true,
    "createdAt": "2025-10-28T10:30:00",
    "updatedAt": "2025-10-28T10:30:00"
  },
  "detalles": [
    {
      "id": 1,
      "descripcion": "Producto A",
      "cantidad": 2,
      "precioUnitario": 100.00,
      "descuento": 0.00,
      "subtotal": 200.00,
      "unidadMedida": "UND",
      "codigoProducto": "PROD-001"
    },
    {
      "id": 2,
      "descripcion": "Servicio B",
      "cantidad": 1,
      "precioUnitario": 150.00,
      "descuento": 10.00,
      "subtotal": 140.00,
      "unidadMedida": "SERV",
      "codigoProducto": "SERV-001"
    }
  ],
  "subtotal": 340.00,
  "iva": 44.20,
  "it": 10.20,
  "total": 394.40,
  "estado": "BORRADOR",
  "tipoComprobante": "FACTURA",
  "observaciones": "Factura por servicios prestados",
  "createdAt": "2025-10-28T11:00:00",
  "updatedAt": "2025-10-28T11:00:00"
}
```

**Cálculo de Impuestos:**
- **Subtotal:** Suma de todos los subtotales de detalles
- **IVA (13%):** subtotal × 0.13
- **IT (3%):** subtotal × 0.03
- **Total:** subtotal + IVA + IT

**Errores:**
- `400 Bad Request` - Datos inválidos
- `404 Not Found` - Cliente no encontrado

---

### 3.2 Obtener Factura por ID

**Endpoint:** `GET /api/invoices/{id}`

**Descripción:** Obtener información completa de una factura

**Autenticación:** Requerida (Todos los roles)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la factura |

**Ejemplo:** `GET /api/invoices/1`

**Response 200 OK:** (Mismo esquema que 3.1)

---

### 3.3 Obtener Factura por Número

**Endpoint:** `GET /api/invoices/number/{numeroFactura}`

**Descripción:** Buscar factura por su número

**Autenticación:** Requerida (Todos los roles)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| numeroFactura | string | Número de factura (ej: FAC-00000001) |

**Ejemplo:** `GET /api/invoices/number/FAC-00000001`

**Response 200 OK:** (Mismo esquema que 3.1)

---

### 3.4 Listar Todas las Facturas

**Endpoint:** `GET /api/invoices`

**Descripción:** Obtener lista de todas las facturas

**Autenticación:** Requerida (Todos los roles)

**Response 200 OK:** (Array con mismo esquema que 3.1)

---

### 3.5 Listar Facturas por Cliente

**Endpoint:** `GET /api/invoices/client/{clientId}`

**Descripción:** Obtener todas las facturas de un cliente específico

**Autenticación:** Requerida (Todos los roles)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| clientId | long | ID del cliente |

**Ejemplo:** `GET /api/invoices/client/1`

**Response 200 OK:** (Array con mismo esquema que 3.1)

---

### 3.6 Listar Facturas por Estado

**Endpoint:** `GET /api/invoices/status/{status}`

**Descripción:** Filtrar facturas por estado

**Autenticación:** Requerida (Todos los roles)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| status | enum | Estado de la factura |

**Estados Disponibles:**
- `BORRADOR` - Factura en borrador
- `EMITIDA` - Factura emitida
- `PAGADA` - Factura pagada
- `ANULADA` - Factura anulada
- `VENCIDA` - Factura vencida

**Ejemplo:** `GET /api/invoices/status/EMITIDA`

**Response 200 OK:** (Array con mismo esquema que 3.1)

---

### 3.7 Listar Facturas por Rango de Fechas

**Endpoint:** `GET /api/invoices/date-range?startDate={fecha1}&endDate={fecha2}`

**Descripción:** Obtener facturas dentro de un rango de fechas

**Autenticación:** Requerida (Todos los roles)

**Query Parameters:**
| Parámetro | Tipo | Requerido | Formato | Descripción |
|-----------|------|-----------|---------|-------------|
| startDate | date | Sí | YYYY-MM-DD | Fecha inicial |
| endDate | date | Sí | YYYY-MM-DD | Fecha final |

**Ejemplo:** `GET /api/invoices/date-range?startDate=2025-10-01&endDate=2025-10-31`

**Response 200 OK:** (Array con mismo esquema que 3.1)

---

### 3.8 Actualizar Estado de Factura

**Endpoint:** `PATCH /api/invoices/{id}/status?status={nuevoEstado}`

**Descripción:** Cambiar el estado de una factura

**Autenticación:** Requerida (ROLE_USER, ROLE_ADMIN)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la factura |

**Query Parameters:**
| Parámetro | Tipo | Requerido | Descripción |
|-----------|------|-----------|-------------|
| status | enum | Sí | Nuevo estado |

**Ejemplo:** `PATCH /api/invoices/1/status?status=EMITIDA`

**Reglas de Transición:**
- `BORRADOR` → `EMITIDA` o `ANULADA`
- `EMITIDA` → `PAGADA`, `VENCIDA` o `ANULADA`
- `PAGADA` → Solo `ANULADA`
- `ANULADA` → No puede cambiar

**Response 200 OK:** (Mismo esquema que 3.1)

**Errores:**
- `400 Bad Request` - Transición de estado inválida

---

### 3.9 Emitir Factura

**Endpoint:** `PATCH /api/invoices/{id}/emit`

**Descripción:** Emitir una factura (cambiar de BORRADOR a EMITIDA)

**Autenticación:** Requerida (ROLE_USER, ROLE_ADMIN)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la factura |

**Ejemplo:** `PATCH /api/invoices/1/emit`

**Response 200 OK:** (Mismo esquema que 3.1)

**Errores:**
- `400 Bad Request` - Solo se pueden emitir facturas en BORRADOR

---

### 3.10 Anular Factura

**Endpoint:** `PATCH /api/invoices/{id}/cancel`

**Descripción:** Anular una factura

**Autenticación:** Requerida (ROLE_USER, ROLE_ADMIN)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la factura |

**Ejemplo:** `PATCH /api/invoices/1/cancel`

**Response 200 OK:** (Mismo esquema que 3.1)

**Errores:**
- `400 Bad Request` - No se pueden anular facturas pagadas

---

### 3.11 Eliminar Factura

**Endpoint:** `DELETE /api/invoices/{id}`

**Descripción:** Eliminar una factura (solo borradores)

**Autenticación:** Requerida (ROLE_ADMIN)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la factura |

**Response 204 No Content**

**Errores:**
- `400 Bad Request` - Solo se pueden eliminar facturas en BORRADOR

---

### 3.12 Descargar Factura en PDF

**Endpoint:** `GET /api/invoices/{id}/pdf`

**Descripción:** Generar y descargar factura en formato PDF

**Autenticación:** Requerida (Todos los roles)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| id | long | ID de la factura |

**Ejemplo:** `GET /api/invoices/1/pdf`

**Response 200 OK:**
- **Content-Type:** `application/pdf`
- **Content-Disposition:** `attachment; filename="factura-1.pdf"`
- **Body:** Archivo PDF binario

**Contenido del PDF:**
- Información de la empresa
- Número y tipo de factura
- Fecha de emisión
- Datos del cliente (nombre, NIT, dirección)
- Tabla de detalles con productos/servicios
- Subtotales, IVA (13%), IT (3%) y total
- Observaciones
- Pie de página

---

## 4. Detalles

### 4.1 Información

**Endpoint:** `GET /api/details`

**Descripción:** Información sobre el manejo de detalles

**Autenticación:** Requerida

**Response 200 OK:**
```json
"Los detalles se gestionan a través del endpoint de facturas"
```

**Nota:** Los detalles de factura se crean, modifican y eliminan únicamente a través del endpoint de facturas. No existe gestión independiente de detalles.

---

## 5. Home

### 5.1 Página de Bienvenida

**Endpoint:** `GET /`

**Descripción:** Información general de la API

**Autenticación:** No requiere

**Response 200 OK:**
```json
{
  "aplicacion": "Factus - Sistema de Facturación Electrónica",
  "version": "1.0",
  "empresa": "Galarza TechCorp",
  "documentacion": "/swagger-ui.html",
  "impuestos": {
    "IVA": "13%",
    "IT": "3%"
  },
  "endpoints": {
    "auth": "/api/auth",
    "clients": "/api/clients",
    "invoices": "/api/invoices",
    "swagger": "/swagger-ui.html"
  }
}
```

---

## 📊 Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| 200 | OK - Solicitud exitosa |
| 201 | Created - Recurso creado exitosamente |
| 204 | No Content - Operación exitosa sin contenido |
| 400 | Bad Request - Datos inválidos o error de validación |
| 401 | Unauthorized - Token inválido o no proporcionado |
| 403 | Forbidden - Sin permisos para realizar la acción |
| 404 | Not Found - Recurso no encontrado |
| 500 | Internal Server Error - Error interno del servidor |

---

## 🔒 Matriz de Permisos

| Endpoint | ADMIN | USER | VIEWER |
|----------|-------|------|--------|
| POST /api/clients | ✅ | ✅ | ❌ |
| GET /api/clients/** | ✅ | ✅ | ✅ |
| PUT /api/clients/{id} | ✅ | ✅ | ❌ |
| DELETE /api/clients/{id} | ✅ | ❌ | ❌ |
| POST /api/invoices | ✅ | ✅ | ❌ |
| GET /api/invoices/** | ✅ | ✅ | ✅ |
| PATCH /api/invoices/** | ✅ | ✅ | ❌ |
| DELETE /api/invoices/{id} | ✅ | ❌ | ❌ |

---

## 📝 Formato de Errores

Todos los errores siguen el siguiente formato:

```json
{
  "timestamp": "2025-10-28T11:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Descripción del error",
  "path": "/api/clients",
  "validationErrors": {
    "campo1": "mensaje de error del campo1",
    "campo2": "mensaje de error del campo2"
  }
}
```

---

## 🚀 Ejemplos de Uso con cURL

### Ejemplo 1: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Ejemplo 2: Crear Cliente
```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "nombre": "Juan",
    "apellido": "Pérez",
    "nit": 1234567890,
    "email": "juan@example.com",
    "telefono": "77123456",
    "direccion": "Av. Principal #123",
    "ciudad": "La Paz",
    "departamento": "La Paz"
  }'
```

### Ejemplo 3: Crear Factura
```bash
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "serie": "FAC",
    "fechaEmision": "2025-10-28",
    "clientId": 1,
    "tipoComprobante": "FACTURA",
    "detalles": [
      {
        "descripcion": "Producto A",
        "cantidad": 2,
        "precioUnitario": 100.00,
        "descuento": 0.00,
        "unidadMedida": "UND"
      }
    ],
    "observaciones": "Factura de prueba"
  }'
```

### Ejemplo 4: Descargar PDF
```bash
curl -X GET http://localhost:8080/api/invoices/1/pdf \
  -H "Authorization: Bearer {token}" \
  --output factura-1.pdf
```

---

## 📱 Swagger UI

Para una documentación interactiva y pruebas de la API, accede a:

**URL:** `http://localhost:8080/swagger-ui.html`

Swagger UI permite:
- Ver todos los endpoints disponibles
- Probar endpoints directamente desde el navegador
- Ver esquemas de request/response
- Autenticarse con JWT

---

## 🗄️ Base de Datos

### Configuración Requerida

El sistema requiere PostgreSQL instalado con la siguiente configuración:

```properties
Base de datos: factus_db
Usuario: postgres
Password: postgres
Puerto: 5432
Host: localhost
```

### Credenciales por Defecto

Al iniciar la aplicación por primera vez, se crea automáticamente:

**Usuario Admin:**
- Username: `admin`
- Password: `admin123`
- Role: `ROLE_ADMIN`
- Email: `admin@factus.com`

---

## 🔧 Configuración para Cliente Angular

Para consumir esta API desde Angular, configure:

1. **Base URL:** `http://localhost:8080`
2. **CORS:** Ya está configurado para permitir `http://localhost:4200`
3. **Interceptor JWT:** Agregar header `Authorization: Bearer {token}` en cada request
4. **Manejo de errores:** Capturar códigos 401 para redirigir a login

---

## 📞 Soporte

**Empresa:** Galarza TechCorp  
**Email:** contacto@galarzatechcorp.com  
**Versión API:** 1.0  
**Última Actualización:** 28/10/2025

