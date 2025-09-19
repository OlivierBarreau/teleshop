package eu.telecom.teleshop.notification.events;

public class UserDeleted {
    private Long userId;

    public UserDeleted() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
