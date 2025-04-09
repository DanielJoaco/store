package com.danieljoaco.storeapp;
import java.sql.SQLException;

import com.danieljoaco.storeapp.users.UserDao;
import com.danieljoaco.storeapp.users.*;

public class StoreApp {

    public static void main(String[] args)  {
        // Crear un admin de prueba si es necesario
        try {
        System.out.println(UserDao.adminExists());
        if (!UserDao.adminExists()) {
            System.out.println("Creando primer admin.");
            Admin.createFirtsAdmin("12345", "example@example", "password2", "New Admin");
        }

        // Crear un cliente o soporte si se desea
        System.out.println("Iniciando sesion");
        Admin adminCreator = Admin.instanceAdmin("example@example", "password2");
        System.out.println("Admin creado: " + adminCreator.getTypeUser());
        System.out.println(adminCreator.getTypeUser());
        
        
        SupportAgent agent = SupportAgent.createdSupportAgent("1234567", "example2@example2", "password3", "agent", adminCreator);
        Admin admin2 = Admin.createAdmin("example@example", "password2", "321234", "exampe2@example", "password123", "admin");
        System.out.println("Creado admin 2 " + admin2.getTypeUser());
        SupportAgent agent2 = SupportAgent.createdSupportAgent("1234567", "example2@example2", "password3", "agent", adminCreator);
        System.out.println(agent2.getTypeUser());
            } catch (SQLException e) {
        e.printStackTrace();
    }

        // Verificar la inserción
        //Users retrievedUser = UserDao.findUserById("2");
        //System.out.println("Usuario Recuperado: " + retrievedUser.getName());
    }
}
