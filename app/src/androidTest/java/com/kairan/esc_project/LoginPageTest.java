package com.kairan.esc_project;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

public class LoginPageTest {

    @Rule
    public ActivityTestRule<LoginPage> loginPageTestRule = new ActivityTestRule<LoginPage>(LoginPage.class);

    private String username = "test123@email.com";
    private String password = "123qweasd";
    private String cUsername = "zoezoe@gmail.com";
    private String cPassword = "123456";
    private String invalidC = "Invalid Credentials";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testUserInputScenario() throws InterruptedException {
        // input invalid text into email and password box
        Espresso.onView(withId(R.id.editTextEmail)).perform(clearText());
        Espresso.onView(withId(R.id.editTextEmail)).perform(typeText(username));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword)).perform(clearText());
        Espresso.onView(withId(R.id.editTextPassword)).perform(typeText(password));
        Espresso.closeSoftKeyboard();
        // perform button click
        Espresso.onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2000);
        // check invalid account
        Espresso.onView(withId(R.id.tvInvalidC)).check(matches(withText(invalidC)));
    }

    @Test
    public void testUserInputScenario2() throws InterruptedException {
        // input invalid text into email and password box
        Espresso.onView(withId(R.id.editTextEmail)).perform(clearText());
        Espresso.onView(withId(R.id.editTextEmail)).perform(typeText(cUsername));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword)).perform(clearText());
        Espresso.onView(withId(R.id.editTextPassword)).perform(typeText(cPassword));
        Espresso.closeSoftKeyboard();
        // perform button click
        Espresso.onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2000);
        // see if the select menu page is brought up???????????????
        Espresso.onView(withText("Mode Selection"));
    }

    @After
    public void tearDown() throws Exception {
    }
}