package com.example.macmini1.carouselviewdemo;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.macmini1.carouselviewdemo.Carousel.Carousel;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Carousel c;
    ArrayList<String> a = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        c = findViewById(R.id.carousel);

        a.add("1");
        a.add("2");
        a.add("3");
        a.add("4");
        a.add("5");
        a.add("6");

        c.setAutoScroll(true);
        c.registResourceId(R.layout.carousel_cell);
        c.setDelegate(new Carousel.CarouselDelegate() {

            @Override
            public void carouselWillShowItem(Carousel carousel, View cell, int index) {
//                cell.setBackgroundColor(getColor());
                String s = a.get(index);
                TextView tv = cell.findViewById(R.id.tv_number);
                tv.setText(s);
            }

            @Override
            public void carouselDidShowItem(Carousel carousel, int index) {
//                Log.d("Scroll", "carouselDidShowItem: " + index);
            }

            @Override
            public int carouselNumberOfItems(Carousel carousel) {
                return a.size();
            }
        });
        c.reloadData();

    }

    private int getColor() {
        int color = Color.WHITE;
        String r,g,b;
        Random random = new Random();
        r = Integer.toHexString(random.nextInt(256)).toUpperCase();
        g = Integer.toHexString(random.nextInt(256)).toUpperCase();
        b = Integer.toHexString(random.nextInt(256)).toUpperCase();

        r = r.length()==1 ? "0" + r : r ;
        g = g.length()==1 ? "0" + g : g ;
        b = b.length()==1 ? "0" + b : b ;
        color = Color.parseColor("#" + r + g + b);
        return color;
    }
}
