package backend.proj5.dto;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
    private Number [] userStats;
    private double averageTaskTime;
    private ArrayList<String> categories;
    private List<Object[]> totalTasksDoneByEachDay;
    private List<Object[]> usersRegistred;

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

    public List<Object[]> getUsersRegistred() {
        return usersRegistred;
    }

    public void setUsersRegistred(List<Object[]> usersRegistred) {
        this.usersRegistred = usersRegistred;
    }
}

