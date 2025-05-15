package com.example.avianquest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EncyclopediaFragmentTest {

    @Before
    public void setup() {
        // Launch the fragment
        FragmentScenario.launchInContainer(EncyclopediaFragment.class);
    }

    @Test
    public void testSearchUIComponents() {
        // Verify search components are displayed
        onView(withId(R.id.et_search)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_search)).check(matches(isDisplayed()));
    }

    @Test
    public void testEmptySearchQuery() {
        // Click search with empty query
        onView(withId(R.id.btn_search)).perform(click());
        // Verify toast message
        onView(withText("请输入鸟类名称")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testValidSearch() throws InterruptedException {
        // Type search query
        onView(withId(R.id.et_search)).perform(typeText("麻雀"));
        // Perform search
        onView(withId(R.id.btn_search)).perform(click());
        // Wait for network call
        Thread.sleep(2000);
        // Verify result is displayed
        onView(withId(R.id.tv_result)).check(matches(isDisplayed()));
    }
}