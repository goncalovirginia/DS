package api;

/**
 * Represents a user in the system. Note: the password of a user should not be
 * returned in any method.
 */
public class User {

    private String name;
    private String pwd;
    private String displayName;
    private String domain;

    public User() {
    }

    public User(String name, String pwd, String domain, String displayName) {
        super();
        this.pwd = pwd;
        this.name = name;
        this.domain = domain;
        this.displayName = displayName;
    }

    public User(User user) {
        super();
        this.name = user.getName();
        this.pwd = user.getPwd();
        this.displayName = user.getDisplayName();
        this.domain = user.getDomain();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", pwd=" + pwd + ", displayName=" + displayName + ", domain=" + domain + "]";
    }

    public void setNonNullAttributes(User user) {
        if (user.getPwd() != null) {
            this.pwd = user.getPwd();
        }
        if (user.getDisplayName() != null) {
            this.displayName = user.getDisplayName();
        }
        if (user.getDomain() != null) {
            this.domain = user.getDomain();
        }
    }
}
