package org.dodgybits.shuffle.android.list.view.task;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import org.dodgybits.android.shuffle.R;
import org.dodgybits.shuffle.android.core.model.Id;
import org.dodgybits.shuffle.android.core.model.Project;
import org.dodgybits.shuffle.android.core.model.persistence.EntityCache;
import org.dodgybits.shuffle.android.core.model.persistence.selector.TaskSelector;
import org.dodgybits.shuffle.android.list.event.*;
import org.dodgybits.shuffle.android.list.model.ListQuery;
import org.dodgybits.shuffle.android.list.model.ListSettingsCache;
import org.dodgybits.shuffle.android.list.model.ListTitles;
import org.dodgybits.shuffle.android.preference.model.ListSettings;

public class TaskListContext implements Parcelable {
    public static final String LIST_NAME = "listName";
    public static final String CONTEXT_IDS = "contextIds";
    public static final String PROJECT_IDS = "projectIds";

    protected TaskSelector mSelector;
    protected int mTitleId;

    public static final Parcelable.Creator<TaskListContext> CREATOR
            = new Parcelable.Creator<TaskListContext>() {

        @Override
        public TaskListContext createFromParcel(Parcel source) {
            TaskSelector selector = source.readParcelable(TaskSelector.class.getClassLoader());
            return create(selector);
        }

        @Override
        public TaskListContext[] newArray(int size) {
            return new TaskListContext[size];
        }
    };

    public static final TaskListContext createForContext(Id contextId) {
        TaskSelector selector = TaskSelector.newBuilder().setListQuery(ListQuery.context).
                setContextId(contextId).build();
        return create(selector);
    }

    public static final TaskListContext createForProject(Id projectId) {
        TaskSelector selector = TaskSelector.newBuilder().setListQuery(ListQuery.project).
                setProjectId(projectId).
                build();
        return create(selector);
    }

    public static final TaskListContext create(ListQuery query) {
        TaskSelector selector = TaskSelector.newBuilder().setListQuery(query).build();
        return create(selector);
    }

    public static final TaskListContext create(ListQuery query, Id contextId, Id projectId) {
        TaskListContext listContext;
        if (contextId.isInitialised()) {
            listContext = TaskListContext.createForContext(contextId);
        } else {
            if (projectId.isInitialised()) {
                listContext = TaskListContext.createForProject(projectId);
            } else {
                listContext = TaskListContext.create(query);
            }
        }
        return listContext;
    }

    private static final TaskListContext create(TaskSelector selector) {
        ListQuery query = selector.getListQuery();
        return new TaskListContext(selector, ListTitles.getTitleId(query));
    }

    protected TaskListContext(TaskSelector selector, int titleId) {
        mSelector = selector;
        mTitleId = titleId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mSelector, 0);
    }

    public ListQuery getListQuery() {
        return mSelector.getListQuery();
    }

    public TaskSelector createSelectorWithPreferences(Context context) {
        ListSettings settings = ListSettingsCache.findSettings(mSelector.getListQuery());
        return mSelector.builderFrom().applyListPreferences(context, settings).build();
    }
    
    public String createTitle(Context androidContext,
                              EntityCache<org.dodgybits.shuffle.android.core.model.Context> contextCache, 
                              EntityCache<Project> projectCache) {
        String title;
        String name;
        if (mSelector.getContextId().isInitialised()) {
            // it's possible the context no longer exists at this point
            org.dodgybits.shuffle.android.core.model.Context context = contextCache.findById(mSelector.getContextId());
            name = context == null ? "?" : context.getName();
            title = androidContext.getString(mTitleId, name);
        } else if (mSelector.getProjectId().isInitialised()) {
            // it's possible the project no longer exists at this point
            Project project = projectCache.findById(mSelector.getProjectId());
            name = project == null ? "?" : project.getName();
            title = androidContext.getString(mTitleId, name);
        } else {
            title = androidContext.getString(mTitleId);
        }
        
        return title;
    }

    public EditNewTaskEvent createEditNewTaskEvent() {
        return new EditNewTaskEvent(mSelector.getContextId(), mSelector.getProjectId());
    }

    public NewTaskEvent createNewTaskEventWithDescription(String description) {
        return new NewTaskEvent(description, mSelector.getContextId(), mSelector.getProjectId());
    }

    public boolean isQuickAddEnabled(Context context) {
        ListSettings settings = ListSettingsCache.findSettings(mSelector.getListQuery());
        return settings.getQuickAdd(context);
    }
    
    @Override
    public String toString() {
        return "[TaskListContext " + mSelector.getListQuery() + "]";
    }

    public boolean showMoveActions() {
        return getListQuery() == ListQuery.project;
    }

    public boolean showEditActions() {
        return getListQuery() == ListQuery.project || getListQuery() == ListQuery.context;
    }
    
    public String getEditEntityName(Context context) {
        String name;
        switch (getListQuery()) {
            case context:
                name = context.getString(R.string.context_name);
                break;

            case project:
                name = context.getString(R.string.project_name);
                break;

            default:
                throw new UnsupportedOperationException("Cannot create edit event for listContext " + this);
        }

        return name;
    }
    
    public boolean isEditEntityDeleted(Context androidContext,
                                       EntityCache<org.dodgybits.shuffle.android.core.model.Context> contextCache,
                                       EntityCache<Project> projectCache) {
        boolean isDeleted;
        switch (getListQuery()) {
            case context:
                org.dodgybits.shuffle.android.core.model.Context context = contextCache.findById(mSelector.getContextId());
                isDeleted = context.isDeleted();
                break;

            case project:
                Project project = projectCache.findById(mSelector.getProjectId());
                isDeleted = project.isDeleted();
                break;

            default:
                throw new UnsupportedOperationException("Cannot create edit event for listContext " + this);
        }

        return isDeleted;
    }
    
    public Object createEditEvent() {
        Object event;
        switch (getListQuery()) {
            case context:
                event = new EditContextEvent(mSelector.getContextId());
                break;

            case project:
                event = new EditProjectEvent(mSelector.getProjectId());
                break;

            default:
                throw new UnsupportedOperationException("Cannot create edit event for listContext " + this);
        }
        return event;
    }

    public Object createDeleteEvent(boolean delete) {
        Object event;
        switch (getListQuery()) {
            case context:
                event = new UpdateContextDeletedEvent(mSelector.getContextId(), delete);
                break;

            case project:
                event = new UpdateProjectDeletedEvent(mSelector.getProjectId(), delete);
                break;

            default:
                throw new UnsupportedOperationException("Cannot create delete event for listContext " + this);
        }
        return event;
    }
}
