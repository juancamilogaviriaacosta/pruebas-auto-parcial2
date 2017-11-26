package org.gnucash.android.ui.account;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.gnucash.android.R;
import org.gnucash.android.app.GnuCashApplication;
import org.gnucash.android.db.DatabaseHelper;
import org.gnucash.android.db.adapter.AccountsDbAdapter;
import org.gnucash.android.db.adapter.BooksDbAdapter;
import org.gnucash.android.db.adapter.CommoditiesDbAdapter;
import org.gnucash.android.db.adapter.DatabaseAdapter;
import org.gnucash.android.db.adapter.SplitsDbAdapter;
import org.gnucash.android.db.adapter.TransactionsDbAdapter;
import org.gnucash.android.model.Account;
import org.gnucash.android.model.Commodity;
import org.gnucash.android.test.ui.util.DisableAnimationsRule;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.gnucash.android.test.ui.AccountsActivityTest.preventFirstRunDialogs;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.Is.is;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class Mutante706Test {

    @Rule
    public ActivityTestRule<AccountsActivity> mActivityTestRule = new ActivityTestRule<>(AccountsActivity.class);


    private static final String ACCOUNTS_CURRENCY_CODE = "USD";
    private static final String SIMPLE_ACCOUNT_NAME = "Simple account";
    private static final String SIMPLE_ACCOUNT_UID = "simple-account";

    private static DatabaseHelper mDbHelper;
    private static SQLiteDatabase mDb;
    private static AccountsDbAdapter mAccountsDbAdapter;
    private static TransactionsDbAdapter mTransactionsDbAdapter;
    private static SplitsDbAdapter mSplitsDbAdapter;
    private AccountsActivity mAccountsActivity;

    @BeforeClass
    public static void prepTest(){
        preventFirstRunDialogs(GnuCashApplication.getAppContext());

        String activeBookUID = BooksDbAdapter.getInstance().getActiveBookUID();
        mDbHelper = new DatabaseHelper(GnuCashApplication.getAppContext(), activeBookUID);
        try {
            mDb = mDbHelper.getWritableDatabase();
        } catch (SQLException e) {
            Log.e("AccountsActivityTest", "Error getting database: " + e.getMessage());
            mDb = mDbHelper.getReadableDatabase();
        }
        mSplitsDbAdapter        = SplitsDbAdapter.getInstance();
        mTransactionsDbAdapter  = TransactionsDbAdapter.getInstance();
        mAccountsDbAdapter      = AccountsDbAdapter.getInstance();
        CommoditiesDbAdapter commoditiesDbAdapter = new CommoditiesDbAdapter(mDb);
    }

    @Before
    public void setUp() throws Exception {
        mAccountsActivity = mActivityTestRule.getActivity();

        mAccountsDbAdapter.deleteAllRecords();

        Account simpleAccount = new Account(SIMPLE_ACCOUNT_NAME);
        simpleAccount.setUID(SIMPLE_ACCOUNT_UID);
        simpleAccount.setCommodity(Commodity.getInstance(ACCOUNTS_CURRENCY_CODE));
        mAccountsDbAdapter.addRecord(simpleAccount, DatabaseAdapter.UpdateMethod.insert);

        refreshAccountsList();
    }

    private void refreshAccountsList(){
        try {
            mActivityTestRule.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Fragment fragment = mAccountsActivity.getCurrentAccountListFragment();
                    ((AccountsListFragment) fragment).refresh();
                }
            });
        } catch (Throwable throwable) {
            System.err.println("Failed to refresh fragment");
        }
    }

    @Test
    public void mutante706Test() {
        ViewInteraction floatingActionButton2 = onView(
                allOf(withId(R.id.fab_create_account),
                        childAtPosition(
                                allOf(withId(R.id.coordinatorLayout),
                                        childAtPosition(
                                                withId(R.id.drawer_layout),
                                                0)),
                                2),
                        isDisplayed()));
        floatingActionButton2.perform(click());

        ViewInteraction colorSquare = onView(
                allOf(withId(R.id.input_color_picker),
                        childAtPosition(
                                allOf(withId(R.id.layout_color_type),
                                        childAtPosition(
                                                withId(R.id.fragment_account_form),
                                                3)),
                                0)));

        colorSquare.perform(scrollTo(), click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}