package org.dodgybits.shuffle.android.editor.activity;

import com.google.inject.Inject;
import org.dodgybits.shuffle.android.editor.fragment.AbstractEditFragment;
import org.dodgybits.shuffle.android.editor.fragment.EditContextFragment;

public class EditContextActivity extends AbstractEditActivity {
    private static final String TAG = "EditContextActivity";

    @Inject
    private EditContextFragment mEditFragment;

    @Override
    protected AbstractEditFragment getFragment() {
        return mEditFragment;
    }

    @Override
    protected void setFragment(AbstractEditFragment fragment) {
        mEditFragment = (EditContextFragment) fragment;
    }
}
