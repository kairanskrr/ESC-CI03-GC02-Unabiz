package com.kairan.esc_project;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import java.util.Random;

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

    private String cUsername = "zoezoe@gmail.com";
    private String cPassword = "123456";
    private String invalidC = "Invalid Credentials";

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    private final String name = random();
    private final String incorrect_email = name + "@email.com";
    private final String incorrect_pw = random();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testUserInputScenario_LoginFalse() throws InterruptedException {
        // input invalid text into email and password box
        Espresso.onView(withId(R.id.editTextEmail)).perform(clearText());
        Espresso.onView(withId(R.id.editTextEmail)).perform(typeText(incorrect_email));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword)).perform(clearText());
        Espresso.onView(withId(R.id.editTextPassword)).perform(typeText(incorrect_pw));
        Espresso.closeSoftKeyboard();
        // perform button click
        Espresso.onView(withId(R.id.buttonLogin)).perform(click());
        Thread.sleep(2000);
        // check invalid account
        Espresso.onView(withId(R.id.tvInvalidC)).check(matches(withText(invalidC)));
    }

    @Test
    public void testUserInputScenario_LoginTrue() throws InterruptedException {
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
        // check that we entered Mode Selection view
        Espresso.onView(withId(R.id.MenuTitle)).check(matches(withText("Mode Selection")));
    }

    @After
    public void tearDown() throws Exception {
    }

    public static String random() {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(13);
        for(int i = 0; i < 13; ++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}