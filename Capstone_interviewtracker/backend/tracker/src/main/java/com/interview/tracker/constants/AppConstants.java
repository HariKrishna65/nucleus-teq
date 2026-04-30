package com.interview.tracker.constants;

public class AppConstants {

    private AppConstants() {}

    // Base paths
    public static final String AUTH = "/auth";
    public static final String HR = "/hr";
    public static final String CANDIDATES = "/candidates";
    public static final String INTERVIEWS = "/interviews";
    public static final String JD = "/jd";
    public static final String FEEDBACK = "/feedback";

    // Auth sub-paths
    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String VERIFY = "/verify";
    public static final String VERIFY_AND_SET_PASSWORD = "/verify-and-set-password";
    public static final String SET_PASSWORD = "/set-password";
    public static final String FORGOT_PASSWORD = "/forgot-password";
    public static final String RESEND_VERIFICATION = "/resend-verification";
    public static final String CREATE_TEST_USER = "/create-test-user";

    // Roles
    public static final String ROLE_HR = "HR";
    public static final String ROLE_PANEL = "PANEL";
    public static final String ROLE_CANDIDATE = "CANDIDATE";
}