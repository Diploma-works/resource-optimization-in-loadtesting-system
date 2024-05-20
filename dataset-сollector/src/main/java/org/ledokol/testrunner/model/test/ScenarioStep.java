package org.ledokol.testrunner.model.test;

import lombok.Data;
import lombok.Getter;

@Data
public class ScenarioStep {

    @Getter
    private String action;

    private Integer totalUsersCount;

    private Integer countUsersByPeriod;

    private Double period;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getTotalUsersCount() {
        return totalUsersCount;
    }

    public void setTotalUsersCount(Integer totalUsersCount) {
        this.totalUsersCount = totalUsersCount;
    }

    public Integer getCountUsersByPeriod() {
        return countUsersByPeriod;
    }

    public void setCountUsersByPeriod(Integer countUsersByPeriod) {
        this.countUsersByPeriod = countUsersByPeriod;
    }

    public Double getPeriod() {
        return period;
    }

    public void setPeriod(Double period) {
        this.period = period;
    }
}
