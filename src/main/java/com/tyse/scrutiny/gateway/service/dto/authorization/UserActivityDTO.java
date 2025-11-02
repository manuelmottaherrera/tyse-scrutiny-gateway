package com.tyse.scrutiny.gateway.service.dto.authorization;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing a user's audit activity statistics.
 */
public class UserActivityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private Long count;

    public UserActivityDTO() {
        // Empty constructor needed for Jackson
    }

    public UserActivityDTO(String username, Long count) {
        this.username = username;
        this.count = count;
    }

    // Getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserActivityDTO)) return false;
        UserActivityDTO that = (UserActivityDTO) o;
        return Objects.equals(username, that.username) && Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, count);
    }

    @Override
    public String toString() {
        return "UserActivityDTO{" + "username='" + username + '\'' + ", count=" + count + '}';
    }
}
