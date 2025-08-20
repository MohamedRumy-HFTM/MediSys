package ch.medisys.util;

/**
 * Singleton-Klasse zur Verwaltung der aktuellen Benutzersession
 */
public class SessionManager {
    
    private static SessionManager instance;
    
    private int userId;
    private String userName;
    private String userRole;
    private String userEmail;
    
    private SessionManager() {
        // Private constructor für Singleton
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    // Getter und Setter
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public boolean isArzt() {
        return "Arzt".equals(userRole);
    }
    
    public boolean isMPA() {
        return "MPA".equals(userRole);
    }
    
    public boolean isTherapeut() {
        return "Therapeut".equals(userRole);
    }
    
    public void clearSession() {
        userId = 0;
        userName = null;
        userRole = null;
        userEmail = null;
    }
}
