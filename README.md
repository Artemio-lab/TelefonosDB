# Agenda - Personas, Teléfonos y Direcciones (JavaFX + MariaDB)

Aplicación de escritorio con JavaFX para gestionar Altas, Bajas y
Modificaciones (ABM) sobre `Personas`, `Telefonos` y `Direcciones`.

- **Personas ↔ Telefonos**: relación uno a muchos (una persona, varios teléfonos).
- **Personas ↔ Direcciones**: relación **muchos a muchos** — una persona
  puede tener varias direcciones, y una misma dirección puede ser
  compartida por varias personas (por ejemplo, una familia en la misma
  casa) — resuelta con la tabla intermedia `PersonaDireccion`.

## Estructura del proyecto

agenda-app/
├── pom.xml
├── schema.sql                 -> referencia del esquema (no se ejecuta desde la app)
├── ANALISIS_POO.md            -> tabla: conceptos de POO vs. código del proyecto
└── src/
    ├── main/java/com/example/telefonosdb/
    │   ├── module-info.java
    │   ├── Conexion.java       -> conexión centralizada a la base de datos
    │   ├── GUI/
    │   │   └── MainApp.java    -> interfaz gráfica (JavaFX), clase principal
    │   └── Logic/
    │       ├── Persona.java
    │       ├── Telefono.java
    │       ├── Direccion.java
    │       ├── PersonaDAO.java
    │       ├── TelefonoDAO.java
    │       └── DireccionDAO.java
    └── test/java/com/example/telefonosdb/Logic/
        ├── PersonaDAOTest.java
        ├── TelefonoDAOTest.java
        └── DireccionDAOTest.java
```

## Ejecutar las pruebas

bash mvn test

Todas son pruebas de **integración** con JUnit 5 contra la base real
definida en `Conexion.java`; cada una limpia los datos que crea.
`DireccionDAOTest` cubre específicamente los casos de la relación N:M:
una persona con varias direcciones, una dirección compartida por dos
personas, desasociar sin borrar del catálogo, y que eliminar una persona
no afecte una dirección que otra persona sigue compartiendo.

## Funcionalidad

### Pestaña "Personas"
Alta, baja y modificación por nombre. Al eliminar una persona se borran
sus teléfonos y sus asociaciones de dirección (no las direcciones en sí,
por si las comparte alguien más), y se refrescan los combos de las otras
dos pestañas.

### Pestaña "Teléfonos"
Elegir una persona del combo (precargado al iniciar) y gestionar sus
teléfonos con alta, baja y modificación.

### Pestaña "Direcciones"
Elegir una persona del combo y gestionar sus direcciones:
- **Asociar existente**: vincula una dirección ya creada (del catálogo)
  a la persona seleccionada — así se comparte entre personas.
- **Crear nueva y asociar**: da de alta una dirección nueva en el
  catálogo y la asocia de una vez a la persona seleccionada.
- **Editar texto**: modifica el texto de la dirección seleccionada.
  Como puede estar compartida, pide confirmación explícita porque
  afecta a todas las personas que la tengan asociada.
- **Quitar de esta persona**: elimina solo el vínculo (la dirección
  sigue existiendo en el catálogo para quien más la use).
- **Eliminar dirección (definitivo)**: borra la dirección del catálogo
  por completo, quitándola también de cualquier otra persona que la
  compartiera. Pide confirmación.

## Notas de diseño

- Toda la lógica de acceso a datos vive en los DAO (`PersonaDAO`,
  `TelefonoDAO`, `DireccionDAO`), separada de la interfaz gráfica.
- Se usan `PreparedStatement` en todas las operaciones para evitar
  inyección SQL.
- `PersonaDAO.eliminar()` y `DireccionDAO.eliminar()` usan transacciones
  manuales (`setAutoCommit(false)` + `commit()`/`rollback()`) para
  mantener la integridad referencial al borrar en varias tablas a la vez.
- `module-info.java` declara `opens` para `GUI` (hacia
  `javafx.graphics`/`javafx.fxml`) y para `Logic` (hacia `javafx.base`),
  necesarios porque `PropertyValueFactory` y el arranque de
  `Application` acceden a esas clases por reflexión.
- Ver `ANALISIS_POO.md` para la relación entre el código y los conceptos
  de programación orientada a objetos.
