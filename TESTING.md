# 🧪 Testing Suite - Factus API

## Estructura de Tests

Este proyecto incluye **60 tests completos** (20 por cada recurso: Client, Invoice, Detail) distribuidos entre tests unitarios e integración.

---

## 📁 Estructura de Carpetas

```
src/test/java/dev/galarza/factus/
├── client/
│   ├── unit/
│   │   └── ClientServiceUnitTest.java (10 tests unitarios)
│   └── integration/
│       └── ClientIntegrationTest.java (10 tests de integración)
├── invoice/
│   ├── unit/
│   │   └── InvoiceServiceUnitTest.java (10 tests unitarios)
│   └── integration/
│       └── InvoiceIntegrationTest.java (10 tests de integración)
└── detail/
    ├── unit/
    │   └── DetailEntityUnitTest.java (10 tests unitarios)
    └── integration/
        └── DetailIntegrationTest.java (10 tests de integración)
```

---

## 🎯 Cobertura de Tests

### **CLIENT (20 tests)**

#### Tests Unitarios (10)
1. ✅ Crear cliente exitosamente con todos los datos válidos
2. ✅ Crear cliente con NIT duplicado lanza excepción
3. ✅ Crear cliente con email duplicado lanza excepción
4. ✅ Obtener cliente por ID inexistente lanza excepción
5. ✅ Buscar cliente por NIT exitosamente
6. ✅ Listar todos los clientes activos correctamente
7. ✅ Actualizar cliente con validación de NIT y email únicos
8. ✅ Actualizar cliente con mismo NIT no valida duplicado
9. ✅ Búsqueda de clientes con múltiples resultados y mapeo correcto
10. ✅ Eliminación lógica de cliente (soft delete) cambia estado a inactivo

#### Tests de Integración (10)
1. ✅ Crear cliente con datos completos y verificar persistencia en BD
2. ✅ Crear múltiples clientes y verificar unicidad de NIT
3. ✅ Buscar cliente por NIT y verificar datos completos
4. ✅ Actualizar cliente y verificar cambios en BD
5. ✅ Listar clientes activos vs inactivos correctamente
6. ✅ Búsqueda de clientes por nombre, apellido y NIT
7. ✅ Eliminación lógica (soft delete) mantiene datos en BD
8. ✅ Validación de NIT de 10 dígitos en creación
9. ✅ Transacción completa de CRUD en secuencia
10. ✅ Concurrencia - Crear múltiples clientes simultáneamente

---

### **INVOICE (20 tests)**

#### Tests Unitarios (10)
1. ✅ Crear factura con cálculo automático de impuestos (IVA 13%, IT 3%)
2. ✅ Generar número de factura secuencial por serie
3. ✅ Crear factura con cliente inexistente lanza excepción
4. ✅ Crear factura con múltiples detalles calcula subtotales correctamente
5. ✅ Emitir factura solo funciona con estado BORRADOR
6. ✅ Emitir factura ya emitida lanza excepción
7. ✅ Anular factura pagada lanza excepción
8. ✅ Buscar facturas por rango de fechas
9. ✅ Validar transiciones de estado permitidas
10. ✅ No se puede cambiar estado de factura anulada

#### Tests de Integración (10)
1. ✅ Crear factura con cálculo automático de IVA 13% e IT 3%
2. ✅ Crear factura con múltiples detalles y descuentos
3. ✅ Generar número de factura secuencial por serie
4. ✅ Emitir factura cambia estado de BORRADOR a EMITIDA
5. ✅ Anular factura emitida funciona correctamente
6. ✅ No se puede anular factura pagada
7. ✅ Buscar facturas por cliente retorna todas sus facturas
8. ✅ Filtrar facturas por estado funciona correctamente
9. ✅ Buscar facturas por rango de fechas
10. ✅ Ciclo completo CRUD de factura con validaciones

---

### **DETAIL (20 tests)**

#### Tests Unitarios (10)
1. ✅ Calcular subtotal sin descuento
2. ✅ Calcular subtotal con descuento
3. ✅ Calcular subtotal con cantidad grande
4. ✅ Calcular subtotal con precio unitario decimal
5. ✅ Calcular subtotal con descuento mayor que subtotal sin descuento
6. ✅ Validar que cantidad mínima es 1
7. ✅ Validar precio unitario positivo
8. ✅ Calcular subtotal con valores muy pequeños
9. ✅ Builder pattern crea detail correctamente
10. ✅ Validar campos opcionales pueden ser null

#### Tests de Integración (10)
1. ✅ Crear detalle a través de factura persiste correctamente
2. ✅ Múltiples detalles con diferentes unidades de medida
3. ✅ Cálculo de subtotal con descuento en detalles
4. ✅ Detalles con precios decimales precisos
5. ✅ Validación de cantidad mínima en detalle
6. ✅ Validación de precio unitario mínimo
7. ✅ Detalles con códigos de producto únicos
8. ✅ Detalle con descripción larga
9. ✅ Relación bidireccional entre Invoice y Detail
10. ✅ Cascada de eliminación - eliminar factura elimina detalles

