package ru.myshows.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import ru.myshows.activity.MyShows;
import ru.myshows.activity.R;
import ru.myshows.activity.ShowActivity;
import ru.myshows.adapters.SectionedAdapter;
import ru.myshows.api.MyShowsApi;
import ru.myshows.domain.Episode;
import ru.myshows.domain.IShow;
import ru.myshows.domain.Searchable;
import ru.myshows.domain.UserShow;
import ru.myshows.tasks.BaseTask;
import ru.myshows.tasks.GetShowsTask;
import ru.myshows.tasks.TaskListener;
import ru.myshows.tasks.Taskable;
import ru.myshows.util.EpisodeComparator;
import ru.myshows.util.Settings;
import ru.myshows.util.ShowsComparator;
import ru.myshows.util.Utils;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: GGobozov
 * Date: 09.06.2011
 * Time: 15:19:35
 * To change this template use File | Settings | File Templates.
 */
public class ShowsFragment extends Fragment implements Taskable, Searchable, TaskListener<List<IShow>> {

    public static final int SHOWS_SEARCH = 1;
    public static final int SHOWS_TOP = 2;
    public static final int SHOWS_USER = 3;
    public static final int SHOWS_ALL = 4;


    private int action;
    private ListView list;
    private ProgressBar progress;
    private boolean isTaskExecuted = false;
    private SectionedAdapter adapter;

    public ShowsFragment() {
    }

    public ShowsFragment(int action) {
        this.action = action;
    }


    public void setAction(int action) {
        this.action = action;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MyShows", "Shows fragment: OnCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("MyShows", "Shows fragment: OnCreate View");
        View view = inflater.inflate(R.layout.shows, container, false);
        list = (ListView) view.findViewById(R.id.shows_list);
        progress = (ProgressBar) view.findViewById(R.id.progress_shows);
        return view;
    }

    @Override
    public void onTaskComplete(List<IShow> result) {
        list.setAdapter(populateAdapter(action, result));
        progress.setVisibility(View.GONE);
        progress.setIndeterminate(false);
        list.setVisibility(View.VISIBLE);
        isTaskExecuted = true;
        MyShows.isUserShowsChanged = false;
    }

