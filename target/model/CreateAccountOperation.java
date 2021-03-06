package model;

import database.User;

import java.sql.SQLException;

public class CreateAccountOperation implements Validation {
    private String password, confirmPassword;

    public void init(User account, String username, String password) {
        this.password = account.getPassword();
        this.confirmPassword = password;
    }

    public void validateCredentials() throws SQLException {
        if (!password.equals(confirmPassword)) {
            throw new SQLException("Passwords do not match. "
                + "Please try again.");
        }
    }
}