---

## 🔧 Tecnologías Utilizadas

- **JUnit 5** - Framework de testing
- **Mockito** - Mocking para tests unitarios
- **AssertJ** - Assertions fluidas
- **Spring Boot Test** - Soporte de testing de Spring
- **H2 Database** - Base de datos en memoria para tests
- **REST Assured** - Testing de APIs REST
- **MockMvc** - Testing de controladores

---

## ▶️ Ejecutar Tests

### Ejecutar todos los tests
```bash
mvnw test
```

### Ejecutar tests de un recurso específico
```bash
# Client tests
mvnw test -Dtest=Client*

# Invoice tests
mvnw test -Dtest=Invoice*

# Detail tests
mvnw test -Dtest=Detail*
```

### Ejecutar solo tests unitarios
```bash
mvnw test -Dtest=**/*UnitTest
```

### Ejecutar solo tests de integración
```bash
mvnw test -Dtest=**/*IntegrationTest
```

### Ejecutar con cobertura de código
```bash
mvnw clean test jacoco:report
```

---

## 📊 Reporte de Cobertura

Los reportes de cobertura se generan en:
```
target/site/jacoco/index.html
```

---

## 🎨 Características de los Tests

### Tests Unitarios
- ✅ Uso de Mocks para aislar dependencias
- ✅ Validación de lógica de negocio
- ✅ Pruebas de excepciones y validaciones
- ✅ Testing de cálculos y transformaciones
- ✅ Verificación de llamadas a repositorios

### Tests de Integración
- ✅ Base de datos H2 en memoria
- ✅ Transacciones con rollback automático
- ✅ Autenticación con usuarios de prueba
- ✅ Pruebas end-to-end de APIs REST
- ✅ Validación de persistencia real
- ✅ Testing de relaciones entre entidades

---

## 🔐 Seguridad en Tests

Los tests de integración utilizan:
- `@WithMockUser` para simular usuarios autenticados
- Roles: `USER`, `ADMIN` según el endpoint
- JWT configurado para ambiente de test

---

## 📝 Convenciones de Naming

### Clases
- Tests Unitarios: `*ServiceUnitTest` o `*EntityUnitTest`
- Tests de Integración: `*IntegrationTest`

### Métodos
```java
@Test
@DisplayName("Test N: Descripción clara del test")
void testNombreDescriptivo_Escenario_ResultadoEsperado() {
    // Given - Preparación
    // When - Acción
    // Then - Verificación
}
```

---

## 🎯 Casos de Prueba Complejos

### Validaciones
- ✅ NIT de 10 dígitos exactos
- ✅ Emails únicos en el sistema
- ✅ Precios decimales con precisión
- ✅ Cálculos de impuestos (IVA 13%, IT 3%)

### Lógica de Negocio
- ✅ Transiciones de estados de facturas
- ✅ Soft delete vs hard delete
- ✅ Números secuenciales por serie
- ✅ Cálculo automático de totales

### Integridad de Datos
- ✅ Relaciones bidireccionales
- ✅ Cascadas de eliminación
- ✅ Persistencia transaccional
- ✅ Búsquedas complejas con filtros

---

## 🐛 Debugging Tests

Para ejecutar tests en modo debug:
```bash
mvnw test -Dmaven.surefire.debug
```

Luego conectar el debugger en el puerto 5005.

---

## 📈 Mejores Prácticas Implementadas

1. **AAA Pattern** - Arrange, Act, Assert
2. **Test Isolation** - Cada test es independiente
3. **Descriptive Names** - Nombres claros y descriptivos
4. **Given-When-Then** - Estructura clara de tests
5. **Mock Strategic** - Solo mockear dependencias externas
6. **Clean Up** - @BeforeEach y @Transactional para limpieza
7. **Assertions Fluidas** - Uso de AssertJ para legibilidad
8. **Test Order** - @Order para tests de integración secuenciales

---

## 🚀 Continuous Integration

Los tests están listos para ejecutarse en CI/CD:
- ✅ Base de datos en memoria (sin dependencias externas)
- ✅ Configuración por perfiles (test profile)
- ✅ Ejecución rápida y confiable
- ✅ Sin efectos secundarios

---

## 📞 Soporte

Para preguntas sobre los tests:
- **Email:** test-support@galarzatechcorp.com
- **Documentación:** Ver JavaDoc en cada clase de test

---

**Total: 60 Tests Implementados** ✅
- 30 Tests Unitarios
- 30 Tests de Integración
- 100% Cobertura de recursos principales

