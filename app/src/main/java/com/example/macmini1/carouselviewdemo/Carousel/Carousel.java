package com.example.macmini1.carouselviewdemo.Carousel;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Carousel extends RelativeLayout {

    private CarouselPager mPager;
    private Context mContext;
    private int mResourceId = -1;
    private Carousel self;
    private CarouselDelegate mDelegate;
    private CarouselAdapter mAdapter;
    private int mCurrentItem = 1;
    private int mItemCount;

    public Carousel(Context context) {
        super(context);
        init(context);
    }

    public Carousel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Carousel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        self = this;

        mAdapter = new CarouselAdapter();

        mPager = new CarouselPager(mContext);
        RelativeLayout.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        addView(mPager,layoutParams);

        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

//                Log.d("Selected", "onPageSelected: " + position);
                mCurrentItem = position;
                int display_index = mCurrentItem;
                if (position == 0) {
                    display_index = mItemCount;
                } else if (position == mItemCount + 1) {
                    display_index = 1;
                } else {
                    display_index = position;

                    // 代理方法在此调用避免两端点的索引重复出现
                    if (mDelegate != null) {
                        mDelegate.carouselDidShowItem(self,display_index - 1);
                    }
                }


            }

            @Override
            public void onPageScrollStateChanged(int state) {

                // 解决两端切换时闪屏
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (mCurrentItem == mPager.getAdapter().getCount() - 1) {
                        mPager.setCurrentItem(1, false);
                    }
                    else if (mCurrentItem == 0) {
                        mPager.setCurrentItem(mPager.getAdapter().getCount() - 2, false);
                    }
                }
            }
        });
        mPager.setAdapter(mAdapter);
        reloadData();
    }

    public void registResourceId(int resourceId) {
        mResourceId = resourceId;
    }

    public void setDelegate(CarouselDelegate delegate) {
        mDelegate = delegate;
    }

    public void reloadData() {
        mAdapter.notifyDataSetChanged();
        mCurrentItem = 1;
        mPager.setCurrentItem(mCurrentItem,false);
        if (mAutoScroll) {

            startAutoScroll();
        }
    }



    public interface CarouselDelegate {

        void carouselWillShowItem(Carousel carousel,View cell,int index);
        void carouselDidShowItem(Carousel carousel,int index);
        int  carouselNumberOfItems(Carousel carousel);

    }

    public class CarouselAdapter extends PagerAdapter {

        ArrayList<View> mReusePool = new ArrayList<>();

        @Override
        public int getCount() {
            if (mDelegate == null || mResourceId == -1) {

                return mItemCount = 0;
            } else {
                int number = mDelegate.carouselNumberOfItems(self);
                if (number == 0) {
                    return mItemCount = 0;
                } else {
                    mItemCount = number;
                    return number + 2;
                }
            }

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
//            return super.getItemPosition(object);
            return POSITION_NONE; // 解决notifyDataChange后界面没更新
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            View cell = (View) object;
            container.removeView(cell);
            mReusePool.add(cell);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View cell = null;
            if (mReusePool.isEmpty()) {

                cell = (View) LayoutInflater.from(mContext).inflate(mResourceId,null);

            } else {
                cell = mReusePool.get(0);
                mReusePool.remove(cell);
            }


            int index = position;
            if (position == 0) {
                index = mItemCount - 1;
            } else if (position == mItemCount + 1) {
                index = 0;
            } else {
                index = position - 1;
            }


            if (mDelegate!= null) {
                mDelegate.carouselWillShowItem(self,cell,index);
            }

            container.addView(cell);

            return cell;
        }
    }

    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean mAutoScroll = false;
    private int mAutoScrollFlag = 0; // Timer开始第一次执行无效
    private int mScrollTimeInterval = 10; // 单位秒

    public void setAutoScroll(boolean autoScroll) {
        mAutoScroll = autoScroll;
        if (mAutoScroll) {
            startAutoScroll();
        } else {
            stopAutoScroll();
        }
    }

    public boolean getAutoScroll() {
        return mAutoScroll;
    }

    public void setScrollTimeInterval(int scrollTimeInterval) {
        this.mScrollTimeInterval = scrollTimeInterval;
    }

    public int getScrollTimeInterval() {
        return this.mScrollTimeInterval;
    }

    private void startAutoScroll() {
        if (mItemCount == 0) {
            return;
        }

        stopAutoScroll();

        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAutoScrollFlag == 0) {
                            mAutoScrollFlag = 1;
                        } else {
                            mPager.setCurrentItem(mCurrentItem + 1,true);
                        }
                    }
                });
            }
        };
        mTimer.schedule(mTimerTask,new Date(),1000 * mScrollTimeInterval);
    }

    private void stopAutoScroll() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
            mAutoScrollFlag = 0;
        }
    }

    private class CarouselPager extends ViewPager {

        public CarouselPager(Context context) {
            super(context);
        }

        public CarouselPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                stopAutoScroll();
            }

            if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                if (mAutoScroll) {
                    startAutoScroll();
                }
            }

            return super.onTouchEvent(event);
        }
    }
}
