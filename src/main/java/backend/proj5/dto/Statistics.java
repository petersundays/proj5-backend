package backend.proj5.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Statistics {
    private Number [] userStats;
    private double averageTaskTime;
    private ArrayList<String> categories;
    private List<Object[]> totalTasksDoneByEachDay;
    private List<Object[]> usersRegistered;

    public Statistics() {
    }

    public Number[] getUserStats() {
        return userStats;
    }

    public void setUserStats(Number[] userStats) {
        this.userStats = userStats;
    }

    public double getAverageTaskTime() {
        return averageTaskTime;
    }

    public void setAverageTaskTime(double averageTaskTime) {
        this.averageTaskTime = averageTaskTime;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public List<Object[]> getTotalTasksDoneByEachDay() {
        return totalTasksDoneByEachDay;
    }

    public void setTotalTasksDoneByEachDay(List<Object[]> totalTasksDoneByEachDay) {
        this.totalTasksDoneByEachDay = totalTasksDoneByEachDay;
    }

    public List<Object[]> getUsersRegistered() {
        return usersRegistered;
    }

    public void setUsersRegistered(List<Object[]> usersRegistered) {
        this.usersRegistered = usersRegistered;
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "userStats=" + Arrays.toString(userStats) +
                ", averageTaskTime=" + averageTaskTime +
                ", categories=" + categories +
                ", totalTasksDoneByEachDay=" + totalTasksDoneByEachDay +
                ", usersRegistered=" + usersRegistered +
                '}';
    }
}

