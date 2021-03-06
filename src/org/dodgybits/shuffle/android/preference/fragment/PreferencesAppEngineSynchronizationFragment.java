package org.dodgybits.shuffle.android.preference.fragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.inject.Inject;
import org.dodgybits.android.shuffle.R;
import org.dodgybits.shuffle.android.preference.activity.PreferencesAppEngineSynchronizationActivity;
import org.dodgybits.shuffle.android.preference.model.Preferences;
import org.dodgybits.shuffle.android.server.sync.GaeSyncService;
import org.dodgybits.shuffle.android.server.sync.ObtainAuthTokenTask;
import org.dodgybits.shuffle.android.server.sync.event.ResetSyncSettingsEvent;
import org.dodgybits.shuffle.android.server.sync.listener.SyncListener;
import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class PreferencesAppEngineSynchronizationFragment extends RoboFragment {
    private static final String TAG = "PrefAppEngSyncAct";

    public static final String GOOGLE_ACCOUNT = "com.google";

    @InjectView(R.id.intro_message)
    private TextView mIntroTextView;

    @InjectView(R.id.select_account)
    private Button mSelectAccountButton;

    @InjectView(R.id.logged_in_message)
    private TextView mLoggedInTextView;

    @InjectView(R.id.logout)
    private Button mLogoutButton;

    @InjectView(R.id.sync_now)
    private Button mSyncNowButton;

    @InjectView(R.id.last_sync_message)
    private TextView mLastSyncTextView;

    @Inject
    private EventManager mEventManager;

    @Inject
    private SyncListener mSyncListener;

    private Account mSelectedAccount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView+");
        return inflater.inflate(R.layout.preferences_appengine_sync, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupScreen();
    }

    public void onSelectAccountClicked(View view) {
        getActivity().showDialog(PreferencesAppEngineSynchronizationActivity.ACCOUNTS_DIALOG);
    }

    public void onLogoutClicked(View view) {
        Preferences.getEditor(getActivity())
                .putBoolean(Preferences.SYNC_ENABLED, false)
                .commit();
        updateViewsOnSyncAccountSet();
    }

    public void onSyncNowClicked(View view) {
        if (Preferences.getSyncAuthToken(getActivity()) != null) {
            Intent intent = new Intent(getActivity(), GaeSyncService.class);
            getActivity().startService(intent);
        } else {
            // token was invalidated - fetch a new one
            String accountName = Preferences.getSyncAccount(getActivity());
            Account account = null;
            AccountManager manager = AccountManager.get(getActivity());
            final Account[] accounts = manager.getAccountsByType(GOOGLE_ACCOUNT);
            final int numAccounts = accounts.length;
            for (int i=0; i < numAccounts; i++)
            {
                final String name = accounts[i].name;
                if (name.equals(accountName)) {
                    account = accounts[i];
                    break;
                }
            }

            new ObtainAuthTokenTask(getActivity(), account).execute();
        }
    }

        public Dialog createAccountsDialog() {
        AccountManager manager = AccountManager.get(getActivity());
        final Account[] accounts = manager.getAccountsByType(GOOGLE_ACCOUNT);

        final int numAccounts = accounts.length;

        if (numAccounts == 0) {
            return createNoAccountsDialog();
        }

        final CharSequence[] items = new CharSequence[numAccounts];

        String accountName = Preferences.getSyncAccount(getActivity());

        int selectedIndex = -1;
        mSelectedAccount = null;
        for (int i=0; i < numAccounts; i++)
        {
            final String name = accounts[i].name;
            if (name.equals(accountName)) {
                selectedIndex = i;
                mSelectedAccount = accounts[i];
            }
            items[i] = name;
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity())
            .setTitle(R.string.select_account_button_title)
            .setSingleChoiceItems(items, selectedIndex,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    mSelectedAccount = accounts[item];
                }
            })
            .setPositiveButton(R.string.ok_button_title,
                    new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mSelectedAccount != null) {
                        final Account account = mSelectedAccount;
                        String oldAccountName = Preferences.getSyncAccount(getActivity());
                        SharedPreferences.Editor editor = Preferences.getEditor(getActivity());
                        editor.putBoolean(Preferences.SYNC_ENABLED, true);
                        if (!oldAccountName.equals(account.name)) {
                            Log.i(TAG, "Switching from account " + oldAccountName +
                                    " to " + account.name);
                            editor.putString(Preferences.SYNC_ACCOUNT, account.name);
                            mEventManager.fire(new ResetSyncSettingsEvent());
                        }
                        editor.commit();
                        new ObtainAuthTokenTask(getActivity(), account).execute();
                        updateViewsOnSyncAccountSet();
                    }
                }
            });

        AlertDialog alert = builder.create();
        return alert;
    }

    private Dialog createNoAccountsDialog() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity())
                        .setMessage(R.string.no_sync_accounts)
                        .setNegativeButton(R.string.cancel_button_title,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                        .setPositiveButton(R.string.ok_button_title,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
                                        getActivity().startActivity(intent);
                                    }
                                });
        return builder.create();
    }


    private void setupScreen() {
        updateViewsOnSyncAccountSet();
    }

    private void updateViewsOnSyncAccountSet() {
        String syncAccount = Preferences.getSyncAccount(getActivity());
        final boolean syncEnabled = Preferences.isSyncEnabled(getActivity());
        mIntroTextView.setVisibility(syncEnabled ? View.GONE : View.VISIBLE);
        mSelectAccountButton.setVisibility(syncEnabled ? View.GONE : View.VISIBLE);
        mLoggedInTextView.setVisibility(syncEnabled ? View.VISIBLE : View.GONE);
        mLoggedInTextView.setText(getString(R.string.sync_selected_account, syncAccount));
        mLogoutButton.setVisibility(syncEnabled ? View.VISIBLE : View.GONE);
        mSyncNowButton.setVisibility(syncEnabled ? View.VISIBLE : View.GONE);
        mLastSyncTextView.setVisibility(syncEnabled ? View.VISIBLE : View.GONE);
        long lastSyncDate = Preferences.getLastSyncLocalDate(getActivity());
        if (lastSyncDate == 0L) {
            mLastSyncTextView.setText(R.string.no_previous_sync);
        } else {
            CharSequence syncDate = DateUtils.getRelativeTimeSpanString(getActivity(), lastSyncDate, false);
            mLastSyncTextView.setText(getString(R.string.last_sync_title, syncDate));
        }

    }

}
