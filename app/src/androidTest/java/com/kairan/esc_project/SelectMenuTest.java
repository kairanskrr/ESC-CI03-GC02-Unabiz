package com.kairan.esc_project;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

public class SelectMenuTest {

    @Rule
    public ActivityTestRule<SelectMenu> SelectMenuTestRule = new ActivityTestRule<SelectMenu>(SelectMenu.class);


    @Before
    public void setUp() throws Exception {
        System.out.println("set up");
    }

    @Test
    public void testUserInputScenario_TestSelection_Mapping() throws InterruptedException {
        System.out.println("trying to perform click");
        Espresso.onView(withId(R.id.MappingModeButton)).perform(click());
        Espresso.onView(withId(R.id.MappingTitle)).check(matches(withText("Upload Floorplan")));
    }

    @Test
    public void testUserInputScenario_TestSelection_Testing() throws InterruptedException {
        System.out.println("trying to perform click");
        Espresso.onView(withId(R.id.TestingModeButton)).perform(click());
        Espresso.onView(withId(R.id.button_selectMap)).check(matches(withText("Select Available Map")));
    }

    @Test
    public void testUserInputScenario_TestSelection_WifiScanning() throws InterruptedException {
        System.out.println("trying to perform click");
        Espresso.onView(withId(R.id.WifiScannerButton)).perform(click());
        Espresso.onView(withId(R.id.textViewTestWifi)).check(matches(withText("Test WiFi Scanning")));
    }

    @After
    public void tearDown() throws Exception {
    }
}