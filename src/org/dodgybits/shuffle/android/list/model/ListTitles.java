package org.dodgybits.shuffle.android.list.model;

import org.dodgybits.android.shuffle.R;

import java.util.HashMap;

public class ListTitles {

    private static final HashMap<ListQuery,Integer> TITLE_ID_MAP =
            new HashMap<ListQuery,Integer>();

    static {
        TITLE_ID_MAP.put(ListQuery.all, R.string.title_all);
        TITLE_ID_MAP.put(ListQuery.context, R.string.title_context_tasks);
        TITLE_ID_MAP.put(ListQuery.project, R.string.title_project_tasks);
        TITLE_ID_MAP.put(ListQuery.inbox, R.string.title_inbox);
        TITLE_ID_MAP.put(ListQuery.custom, R.string.title_custom);
        TITLE_ID_MAP.put(ListQuery.nextTasks, R.string.title_next_tasks);
        TITLE_ID_MAP.put(ListQuery.tickler, R.string.title_tickler);
        TITLE_ID_MAP.put(ListQuery.dueToday, R.string.title_due_today);
        TITLE_ID_MAP.put(ListQuery.dueNextWeek, R.string.title_due_next_week);
        TITLE_ID_MAP.put(ListQuery.dueNextMonth, R.string.title_due_next_month);
    }

    public static int getTitleId(ListQuery query) {
        return TITLE_ID_MAP.get(query);
    }
}
