package ru.myshows.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import ru.myshows.activity.MyShows;
import ru.myshows.activity.R;
import ru.myshows.adapters.SectionedAdapter;
import ru.myshows.components.RatingDialog;
import ru.myshows.domain.Episode;
import ru.myshows.domain.Season;
import ru.myshows.domain.UserShow;
import ru.myshows.tasks.BaseTask;
import ru.myshows.tasks.GetNewEpisodesTask;
import ru.myshows.tasks.TaskListener;
import ru.myshows.tasks.Taskable;
import ru.myshows.util.EpisodeComparator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: gb
 * Date: 06.10.11
 * Time: 1:10
 * To change this template use File | Settings | File Templates.
 */
public class NewEpisodesFragment extends SherlockFragment implements TaskListener<List<Episode>>, Taskable {

    private MyExpandableListAdapter adapter;
    private RelativeLayout rootView;
    private ExpandableListView list;
    private ProgressBar progress;
    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    private LayoutInflater inflater;
    private boolean isTaskExecuted = false;
    private com.actionbarsherlock.view.ActionMode mMode;


    public NewEpisodesFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        rootView = (RelativeLayout) inflater.inflate(R.layout.new_episodes, container, false);
        progress = (ProgressBar) rootView.findViewById(R.id.progress_new_episodes);
        list = (ExpandableListView) rootView.findViewById(R.id.new_episodes_list);
        return rootView;
    }


    @Override
    public void executeTask() {
        if (isTaskExecuted)
            return;
        GetNewEpisodesTask episodesTask = new GetNewEpisodesTask(getActivity());
        episodesTask.setTaskListener(this);
        episodesTask.execute();
    }

    @Override
    public void executeUpdateTask() {
        GetNewEpisodesTask episodesTask = new GetNewEpisodesTask(getActivity(), true);
        episodesTask.setTaskListener(this);
        list.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        episodesTask.execute();
    }

    @Override
    public void onTaskComplete(List<Episode> result) {
        adapter = new MyExpandableListAdapter(result);
        list.setAdapter(adapter);
        progress.setVisibility(View.GONE);
        progress.setIndeterminate(false);
        list.setVisibility(View.VISIBLE);
        isTaskExecuted = true;
    }

    @Override
    public void onTaskFailed(Exception e) {
        if (e != null){
            progress.setVisibility(View.GONE);
        }
        final AlertDialog alert;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.something_wrong)
                .setMessage(R.string.try_again)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executeUpdateTask();
                    }
                })
                .setNegativeButton(R.string.no, null);
        alert = builder.create();
        alert.show();
    }



    public class MyExpandableListAdapter extends BaseExpandableListAdapter {

        private ArrayList<UserShow> shows = new ArrayList<UserShow>();
        private ArrayList<List<Episode>> children = new ArrayList<List<Episode>>();


        public MyExpandableListAdapter(Collection<Episode> eps) {

            if (eps == null || eps.isEmpty())
                return;


            Map<Integer, List<Episode>> episodesByShows = new HashMap<Integer, List<Episode>>();

            for (Episode e : eps) {
                // exclude specials episodes
                if (e.getEpisodeNumber() == 0)
                    continue;

                List<Episode> temp = episodesByShows.get(e.getShowId());
                if (temp == null) {
                    temp = new ArrayList<Episode>();
                    episodesByShows.put(e.getShowId(), temp);

                }
                temp.add(e);
            }

            for (Map.Entry<Integer, List<Episode>> entry : episodesByShows.entrySet()) {
                Integer showId = entry.getKey();
                UserShow show = MyShows.getUserShow(showId);
                List<Episode> episodes = entry.getValue();
                Collections.sort(episodes, new EpisodeComparator("shortName"));
                //TODO FIXME
                shows.add(show != null ? show : null);
                children.add(episodes);
                //adapter.addSection(title + "*", new EpisodesAdapter(getActivity(), R.layout.episode, episodes, title));
            }

        }

        public Object getAllChildrenAsList() {
            ArrayList<Episode> episodes = new ArrayList<Episode>();
            for (Iterator<List<Episode>> i = children.iterator(); i.hasNext(); ) {
                List<Episode> list = i.next();
                episodes.addAll(list);
            }
            return episodes;
        }


        public Object getChild(int groupPosition, int childPosition) {
            return children.get(groupPosition).get(childPosition);
        }

        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        public int getChildrenCount(int groupPosition) {
            return children.get(groupPosition).size();
        }

        public List getGroupChildren(int groupPosition) {
            return children.get(groupPosition);
        }


        public void checkAll() {
            for (UserShow s : shows) {
                s.setChecked(true);
            }
            for (Episode e : (List<Episode>) getAllChildrenAsList()) {
                e.setChecked(true);
            }
            adapter.notifyDataSetChanged();
        }

        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            final int gp = groupPosition;
            final ViewHolder holder;
            final UserShow userShow = (UserShow) getGroup(groupPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.season, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.season_title);
                holder.unwatched = (TextView) convertView.findViewById(R.id.unwatched);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.season_check_box);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.title.setText(userShow.getTitle());
            holder.unwatched.setVisibility(View.VISIBLE);
            holder.unwatched.setText(getActivity().getResources().getString(R.string.unwatched) + ": " + getChildrenCount(groupPosition));
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    userShow.setChecked(isChecked);
                }
            });
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox) v;
                    boolean isChecked = checkBox.isChecked();
                    for (Episode e : (List<Episode>) getGroupChildren(gp)) {
                        e.setChecked(isChecked);
                    }
                    SherlockFragmentActivity activity = (SherlockFragmentActivity) getActivity();
                    if (mMode == null)
                        mMode = activity.startActionMode(new CheckNewEpisodesActionMode());
                    adapter.notifyDataSetChanged();
                }
            });
            holder.checkBox.setChecked(userShow.isChecked());
            return convertView;
        }

        protected class ViewHolder {
            protected TextView title;
            protected TextView unwatched;
            protected CheckBox checkBox;
            protected TextView shortTitle;
            private TextView airDate;
        }

        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final Episode episode = (Episode) getChild(groupPosition, childPosition);
            final int gp = groupPosition;
            final ViewHolder holder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.episode, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.episode_title);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.episode_check_box);
                holder.shortTitle = (TextView) convertView.findViewById(R.id.episode_short_title);
                holder.airDate = (TextView) convertView.findViewById(R.id.episode_air_date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(episode.getTitle());
            holder.shortTitle.setText(episode.getShortName() != null ? episode.getShortName() : "");
            holder.airDate.setText(episode.getAirDate() != null ? df.format(episode.getAirDate()) : "unknown");

            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    UserShow userShow = (UserShow) getGroup(gp);
                    episode.setChecked(isChecked);
                    if (!isChecked && userShow.isChecked()) {
                        userShow.setChecked(isChecked);
                        adapter.notifyDataSetChanged();
                    }

                    boolean isAllEpisodesChecked = true;
                    for (Episode e : (List<Episode>) getGroupChildren(gp)) {
                        if (!e.isChecked()) {
                            isAllEpisodesChecked = false;
                            break;
                        }
                    }
                    if (isAllEpisodesChecked) {
                        userShow.setChecked(true);
                        notifyDataSetChanged();
                    }


                }
            });

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SherlockFragmentActivity activity = (SherlockFragmentActivity) getActivity();
                    if (mMode == null)
                        mMode = activity.startActionMode(new CheckNewEpisodesActionMode());
                }
            });


            holder.checkBox.setChecked(episode.isChecked());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                }
            });

            convertView.setOnCreateContextMenuListener(null);

            return convertView;
        }

        public Object getGroup(int groupPosition) {
            return shows.get(groupPosition);
        }

        public int getGroupCount() {
            return shows.size();
        }

        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }


    }



    private class CheckNewEpisodesTask extends BaseTask<Boolean> {

        @Override
        public Boolean doWork(Object... objects) throws Exception {
            Map<Integer, String> paramsMap = new HashMap<Integer, String>();

            for (int i = 0; i < adapter.getGroupCount(); i++) {

                List<Episode> episodes = adapter.getGroupChildren(i);
                for (Episode e : episodes) {
                    if (e.isChecked()) {
                        String value = paramsMap.get(e.getShowId());
                        if (value == null)
                            paramsMap.put(e.getShowId(), e.getEpisodeId().toString());
                        else
                            paramsMap.put(e.getShowId(), value += "," + e.getEpisodeId().toString());
                        MyShows.newEpisodes.remove(e);

                    }
                }
            }

            for (Map.Entry<Integer, String> entry : paramsMap.entrySet()) {
                final Integer showId = entry.getKey();
                final String episodesIds = entry.getValue();
                new Thread() {
                    public void run() {
                        UserShow userShow = MyShows.getUserShow(showId);
                        userShow.setWatchedEpisodes(userShow.getWatchedEpisodes() + episodesIds.split(",").length);
                        //app.setUserShowsChanged(true);
                        MyShows.client.syncAllShowEpisodes(showId, episodesIds, null);
                    }
                }.start();
            }
            return true;
        }

        @Override
        public void onResult(Boolean result) {
            Toast.makeText(getActivity(), exception == null ? R.string.changes_saved : R.string.changes_not_saved, Toast.LENGTH_SHORT).show();
            if (result)
                executeUpdateTask();
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }


    private final class CheckNewEpisodesActionMode implements com.actionbarsherlock.view.ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(com.actionbarsherlock.view.ActionMode mode, com.actionbarsherlock.view.Menu menu) {
            //Used to put dark icons on light action bar
            menu.add(0, 1, 1, "Save").setIcon(R.drawable.ic_save).setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
            menu.add(0, 2, 2, R.string.episode_rating).setIcon(R.drawable.ic_rating_important).setShowAsAction(com.actionbarsherlock.view.MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(com.actionbarsherlock.view.ActionMode mode, com.actionbarsherlock.view.Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(com.actionbarsherlock.view.ActionMode mode, com.actionbarsherlock.view.MenuItem item) {
            switch (item.getItemId()) {
                case 1:
                    new CheckNewEpisodesTask().execute();
                    break;
                case 2:

                    Handler handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            int rating = msg.arg1;
                            new ChangeEpisodesRateTask().execute(rating);
                        }
                    };
                    RatingDialog rate = new RatingDialog(getActivity(), handler);
                    rate.setTitle(R.string.episode_rating);
                    rate.show();

            }
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(com.actionbarsherlock.view.ActionMode mode) {
            mMode = null;
        }
    }



    public class ChangeEpisodesRateTask extends BaseTask<Boolean> {
        ArrayList<Episode> episodes = (ArrayList<Episode>) adapter.getAllChildrenAsList();

        @Override
        public Boolean doWork(Object... objects) throws Exception {

            Integer ratio = (Integer) objects[0];

            StringBuilder checkedIds = new StringBuilder();
            for (Episode e : episodes)
                if (e.isChecked())
                    checkedIds.append(e.getEpisodeId() + ",");

            String checked = checkedIds.toString();
            if (checked.endsWith(",")) checked = checked.substring(0, checked.length() - 1);
            return MyShows.client.changeEpisodesRatio(ratio, checked);
        }

        @Override
        public void onResult(Boolean result) {
            Toast.makeText(getActivity(), result ? R.string.changes_saved : R.string.changes_not_saved, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(Exception e) {
            e.printStackTrace();
        }
    }




}