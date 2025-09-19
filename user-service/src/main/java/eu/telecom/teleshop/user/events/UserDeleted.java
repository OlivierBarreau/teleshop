package eu.telecom.teleshop.user.events;

public class UserDeleted {
    private Long userId;

    public UserDeleted() {}
    public UserDeleted(Long userId) { this.userId = userId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
