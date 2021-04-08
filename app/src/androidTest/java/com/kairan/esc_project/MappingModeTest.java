package com.kairan.esc_project;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

public class MappingModeTest {

    @Rule
    public ActivityTestRule<MappingMode> MappingModeTestRule = new ActivityTestRule<MappingMode>(MappingMode.class);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testUserInputScenario_urlUpload() throws InterruptedException{
        Espresso.onView(withId(R.id.UrlUpload)).perform(click());
        Espresso.onView(withId(R.id.UrlEntry)).perform(typeText("https://i.insider.com/5cdec279021b4c0a911a1d3a?width=600&format=jpeg&auto=webp"));
        Espresso.closeSoftKeyboard();
        Thread.sleep(1000);
        Espresso.onView(withId(R.id.ConfirmURL)).perform(click());
    }

    @Test
    public void testUserInputScenario_urlUpload_False() throws InterruptedException{
        Espresso.onView(withId(R.id.UrlUpload)).perform(click());
        Thread.sleep(1000);
        Espresso.onView(withId(R.id.ConfirmURL)).perform(click());
        Espresso.onView(withId(R.id.MappingTitle)).check(matches(withText("Upload Floorplan")));
    }

    @After
    public void tearDown() throws Exception {
    }


}