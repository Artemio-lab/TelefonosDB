package com.example.telefonosdb.GUI;

import com.example.telefonosdb.Logic.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Clase que sirve para poder probar el programa.
 * Crea personas, telefonos y direcciones para ver como funciona el programa
 */
public class Test {

    public static void main(String[] args) {
        PersonaDAO personaDAO = new PersonaDAO();
        TelefonoDAO telefonoDAO = new TelefonoDAO();
        DireccionDAO direccionDAO = new DireccionDAO();

        try {
            System.out.println("=== 1) Agregando personas ===");
            int idJuan = personaDAO.insertar("Juan Pérez");
            int idAna = personaDAO.insertar("Ana Gómez");
            System.out.println("Persona creada: Juan Pérez (id " + idJuan + ")");
            System.out.println("Persona creada: Ana Gómez (id " + idAna + ")");

            System.out.println("\n=== 2) Agregando teléfonos asociados ===");
            telefonoDAO.insertar(idJuan, "11-5555-0001");
            telefonoDAO.insertar(idJuan, "11-5555-0002");
            telefonoDAO.insertar(idAna, "11-5555-0003");
            System.out.println("Juan Pérez ahora tiene 2 teléfonos.");
            System.out.println("Ana Gómez ahora tiene 1 teléfono.");

            System.out.println("\n=== 3) Agregando direcciones ===");
            int idDirJuan = direccionDAO.insertar("Av. Siempre Viva 742");
            direccionDAO.asociar(idJuan, idDirJuan);
            System.out.println("Dirección propia creada y asociada a Juan Pérez: Av. Siempre Viva 742");

            int idDirOficina = direccionDAO.insertar("Calle Trabajo 100, Oficina 4");
            direccionDAO.asociar(idJuan, idDirOficina);
            System.out.println("Segunda dirección asociada a Juan Pérez (oficina): Calle Trabajo 100, Oficina 4");

            int idDirCompartida = direccionDAO.insertar("Calle Familiar 500");
            direccionDAO.asociar(idJuan, idDirCompartida);
            direccionDAO.asociar(idAna, idDirCompartida);
            System.out.println("Dirección compartida creada y asociada a Juan Pérez Y a Ana Gómez: Calle Familiar 500");

            System.out.println("\n=== 4) Listado final ===");
            for (Persona persona : List.of(
                    personaDAO.listarTodas().stream().filter(p -> p.getId() == idJuan).findFirst().orElseThrow(),
                    personaDAO.listarTodas().stream().filter(p -> p.getId() == idAna).findFirst().orElseThrow())) {

                System.out.println("\n" + persona.getNombre() + " (id " + persona.getId() + ")");

                System.out.println("  Teléfonos:");
                List<Telefono> telefonos = telefonoDAO.listarPorPersona(persona.getId());
                for (Telefono t : telefonos) {
                    System.out.println("    - " + t.getTelefono());
                }

                System.out.println("  Direcciones:");
                List<Direccion> direcciones = direccionDAO.listarPorPersona(persona.getId());
                for (Direccion d : direcciones) {
                    System.out.println("    - " + d.getDireccion());
                }
            }

            System.out.println("\n=== 5) Verificando quién comparte 'Calle Familiar 500' ===");
            List<Persona> personasEnDireccionCompartida = direccionDAO.listarPersonasPorDireccion(idDirCompartida);
            for (Persona p : personasEnDireccionCompartida) {
                System.out.println("  - " + p.getNombre());
            }

            //Si se quiere probar este código en vista será necesario borrar las siguientes lineas
            //Las cuales tienen como funcionalidad borrar los datos ingresados
            //region Borrar esta region
            System.out.println("\n=== 6) Limpiando datos de prueba ===");
            personaDAO.eliminar(idJuan);
            personaDAO.eliminar(idAna);
            System.out.println("Personas eliminadas (junto con sus teléfonos y asociaciones).");

            direccionDAO.eliminar(idDirJuan);
            direccionDAO.eliminar(idDirOficina);
            direccionDAO.eliminar(idDirCompartida);
            System.out.println("Direcciones de prueba eliminadas del catálogo.");
            //endregion

            System.out.println("\nPrueba finalizada sin errores.");

        } catch (SQLException e) {
            System.err.println("Error durante la prueba:");
            e.printStackTrace();
        }
    }
}
