package com.antsapps.triples;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.antsapps.triples.backend.Application;
import com.antsapps.triples.backend.Card;
import com.antsapps.triples.backend.OnValidTripleSelectedListener;
import com.antsapps.triples.backend.TutorialGame;
import com.antsapps.triples.backend.Utils;
import com.antsapps.triples.cardsview.CardsView;
import com.google.common.collect.ImmutableList;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Collection;

public class TutorialFragment extends Fragment implements OnValidTripleSelectedListener {

    private CardsView mHelpCardsView;
    private ImmutableList<Card> mCardsShown;
    private TextView mNumberExplanation;
    private TextView mShapeExplanation;
    private TextView mPatternExplanation;
    private TextView mColorExplanation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.help, container, false);

        mHelpCardsView = (CardsView) view.findViewById(R.id.cards_view);
        mHelpCardsView.setOnValidTripleSelectedListener(this);

        mNumberExplanation = (TextView) view.findViewById(R.id.number_explanation);
        mShapeExplanation = (TextView) view.findViewById(R.id.shape_explanation);
        mPatternExplanation = (TextView) view.findViewById(R.id.pattern_explanation);
        mColorExplanation = (TextView) view.findViewById(R.id.color_explanation);

        ImmutableList<Card> newCards = Utils.createValidTriple();
        mHelpCardsView.updateCardsInPlay(newCards);
        mCardsShown = newCards;
        updateTextExplanation();

        view.findViewById(R.id.show_me_another).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAnotherTriple();
            }
        });

        // Add Start Tutorial button to the LinearLayout inside the ScrollView
        LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.help_content);
        Button startButton = new Button(getActivity());
        startButton.setText("START TUTORIAL GAME");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TutorialGame game = TutorialGame.createFromSeed(System.currentTimeMillis());
                Application.getInstance(getActivity()).setTutorialGame(game);
                Intent intent = new Intent(getActivity(), TutorialGameActivity.class);
                startActivity(intent);
            }
        });
        contentLayout.addView(startButton);

        FirebaseAnalytics.getInstance(getActivity()).logEvent(AnalyticsConstants.Event.VIEW_HELP, null);

        return view;
    }

    @Override
    public void onValidTripleSelected(Collection<Card> validTriple) {
        showAnotherTriple();
    }

    private void showAnotherTriple() {
        ImmutableList<Card> newCards = Utils.createValidTriple();
        mHelpCardsView.updateCardsInPlay(newCards);
        mCardsShown = newCards;
        updateTextExplanation();
    }

    private void updateTextExplanation() {
        mNumberExplanation.setText(checkNumber(mCardsShown) ? R.string.all_same : R.string.all_different);
        mShapeExplanation.setText(checkShape(mCardsShown) ? R.string.all_same : R.string.all_different);
        mPatternExplanation.setText(checkPattern(mCardsShown) ? R.string.all_same : R.string.all_different);
        mColorExplanation.setText(checkColor(mCardsShown) ? R.string.all_same : R.string.all_different);
    }

    private static boolean checkNumber(ImmutableList<Card> cards) {
        return cards.get(0).mNumber == cards.get(1).mNumber && cards.get(0).mNumber == cards.get(2).mNumber;
    }

    private static boolean checkShape(ImmutableList<Card> cards) {
        return cards.get(0).mShape == cards.get(1).mShape && cards.get(0).mShape == cards.get(2).mShape;
    }

    private static boolean checkPattern(ImmutableList<Card> cards) {
        return cards.get(0).mPattern == cards.get(1).mPattern && cards.get(0).mPattern == cards.get(2).mPattern;
    }

    private static boolean checkColor(ImmutableList<Card> cards) {
        return cards.get(0).mColor == cards.get(1).mColor && cards.get(0).mColor == cards.get(2).mColor;
    }
}
