package com.danieljoaco.storeapp.users;

import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

public class Admin extends Users {
    
    // Constructor privado, no puede ser llamado desde fuera de la clase
    private Admin(String id, String email, String password, String name) {
        super(id, email, password, UserType.ADMIN.name(), name);
    }
    private Admin(String id, String email, String password, String name, boolean isIstance) {
        super(id, email, password, UserType.ADMIN.name(), name, isIstance);
    }

    public static Admin createFirtsAdmin(
        String id,
        String email,
        String password,
        String name        
    )   throws SQLException {
        // 1) No Debe existir un Admin
        if (UserDao.adminExists()) {
            throw new IllegalStateException("At least 1 admin already exists, use createdAdmin().");
        }
        return new Admin(id, email, password, name);
    }

    public static Admin createAdmin(
        String id,
        String email,
        String password,
        String name,
        Admin admin
    ) throws SQLException {
        if(admin.isAdmin()){
            return new Admin(id, email, password, name);
        } else {
            throw new IllegalArgumentException("The user is not an admin.");
        }
    }

    public static Admin loginAdmin(String emailAccess, String passwordAccess) throws SQLException {
        if (!UserDao.adminExists()) {
            throw new IllegalStateException("No admin exists. Use createFirstAdmin(...) first.");
        }
        Users user = UserDao.findUserByEmail(emailAccess);
        if (user != null) {
            if (BCrypt.checkpw(passwordAccess, user.getPasswordHash()) &&
                    user.getTypeUser().equals(UserType.ADMIN.name())){
                return (Admin) user;
            } else{
                throw new IllegalStateException("Incorrect credentials.");
            }
        } else {
            throw new IllegalStateException("Incorrect credentials.");
        }
    }
    static Admin createAdminFromDb(String id, String email, String passwordHash, String name) {
        boolean isInstance = true;
        return new Admin(id, email, passwordHash, name, isInstance);
    }
    
    @Override
    public boolean isAdmin() {
        return true;
    }
}

