/*
 * Copyright 2017 Nimrod Dayan CodePond.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codepond.commitbrowser.commitlist;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModel;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;

import org.codepond.commitbrowser.api.GithubApi;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class CommitListViewModel extends ViewModel implements LifecycleObserver {
    private GithubApi githubApi;
    private ObservableList<CommitItemViewModel> commits = new ObservableArrayList<>();
    private int page = 1;
    private Subscription subscription;

    public CommitListViewModel(GithubApi githubApi) {
        this.githubApi = githubApi;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void loadCommits() {
        if (commits.size() == 0) {
            Timber.v("Request commit list");
            subscription = githubApi.getCommits(page)
                    .flatMap(Observable::from)
                    .map(commitResponse -> new CommitItemViewModel(
                            commitResponse.commit().message(),
                            commitResponse.commit().author().date(),
                            commitResponse.commit().author().name()))
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(commitList -> {
                        Timber.v("Received commit list. Size: %d", commitList.size());
                        commits.addAll(commitList);
                    });
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void clean() {
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    public ObservableList<CommitItemViewModel> getCommits() {
        return commits;
    }
}
