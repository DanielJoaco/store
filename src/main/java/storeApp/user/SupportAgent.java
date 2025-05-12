package storeApp.user;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;

public class SupportAgent extends User {
    
    private SupportAgent(String id, String email, String password, String name){
        super(id, email, password, UserType.SUPPORT_AGENT.name(), name);
    }
    private SupportAgent(String id, String email, String password, String name, LocalDate createdAt){
        super(id, email, password, UserType.SUPPORT_AGENT.name(), name, createdAt);
    }
    
    public static SupportAgent createdSupportAgent(String id, String email, String password, String name, Admin creator){
        if (creator.isAdmin()) {
                return new SupportAgent(id, email, password, name);
        } else
           throw new IllegalArgumentException("Only an Admin can created another Support Agent."); 
    }

    public static SupportAgent loginSupportAgent(String emailAccess, String passwordAccess){
        User user = UserDao.findUserByEmail(emailAccess);
        assert user != null;
        if (!BCrypt.checkpw(passwordAccess, user.getPasswordHash()) ||
                !user.getTypeUser().equals(UserType.SUPPORT_AGENT.name())) {
            throw new IllegalStateException("Incorrect credentials.");
        }
        System.out.println("Credentials are correct. Welcome " + user.getName());
        return (SupportAgent) user;

    }

    /** Fábrica para cargar un SupportAgent desde la BD. */
    static SupportAgent createAgentFromDb(String id, String email, String passwordHash, String name, LocalDate createdAt) {
        return new SupportAgent(id, email, passwordHash, name, createdAt);
    } 
}