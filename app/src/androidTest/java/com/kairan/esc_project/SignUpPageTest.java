package com.kairan.esc_project;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;

import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

public class SignUpPageTest {

    @Rule
    public ActivityTestRule<SignUpPage> signUpPageTestRule = new ActivityTestRule<SignUpPage>(SignUpPage.class);

    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";
    private final String name = random();
    private final String email = name + "@email.com";
    private final String password = "1234567qwert";

    private final String fpassword = "1234";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void signUp_True() throws InterruptedException {
        // input invalid text into email and password box
        Espresso.onView(withId(R.id.editTextName)).perform(clearText());
        Espresso.onView(withId(R.id.editTextName)).perform(typeText(name));
        Espresso.onView(withId(R.id.editTextEmail2)).perform(typeText(email));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword2)).perform(typeText(password));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword3)).perform(typeText(password));
        Espresso.closeSoftKeyboard();
        // perform button click
        Espresso.onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(2000);
        // check valid account
        Espresso.onView(withId(R.id.textViewTestWifi)).check(matches(withText("Test WiFi")));
    }

    @Test
    public void signUp_False0() throws InterruptedException {
        // input invalid text into email and password box
        Espresso.onView(withId(R.id.editTextName)).perform(clearText());
        Espresso.onView(withId(R.id.editTextName)).perform(typeText(name));
        Espresso.onView(withId(R.id.editTextEmail2)).perform(typeText(email));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword2)).perform(typeText(fpassword));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword3)).perform(typeText(fpassword));
        Espresso.closeSoftKeyboard();
        // perform button click
        Espresso.onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(2000);
        // check valid account
        Espresso.onView(withId(R.id.textViewRegister)).check(matches(withText("Register")));
    }

    @Test
    public void signUp_False1() throws InterruptedException {
        // input invalid text into email and password box
        Espresso.onView(withId(R.id.editTextName)).perform(clearText());
        Espresso.onView(withId(R.id.editTextName)).perform(typeText(name));
        Espresso.onView(withId(R.id.editTextEmail2)).perform(typeText(name));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword2)).perform(typeText(password));
        Espresso.closeSoftKeyboard();
        Espresso.onView(withId(R.id.editTextPassword3)).perform(typeText(password));
        Espresso.closeSoftKeyboard();
        // perform button click
        Espresso.onView(withId(R.id.buttonRegister)).perform(click());
        Thread.sleep(2000);
        // check valid account
        Espresso.onView(withId(R.id.textViewRegister)).check(matches(withText("Register")));
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

