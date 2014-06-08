package model;

import javax.servlet.http.HttpServletRequest;

public class LoginForm {
    private HttpServletRequest request;
    private String username;
    private String password;

    public LoginForm() {
    }

    public boolean isAuthenticationSuccessful(HttpServletRequest request) {
        this.request = request;
        password = request.getParameter("password");
        username = request.getParameter("username");
        if (wasLoginButtonClicked() == false)
            return false;
        if (areloginParametersNotNull()) {
            if (AccountForm.isLoginSuccessful(username, password)) {
                createWelcomeName();
                return true;
            }
            else {
                generateAuthenticationError();
                return false;
            }
        }
        else {
            generateNullError();
            return false;
        }
    }

    private boolean wasLoginButtonClicked() {
        return request.getParameter("loginButton") != null;
    }

    private boolean areloginParametersNotNull() {
        return passwordNotNull() && usernameNotNull();
    }

    private void createWelcomeName() {
        UserAccount currentAccount = AccountForm.getUserAccounts().get(username);
        String welcomeName = currentAccount.getName();
        Attributes.storeAttribute(Attributes.CURRENT_USER, welcomeName);
    }

    private void generateNullError() {
        if (usernameNotNull() == false) {
            request.setAttribute("error", "Unknown user. Please try again.");
        }
        else if (passwordNotNull() == false) {
            request.setAttribute("error", "Password is incorrect. Please try again.");
        }
    }

    private void generateAuthenticationError() {
        if (usernameNotNull() && passwordNotNull()) {
            request.setAttribute("error", "Authentication failed. Please try again.");
        }
    }

    private boolean passwordNotNull() {
        return password != null && !password.isEmpty();
    }

    private boolean usernameNotNull() {
        return username != null && !username.isEmpty();
    }

    private void resetErrorMessage() {
        request.setAttribute("error", "");
    }
}
