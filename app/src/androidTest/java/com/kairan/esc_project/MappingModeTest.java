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

public class MappingModeTest {

    @Rule
    public ActivityTestRule<MappingMode> MappingModeTestRule = new ActivityTestRule<MappingMode>(MappingMode.class);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testUserInputScenario_checkPhoto() throws InterruptedException{
        Thread.sleep(1000);
        Espresso.onView(withId(R.id.UrlUpload)).perform(click());
//        Espresso.pressBack();
//        Espresso.onView(withId(R.id.button_confirm)).perform(click());
//        Thread.sleep(2000);
    }

    @After
    public void tearDown() throws Exception {
    }


}