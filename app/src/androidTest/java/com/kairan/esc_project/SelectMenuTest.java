package com.kairan.esc_project;

import androidx.test.espresso.Espresso;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

public class SelectMenuTest {

    @Rule
    public ActivityTestRule<SelectMenu> SelectMenuTestRule = new ActivityTestRule<SelectMenu>(SelectMenu.class);


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testUserInputScenario_TestSelection() throws InterruptedException {
        Espresso.onView(withId(R.id.MappingModeButton)).perform(click());
    }

    @After
    public void tearDown() throws Exception {
    }
}