    @Override
    public void onTaskFailed(Exception e) {
        if (e != null) {
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


    @Override
    public void executeTask() {
        if (isTaskExecuted) {
            adapter.notifyDataSetChanged();
            return;
        }
        GetShowsTask task = new GetShowsTask(getActivity(), GetShowsTask.SHOWS_USER);
        task.setTaskListener(this);
        task.execute();
    }

    @Override
    public void executeUpdateTask() {
        GetShowsTask task = new GetShowsTask(getActivity(), true, GetShowsTask.SHOWS_USER);
        task.setTaskListener(this);
        list.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        task.execute();
    }

    public static class ShowsAdapter extends ArrayAdapter<IShow> {

        private List<IShow> shows;
        private Context context;

        public ShowsAdapter(Context context, int textViewResourceId, List<IShow> shows) {
            super(context, textViewResourceId, shows);
            this.context = context;
            this.shows = shows;
        }

        @Override
        public int getCount() {
            if (shows == null || shows.isEmpty()) return 0;
            return shows.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int pos = position;
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.show_item, null);
                holder = new ViewHolder();
                holder.logo = (ImageView) convertView.findViewById(R.id.show_logo);
                holder.title = (TextView) convertView.findViewById(R.id.show_name);
                holder.rating = (RatingBar) convertView.findViewById(R.id.show_rating);
                holder.unwatched = (TextView) convertView.findViewById(R.id.unwatched_episodes);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            IShow show = shows.get(position);
            if (show != null) {

                ImageLoader.getInstance().displayImage(show.getImageUrl(), holder.logo, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted() {
                        holder.logo.setImageResource(R.drawable.ic_list_logo);
                        holder.logo.setScaleType(ImageView.ScaleType.CENTER);
                    }

                    @Override
                    public void onLoadingFailed(FailReason failReason) {
                    }

                    @Override
                    public void onLoadingComplete() {
                        holder.logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }

                    @Override
                    public void onLoadingCancelled() {
                    }
                });

                holder.title.setText(show.getTitle());
               // UserShow userShow = MyShows.getUserShow(show.getShowId());
                holder.rating.setRating(/*userShow != null ? userShow.getRating().floatValue() : */show.getRating().floatValue());

                if (show instanceof UserShow) {

                    if (show.getWatchStatus().equals(MyShowsApi.STATUS.watching)) {
                        int unwatched = getUnwatchedEpisodesCount(show.getShowId());
                        String value = "";
                        if (unwatched > 0)
                            value = String.valueOf(unwatched);
                        holder.unwatched.setText(value);
                    }
                }

            }


            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //IShow show = adapter.getItem(pos, section);
                    IShow show = getItem(pos);
                    Intent intent = new Intent();
                    intent.putExtra("showId", show.getShowId());
                    intent.putExtra("title", show.getTitle());
                    intent.putExtra("watchStatus", show.getWatchStatus());
                    intent.putExtra("yoursRating", show.getYoursRating());
                    intent.setClass(context, ShowActivity.class);
                    context.startActivity(intent);
                }
            });

            return convertView;
        }

        protected class ViewHolder {
            protected ImageView logo;
            protected TextView title;
            protected RatingBar rating;
            protected TextView unwatched;
        }

        public List<IShow> getShows() {
            return shows;
        }

        private int getUnwatchedEpisodesCount(Integer showId) {
            if (MyShows.newEpisodes == null) return 0;
            Map<Integer, List<Episode>> episodesByShows = new HashMap<Integer, List<Episode>>();
            int count = 0;
            for (Episode e : MyShows.newEpisodes) {
                // exclude special episodes
                if (e.getEpisodeNumber() == 0)
                    continue;
                if (e.getShowId().equals(showId))
                    count++;
            }
            return count;
        }
    }


    public SectionedAdapter populateAdapter(int action, List<IShow> shows) {
        //adapter = new SectionedAdapter(getActivity(), R.layout.header, null);

        // hack to avoid FC, sometimes getActivity is null
        // if (getActivity() == null) return adapter;

        List<SectionedAdapter.Section> sectionList = new ArrayList<SectionedAdapter.Section>();

        Resources res = getActivity().getResources();
        switch (action) {

            case SHOWS_SEARCH:
                String search = res.getString(R.string.search_results);
                sectionList.add(new SectionedAdapter.Section(search, new ShowsAdapter(getActivity(), R.layout.show_item, shows)));
                break;
            case SHOWS_TOP:
                String top = res.getString(R.string.top);
                sectionList.add(new SectionedAdapter.Section(top, new ShowsAdapter(getActivity(), R.layout.show_item, shows)));
                break;

            case SHOWS_ALL:
                String all = res.getString(R.string.all);
                sectionList.add(new SectionedAdapter.Section(all, new ShowsAdapter(getActivity(), R.layout.show_item, shows)));
                break;
            case SHOWS_USER:
                ShowsComparator sc = new ShowsComparator("title");

                if (Settings.getString(Settings.PREF_SHOW_SORT).equals("status")) {

                    String watching = res.getString(R.string.status_watching);
                    List<IShow> watchingShows = getByWatchStatus(shows, MyShowsApi.STATUS.watching);
                    Collections.sort(watchingShows, sc);
                    if (watchingShows.size() > 0)
                        sectionList.add(new SectionedAdapter.Section(watching + " (" + watchingShows.size() + ")", new ShowsAdapter(getActivity(), R.layout.show_item, watchingShows)));

                    String willWatch = res.getString(R.string.status_will_watch);
                    List<IShow> willWatchShows = getByWatchStatus(shows, MyShowsApi.STATUS.later);
                    Collections.sort(willWatchShows, sc);
                    if (willWatchShows.size() > 0)
                        sectionList.add(new SectionedAdapter.Section(willWatch + " (" + willWatchShows.size() + ")", new ShowsAdapter(getActivity(), R.layout.show_item, willWatchShows)));


                    String cancelled = res.getString(R.string.status_cancelled);
                    List<IShow> cancelledShows = getByWatchStatus(shows, MyShowsApi.STATUS.cancelled);
                    Collections.sort(cancelledShows, sc);
                    if (cancelledShows.size() > 0)
                        sectionList.add(new SectionedAdapter.Section(cancelled + " (" + cancelledShows.size() + ")", new ShowsAdapter(getActivity(), R.layout.show_item, cancelledShows)));


                    String remove = res.getString(R.string.status_finished);
                    List<IShow> finishedShows = getByWatchStatus(shows, MyShowsApi.STATUS.finished);
                    Collections.sort(finishedShows, sc);
                    if (finishedShows.size() > 0)
                        sectionList.add(new SectionedAdapter.Section(remove + " (" + finishedShows.size() + ")", new ShowsAdapter(getActivity(), R.layout.show_item, finishedShows)));
                    break;
                } else {
                    Collections.sort(shows, sc);
                    sectionList.add(new SectionedAdapter.Section(res.getString(R.string.tab_shows_title) + " (" + shows.size() + ")", new ShowsAdapter(getActivity(), R.layout.show_item, shows)));
                }


        }
        adapter = new SectionedAdapter(getActivity(), R.layout.header, sectionList);
        adapter.notifyDataSetChanged();
        return adapter;
    }


    public static List<IShow> getByWatchStatus(List<IShow> shows, MyShowsApi.STATUS status) {
        List<IShow> list = new ArrayList<IShow>();
        if (shows == null) return list;
        for (IShow show : shows) {
            if (show.getWatchStatus().equals(status)) {
                list.add(show);
            }
        }
        return list;

    }


    @Override
    public ArrayAdapter getAdapter() {
        return adapter;
    }


}
