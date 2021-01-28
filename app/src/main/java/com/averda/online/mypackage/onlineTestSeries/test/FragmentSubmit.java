package com.averda.online.mypackage.onlineTestSeries.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.averda.online.R;

public class FragmentSubmit extends Fragment {
    View rootView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_test_submit, container, false);
        Bundle bundle = getArguments();
        String title = bundle.getString("title");
        String msg = String.format(getString(R.string.exam_submitted_msg), title);
        ((TextView)rootView.findViewById(R.id.submitted_msg)).setText(msg);
        return rootView;
    }
    public void submitted(){
        rootView.findViewById(R.id.submitLayout).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.submitProgress).setVisibility(View.GONE);
    }
}
