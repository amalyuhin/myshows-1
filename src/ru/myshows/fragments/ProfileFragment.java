package ru.myshows.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import ru.myshows.activity.MainActivity;
import ru.myshows.activity.MyShows;
import ru.myshows.activity.R;
import ru.myshows.api.MyShowsApi;
import ru.myshows.components.TextProgressBar;
import ru.myshows.domain.*;
import ru.myshows.tasks.BaseTask;
import ru.myshows.tasks.GetProfileTask;
import ru.myshows.tasks.TaskListener;
import ru.myshows.tasks.Taskable;
import ru.myshows.util.Settings;
import ru.myshows.util.Utils;

import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: GGobozov
 * Date: 09.06.2011
 * Time: 15:19:22
 * To change this template use File | Settings | File Templates.
 */
public class ProfileFragment extends Fragment implements TaskListener<Profile>, Taskable {


    private Button logoutButton;
    private ImageView avatar;
    private TextProgressBar episodesBar;
    private TextProgressBar hoursBar;
    private TextProgressBar daysBar;
    private TextView nickName;
    private View mainView;
    private ProgressBar progress;
    private ScrollView scrollView;
    private boolean isTaskExecuted = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainView = inflater.inflate(R.layout.profile, container, false);
        progress = (ProgressBar) mainView.findViewById(R.id.progress_profile);
        scrollView = (ScrollView) mainView.findViewById(R.id.scroll_layout);
        return mainView;
    }


    @Override
    public void onTaskComplete(Profile result) {
        populateUI(result, result.getStats());
        progress.setIndeterminate(false);
        progress.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
        isTaskExecuted = true;
    }

    @Override
    public void onTaskFailed(Exception e) {
        //To change body of implemented methods use File | Settings | File Templates.
    }



    @Override
    public void executeTask() {
        if (isTaskExecuted)
            return;
        GetProfileTask profileTask = new GetProfileTask(getActivity());
        profileTask.setTaskListener(this);
        profileTask.execute(Settings.getString(Settings.KEY_LOGIN));
    }

    @Override
    public void executeUpdateTask() {
        GetProfileTask profileTask = new GetProfileTask(getActivity(), true);
        profileTask.setTaskListener(this);
        scrollView.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        profileTask.execute(Settings.getString(Settings.KEY_LOGIN));
    }

    private void populateUI(Profile profile, ProfileStats stats) {


        avatar = (ImageView) mainView.findViewById(R.id.avatar);
        if (profile.getAvatarUrl() != null) {
            ImageLoader.getInstance().displayImage(profile.getAvatarUrl(), avatar);
        }

        nickName = (TextView) mainView.findViewById(R.id.profile_name);
        nickName.setText(profile.getLogin());


        episodesBar = (TextProgressBar) mainView.findViewById(R.id.episodes_bar);
        episodesBar.setMax(stats.getWatchedEpisodes() + stats.getRemainingEpisodes());
        episodesBar.setProgress(stats.getWatchedEpisodes());
        episodesBar.setText(stats.getWatchedEpisodes() + "/" + (stats.getWatchedEpisodes() + stats.getRemainingEpisodes()));


        hoursBar = (TextProgressBar) mainView.findViewById(R.id.hours_bar);
        hoursBar.setMax((int) stats.getWatchedHours().doubleValue() + (int) stats.getRemainingHours().doubleValue());
        hoursBar.setProgress((int) stats.getWatchedHours().doubleValue());
        hoursBar.setText((int) stats.getWatchedHours().doubleValue() + "/" + (int) (stats.getWatchedHours() + stats.getRemainingHours()));

        daysBar = (TextProgressBar) mainView.findViewById(R.id.days_bar);
        daysBar.setMax(stats.getWatchedDays() + stats.getRemainingDays());
        daysBar.setProgress(stats.getWatchedDays());
        daysBar.setText(stats.getWatchedDays() + "/" + (stats.getWatchedDays() + stats.getRemainingDays()));

        Log.d("MyShows", "Current login = " + profile.getLogin());
        Log.d("MyShows", "Current user = " + Settings.getString(Settings.KEY_LOGIN));

        if (profile.getLogin().equals(Settings.getString(Settings.KEY_LOGIN))) {
            List<UserShow> shows = MyShows.userShows;

            if (shows != null) {
                mainView.findViewById(R.id.profile_shows_info).setVisibility(View.VISIBLE);
                TextView label = (TextView) mainView.findViewById(R.id.profile_watching_label);
                label.setText
                        (getResources().getString(R.string.status_watching) + " " +
                                Utils.getUserShowsByWatchStatus(shows, MyShowsApi.STATUS.watching).size());

                ((TextView) mainView.findViewById(R.id.profile_will_watch_label)).setText
                        (getResources().getString(R.string.status_will_watch) + " " +
                                Utils.getUserShowsByWatchStatus(shows, MyShowsApi.STATUS.later).size());


                ((TextView) mainView.findViewById(R.id.profile_cancelled_label)).setText
                        (getResources().getString(R.string.status_cancelled) + " " +
                                Utils.getUserShowsByWatchStatus(shows, MyShowsApi.STATUS.cancelled).size());

                ((TextView) mainView.findViewById(R.id.profile_finished_label)).setText(
                        getResources().getString(R.string.status_finished) + " " +
                                Utils.getUserShowsByWatchStatus(shows, MyShowsApi.STATUS.finished).size());
            }

        } else {
            mainView.findViewById(R.id.profile_shows_info).setVisibility(View.INVISIBLE);
        }
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//        String login = getBundleValue(intent, "login", Settings.getString(Settings.KEY_LOGIN));
//        if (currentUser != null && !currentUser.equals(login)) {
//            currentUser = login;
//            new GetProfileTask(this).execute(login);
//        }
//    }


}
