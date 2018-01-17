package io.weichao.annulusdemo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import io.weichao.annulusdemo.model.AnnulusModel;

public class MainActivity extends AppCompatActivity implements IActivity{
    private AnnulusModel mAnnulusModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout rootView = new RelativeLayout(this);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(rootView);

        mAnnulusModel = new AnnulusModel(this);
        rootView.addView(mAnnulusModel.view);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAnnulusModel != null) {
            mAnnulusModel.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAnnulusModel != null) {
            mAnnulusModel.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnnulusModel != null) {
            mAnnulusModel.onDestroy();
        }
    }
}
