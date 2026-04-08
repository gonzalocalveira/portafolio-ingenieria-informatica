# SistemaAlmacen App

## Descripción
SistemaAlmacen App es una aplicación de escritorio desarrollada en Java para la gestión de un almacén. Permite manejar productos, compras, usuarios (clientes y empleados), facturas y medios de pago. El proyecto está estructurado siguiendo el patrón MVC (Modelo-Vista-Controlador) y utiliza archivos de texto para el almacenamiento de datos.

## Tecnologías Utilizadas
- **Lenguaje**: Java
- **Framework de UI**: Swing (con formularios generados por IntelliJ IDEA)
- **Gestión de Dependencias**: Maven
- **Almacenamiento**: Archivos de texto (.txt) para persistencia de datos

## Requisitos Previos
- JDK 8 o superior instalado
- Maven instalado
- IntelliJ IDEA o cualquier IDE compatible con Java

## Instalación
1. Clona el repositorio:
   ```
   git clone <url-del-repositorio>
   ```
2. Navega al directorio del proyecto:
   ```
   cd "SistemaAlmacen App"
   ```
3. Compila el proyecto con Maven:
   ```
   mvn clean compile
   ```

## Ejecución
1. Ejecuta la aplicación desde tu IDE o mediante Maven:
   ```
   mvn exec:java -Dexec.mainClass="MainFormulario"
   ```
   O directamente desde el IDE ejecutando la clase `MainFormulario.java`.

## Estructura del Proyecto
```
SistemaAlmacen App/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controlador/
│   │   │   │   └── ControladorSistemaAlmacen.java
│   │   │   ├── modelo/
│   │   │   │   ├── Carne.java
│   │   │   │   ├── CarritoDeCompra.java
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Empleado.java
│   │   │   │   ├── Factura.java
│   │   │   │   ├── Gaseosa.java
│   │   │   │   ├── GestorCompras.java
│   │   │   │   ├── GestorProductos.java
│   │   │   │   ├── GestorUsuario.java
│   │   │   │   ├── ManejoDeArchivos.java
│   │   │   │   ├── MediosDePago.java
│   │   │   │   ├── Producto.java
│   │   │   │   ├── Queso.java
│   │   │   │   ├── SistemaAlmacen.java
│   │   │   │   ├── Tarjeta.java
│   │   │   │   ├── Transferencia.java
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Interface/
│   │   │   │   │   ├── ICarritoDeCompra.java
│   │   │   │   │   ├── IFactura.java
│   │   │   │   │   ├── IGestorCompras.java
│   │   │   │   │   ├── IGestorProductos.java
│   │   │   │   │   ├── IGestorUsuario.java
│   │   │   │   │   ├── IManejoDeArchivos.java
│   │   │   │   ├── ISistemaAlmacen.java
│   │   │   │   └── testeo/
│   │   │   │       ├── TesteoCarritoCompras.java
│   │   │   │       ├── TesteoCliente.java
│   │   │   │       ├── TesteoFactura.java
│   │   │   │       ├── TesteoGestorCompras.java
│   │   │   │       ├── TesteoGestorProductos.java
│   │   │   │       ├── TesteoGestorUsuario.java
│   │   │   │       └── TesteoSistemaAlmacen.java
│   │   │   ├── servicio/
│   │   │   │   └── EnviadorEmail.java
│   │   │   └── vista/
│   │   │       ├── FormularioAyuda.form
│   │   │       ├── FormularioAyuda.java
│   │   │       ├── FormularioCarrito.form
│   │   │       ├── FormularioCarrito.java
│   │   │       ├── FormularioInicio.form
│   │   │       ├── FormularioInicio.java
│   │   │       ├── FormularioInicioSesionUsuario.form
│   │   │       ├── FormularioInicioSesionUsuario.java
│   │   │       ├── FormularioRegistroUsuario.form
│   │   │       ├── FormularioRegistroUsuario.java
│   │   │       ├── FormularioTarjeta.form
│   │   │       ├── FormularioTarjeta.java
│   │   │       ├── FormularioTransferencia.form
│   │   │       ├── FormularioTransferencia.java
│   │   │       ├── FormularioVerMenuEmpleado.form
│   │   │       └── FormularioVerMenuEmpleado.java
│   │   └── resources/
│   └── MainFormulario.java
├── target/
│   ├── classes/
│   └── test-classes/
├── pom.xml
├── SistemaAlmacenApp.iml
└── documentacion_proyecto.txt
```

### Descripción de Paquetes
- **controlador**: Contiene la lógica de control de la aplicación.
- **modelo**: Incluye las clases de dominio (Producto, Usuario, etc.) y gestores para manejar la lógica de negocio.
- **servicio**: Servicios adicionales como envío de emails.
- **vista**: Formularios de la interfaz gráfica de usuario.
- **Interface**: Interfaces para definir contratos en el modelo.
- **testeo**: Clases de prueba para validar funcionalidades.

## Funcionalidades Principales
- Gestión de productos (carnes, quesos, gaseosas, etc.)
- Gestión de usuarios (clientes y empleados)
- Carrito de compras
- Procesamiento de facturas
- Medios de pago (tarjeta, transferencia)
- Envío de emails
- Persistencia de datos en archivos de texto

## Pruebas
El proyecto incluye clases de testeo en el paquete `testeo`. Para ejecutar las pruebas:
```
mvn test
```

## Documentación
Consulta el archivo `documentacion_proyecto.txt` para más detalles sobre el proyecto.

## Autores
- Desarrollado como parte del Trabajo Práctico Obligatorio (TPO) para la materia Paradigma Orientado a Objetos.
- Universidad Argentina de la Empresa (UADE).
- Docente: LEON MARIA ANGELA.

## Licencia
Este proyecto es para fines educativos y no tiene una licencia específica.