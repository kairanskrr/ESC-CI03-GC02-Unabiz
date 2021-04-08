package com.kairan.esc_project;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

public class MappingActivityTest {

    @Rule
    public ActivityTestRule<MappingActivity> mappingActTestRule = new ActivityTestRule<MappingActivity>(MappingActivity.class);


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testUserInputScenario_LoginFalse() throws InterruptedException {
//        Espresso.onView(withId(R.id.scaleImage_waitingToMap)).perform(GeneralClickAction(Tap.LONG));
//        GeneralClickAction(Tap())
    }

    @After
    public void tearDown() throws Exception {
    }

}
