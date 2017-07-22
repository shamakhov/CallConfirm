/*
 * Copyright 2017 75py
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nagopy.android.callconfirm.viewmodel;

import android.databinding.ObservableField;
import android.view.View;

import com.nagopy.android.callconfirm.di.ActivityScope;
import com.nagopy.android.callconfirm.helper.HookState;
import com.nagopy.android.callconfirm.view.helper.ContactsHelper;
import com.nagopy.android.callconfirm.view.helper.Navigator;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@ActivityScope
public class ConfirmViewModel {

    @Inject
    HookState hookState;

    @Inject
    Navigator navigator;

    @Inject
    ContactsHelper contactsHelper;

    public final ObservableField<String> phoneNumber = new ObservableField<>();

    public final ObservableField<String> name = new ObservableField<>();

    public final ObservableField<String> imageUri = new ObservableField<>();

    @SuppressWarnings("WeakerAccess")
    OnFinishListener onFinishListener;

    @SuppressWarnings("WeakerAccess")
    OnCancelListener onCancelListener;

    @SuppressWarnings("WeakerAccess")
    OnLongClickListener onLongClickListener;

    @Inject
    ConfirmViewModel() {
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public void setOnCancelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new RuntimeException("phoneNumber is empty");
        }

        if (!phoneNumber.matches("[\\d-]+")) {
            throw new RuntimeException("phoneNumber contains illegal chars");
        }

        this.phoneNumber.set(phoneNumber);
    }

    public void findContactData() {
        contactsHelper.findBy(phoneNumber.get())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    Timber.d("dispName=%s, thumbnailUri=%s", s.dispName, s.thumbnailUri);

                    if (s.dispName != null && !s.dispName.isEmpty()) {
                        name.set(s.dispName);
                    }

                    if (s.thumbnailUri != null && !s.thumbnailUri.isEmpty()) {
                        imageUri.set(s.thumbnailUri);
                    }
                }, Timber::e);
    }

    public void destroy() {
        onFinishListener = null;
        onCancelListener = null;
    }

    public final View.OnClickListener onClickCall = v -> {
        Timber.d("Click Call");
        hookState.setHookEnabled(false);
        navigator.call(phoneNumber.get());

        if (onFinishListener != null) {
            onFinishListener.onFinish();
        }
    };

    public final View.OnClickListener onClickCancel = v -> {
        Timber.d("Click Cancel");
        if (onCancelListener != null) {
            onCancelListener.onCancel();
        }
    };

    public final View.OnLongClickListener onLongClick = v -> {
        Timber.d("Show tooltip");
        if (onLongClickListener != null) {
            onLongClickListener.onLongClick(v);
        }
        return false;
    };

    public interface OnFinishListener {
        void onFinish();
    }

    public interface OnCancelListener {
        void onCancel();
    }

    public interface OnLongClickListener {
        void onLongClick(View view);
    }
}
