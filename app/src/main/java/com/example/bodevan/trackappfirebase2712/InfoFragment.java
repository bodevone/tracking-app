package com.example.bodevan.trackappfirebase2712;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import mehdi.sakout.aboutpage.AboutPage;

public class InfoFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);

        View aboutPage = new AboutPage(getActivity())
                .isRTL(false)
                .setImage(R.drawable.logo)
                .setDescription("Это приложения для слежения за водителем в реальном времени." +
                        "\nОно было разработано специально для Grand Alem (c)")
                .addGroup("Связаться с нами")
                .addEmail("info@foodsmag.kz", "Напишите нам в email")
                .addWebsite("http://foodsmag.kz/", "Посетите наш website")
                .create();

        return aboutPage;
    }

}
