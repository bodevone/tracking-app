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
                .setImage(R.drawable.logogo)
                .setDescription("Компания «Гранд Алем» предоставляет услуги аренды автотранспорта и оказывает широкий спектр сопутствующих услуг на территории городов Астана, Алматы, Шымкент, Актау, Караганды, а также их областей.."+
                "\nМы готовы предоставить в аренду автомобили класса: эконом, бизнес, премиум (представительский), внедорожник, минивэн, микроавтобус и автобус."+
                "\nСопутствовать Вам будут услуги: поездка за город, трансфер (встреча и провод в аэропорту, на вокзале), квалифицированный гид и экскурсовод проведет увлекательную экскурсию в городе и по местным достопримечательностям, переводчик иностранного языка поможет преодолеть языковой барьер.Предприятиям и компаниям рады помочь в решении вопроса долгосрочной аренды автотранспорта, развозки и доставки персонала до места работы и обратно. Также мы имеем возможность предоставить в аренду свадебный кортеж и лимузины."+
                "\nПриемлемая цена, индивидуальный подход, ответственно и в срок.")
                .addGroup("Связаться с нами")
                .addEmail("astana@rentavto.kz", "Напишите нам в email")
                .addWebsite("https://rentavto.kz/ru/?is_new=1", "Посетите наш website")
                .create();

        return aboutPage;
    }

}
