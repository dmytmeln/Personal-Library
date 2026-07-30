package org.example.library.config;

import org.example.library.reading_goal.domain.ReadingGoal;
import org.example.library.user.domain.User;

public class ReadingGoalConfigurer {

    private final TestDbClient testDbClient;

    private User user;
    private boolean userSet;
    private int year = 2026;
    private int targetBooks = 12;
    private Integer targetPages;

    public ReadingGoalConfigurer(TestDbClient testDbClient) {
        this.testDbClient = testDbClient;
    }

    public ReadingGoalConfigurer user(User user) {
        this.user = user;
        this.userSet = true;
        return this;
    }

    public ReadingGoalConfigurer year(int year) {
        this.year = year;
        return this;
    }

    public ReadingGoalConfigurer targetBooks(int targetBooks) {
        this.targetBooks = targetBooks;
        return this;
    }

    public ReadingGoalConfigurer targetPages(Integer targetPages) {
        this.targetPages = targetPages;
        return this;
    }

    public ReadingGoal save() {
        if (!userSet) {
            user = new UserConfigurer(testDbClient).save();
        }

        var goal = ReadingGoal.builder()
                .user(user)
                .year(year)
                .targetBooks(targetBooks)
                .targetPages(targetPages)
                .build();

        testDbClient.saveReadingGoal(goal);
        return goal;
    }

}
