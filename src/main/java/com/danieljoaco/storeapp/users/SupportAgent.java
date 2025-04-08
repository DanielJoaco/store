package com.danieljoaco.storeapp.users;

public class SupportAgent extends Users{
    
    private SupportAgent(String id, String email, String password, String name){
        super(id, email, password, UserType.SUPPORT_AGENT.name(), name);
    }
    
    public static SupportAgent createdSupportAgent(String id, String email, String password, String name, Admin creator){
        if (creator.isAdmin()) {
            SupportAgent newSupportAgent = new SupportAgent(id, email, password, name);
            UserDao.saveUser(newSupportAgent);
                return newSupportAgent;
        } else
           throw new IllegalArgumentException("Only an Admin can created another Support Agent."); 
    }
    /** Fábrica para cargar un SupportAgent desde la BD. */
    static SupportAgent createAgentFromDb(String id, String email, String passwordHash, String name) {
        return new SupportAgent(id, email, passwordHash, name);
    } 
}