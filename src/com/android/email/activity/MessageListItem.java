/*
 * Copyright (C) 2009 The Android Open Source Project
 * Copyright (C) 2011 Iranian Supreme Council of ICT, The FarsiTel Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.email.activity;

import com.android.email.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.util.Log;

/**
 * This custom View is the list item for the MessageList activity, and serves two purposes:
 * 1.  It's a container to store message metadata (e.g. the ids of the message, mailbox, & account)
 * 2.  It handles internal clicks such as the checkbox or the favorite star
 */
public class MessageListItem extends RelativeLayout {

    public long mMessageId;
    public long mMailboxId;
    public long mAccountId;
    public boolean mRead;
    public boolean mFavorite;
    public boolean mSelected;

    private boolean mAllowBatch;
    private MessageList.MessageListAdapter mAdapter;

    private boolean mDownEvent;
    private boolean mCachedViewPositions;
    private int mCheckRight;
    private int mStarLeft;
    private int firstX;
    private boolean firstRecorded;
    private boolean magicMb;
    private long parentId;
    private int mCheckLeft;
    private int mStarRight;

    // Padding to increase clickable areas on left & right of each list item
    private final static float CHECKMARK_PAD = 10.0F;
    private final static float STAR_PAD = 10.0F;
    private static final int SWIPE_LEFT = 75;


    public MessageListItem(Context context) {
        super(context);
    }

    public MessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MessageListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Called by the adapter at bindView() time
     * 
     * @param adapter the adapter that creates this view
     * @param allowBatch true if multi-select is enabled for this list
     */
    public void bindViewInit(MessageList.MessageListAdapter adapter, boolean allowBatch, long mbid) {
        mAdapter = adapter;
        mAllowBatch = allowBatch;
        mCachedViewPositions = false;
        magicMb = mbid < -1;
        parentId = mbid;
    }

    /**
     * Overriding this method allows us to "catch" clicks in the checkbox or star
     * and process them accordingly.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        int touchX = (int) event.getX();

        if (!mCachedViewPositions) {
            float paddingScale = getContext().getResources().getDisplayMetrics().density;
            int checkPadding = (int) ((CHECKMARK_PAD * paddingScale) + 0.5);
            int starPadding = (int) ((STAR_PAD * paddingScale) + 0.5);
            mCheckRight = findViewById(R.id.selected).getRight() + checkPadding;
            mStarLeft = findViewById(R.id.favorite).getLeft() - starPadding;
            mCheckLeft = findViewById(R.id.selected).getLeft() - checkPadding;
            mStarRight = findViewById(R.id.favorite).getRight() + starPadding;
            mCachedViewPositions = true;
        }
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownEvent = true;
                if (mRTL) {
                    if (mAllowBatch && touchX > mCheckLeft) {
                        handled = true;
                    } else if (touchX < mStarRight) {
                        firstX = touchX;
                        firstRecorded = true;
                        handled = true;
                    }
                } else {
                    if (mAllowBatch && touchX < mCheckRight) {
                        handled = true;
                    } else if (touchX > mStarLeft) {
                        firstX = touchX;
                        firstRecorded = true;
                        handled = true;
                    }
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                mDownEvent = false;
                break;

            case MotionEvent.ACTION_UP:
                if (mDownEvent) {
                    if (mRTL) {
                        if (mAllowBatch && touchX > mCheckLeft) {
                            mSelected = !mSelected;
                            mAdapter.updateSelected(this, mSelected);
                            handled = true;
                        } else if (touchX < mStarRight) {
                            mFavorite = !mFavorite;
                            mAdapter.updateFavorite(this, mFavorite);
                            handled = true;
                        }
                    } else {
                        if (mAllowBatch && touchX < mCheckRight) {
                            mSelected = !mSelected;
                            mAdapter.updateSelected(this, mSelected);
                            handled = true;
                        } else if (touchX > mStarLeft) {
                            mFavorite = !mFavorite;
                            mAdapter.updateFavorite(this, mFavorite);
                            handled = true;
                        }
                    }
                }
                firstRecorded = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!magicMb)
                    break;
                if (touchX < (firstX - SWIPE_LEFT) && firstRecorded) {
                    MessageList.actionHandleMailboxFromMagic(mContext, this.mMailboxId, parentId);
                    }
                break;
        }

        if (handled) {
            postInvalidate();
        } else {
            handled = super.onTouchEvent(event);
        }

        return handled;
    }
}
