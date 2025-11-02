package com.tyse.scrutiny.gateway.service.dto.authorization;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO representing audit activity statistics for different time periods.
 */
public class ActivityPeriodDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long last24Hours;
    private Long last7Days;
    private Long last30Days;

    public ActivityPeriodDTO() {
        // Empty constructor needed for Jackson
    }

    public ActivityPeriodDTO(Long last24Hours, Long last7Days, Long last30Days) {
        this.last24Hours = last24Hours;
        this.last7Days = last7Days;
        this.last30Days = last30Days;
    }

    // Getters and setters

    public Long getLast24Hours() {
        return last24Hours;
    }

    public void setLast24Hours(Long last24Hours) {
        this.last24Hours = last24Hours;
    }

    public Long getLast7Days() {
        return last7Days;
    }

    public void setLast7Days(Long last7Days) {
        this.last7Days = last7Days;
    }

    public Long getLast30Days() {
        return last30Days;
    }

    public void setLast30Days(Long last30Days) {
        this.last30Days = last30Days;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActivityPeriodDTO)) return false;
        ActivityPeriodDTO that = (ActivityPeriodDTO) o;
        return (
            Objects.equals(last24Hours, that.last24Hours) &&
            Objects.equals(last7Days, that.last7Days) &&
            Objects.equals(last30Days, that.last30Days)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(last24Hours, last7Days, last30Days);
    }

    @Override
    public String toString() {
        return ("ActivityPeriodDTO{" + "last24Hours=" + last24Hours + ", last7Days=" + last7Days + ", last30Days=" + last30Days + '}');
    }
}
