package com.antsapps.triples;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.antsapps.triples.backend.Card;
import com.antsapps.triples.cardsview.CardBackgroundDrawable;
import com.antsapps.triples.cardsview.CardDrawable;
import com.antsapps.triples.cardsview.SymbolDrawable;

import java.util.List;

public class SampleCardView extends View {

    private Card mCard;
    private final CardBackgroundDrawable mBackground = new CardBackgroundDrawable();
    private SymbolDrawable mSymbol;

    public SampleCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCard(Card card) {
        mCard = card;
        mSymbol = new SymbolDrawable(getContext(), mCard);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCard == null) return;

        Rect bounds = new Rect(0, 0, getWidth(), getHeight());
        mBackground.setBounds(bounds);
        mBackground.draw(canvas);

        List<Rect> symbolBounds = CardDrawable.getBoundsForNumId(mCard.mNumber, bounds);
        for (Rect rect : symbolBounds) {
            mSymbol.setBounds(rect);
            mSymbol.draw(canvas);
        }
    }
}
