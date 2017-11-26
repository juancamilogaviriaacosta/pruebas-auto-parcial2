package org.gnucash.android.ui.account;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressMenuKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.gnucash.android.test.ui.AccountsActivityTest.preventFirstRunDialogs;


@LargeTest
@RunWith(AndroidJUnit4.class)
public class Mutante772Test {

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

        Account simpleAccount2 = new Account(SIMPLE_ACCOUNT_NAME + "2");
        simpleAccount2.setUID(SIMPLE_ACCOUNT_UID + "2");
        simpleAccount2.setCommodity(Commodity.getInstance(ACCOUNTS_CURRENCY_CODE));
        mAccountsDbAdapter.addRecord(simpleAccount2, DatabaseAdapter.UpdateMethod.insert);

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

    private static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void mutante772Test() {
        sleep(3000);
        onView(withText("Simple account")).check(matches(isDisplayed())).perform(click());
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        sleep(3000);
        onView(withText("Edit Account")).check(matches(isDisplayed())).perform(click());

    }
}
