package com.averda.online.mypackage.onlineClass;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.averda.online.R;
import com.averda.online.mypackage.MyPackageAdapter;
import com.averda.online.mypackage.PageViewModel;
import com.averda.online.server.ServerApi;
import com.averda.online.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * A placeholder fragment containing a simple view.
 */
public class MyPackageClassFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private MyPackageAdapter adapter = null;

    public static MyPackageClassFragment newInstance(int index) {
        MyPackageClassFragment fragment = new MyPackageClassFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_myclasspackage, container, false);
        mRecyclerView = root.findViewById(R.id.recylerView);
        if (mRecyclerView != null) {
            mRecyclerView.setHasFixedSize(true);
        }
        if(!isAdded()){
            return root;
        }
        mLayoutManager = new GridLayoutManager(getActivity(),1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //studentId is taken 4 here it is of harish
        if(!isAdded()){
            return root;
        }
        ServerApi.callServerApi(getActivity(), ServerApi.BASE_URL,"Onlineclassstudentpackage/"+ Utils.getStudentId(getActivity()), null, new ServerApi.CompleteListener() {
            @Override
            public void response(JSONObject response) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
                JSONArray data = response.optJSONArray("Body");
                JSONArray finalArray = new JSONArray();
                if (data.length() == 0){
                    root.findViewById(R.id.noItemLayout).setVisibility(View.VISIBLE);
                } else {
                    root.findViewById(R.id.noItemLayout).setVisibility(View.GONE);
                }
                for (int i =0; i<data.length(); i++){
                    if (data.optJSONObject(i).optInt("TypeID") == 1 ){
                        finalArray.put(data.optJSONObject(i));
                    }
                }
                if (data != null) {
                    if(adapter == null) {
                        if(!isAdded()){
                            return;
                        }
                        adapter = new MyPackageAdapter(finalArray,R.layout.mypackage_item,getActivity());
                        mRecyclerView.setAdapter(adapter);
                    }else{
                        adapter.refreshAdapter(finalArray);
                    }
                }
            }

            @Override
            public void error(String error) {
                if(Utils.isActivityDestroyed(getActivity())){
                    return;
                }
                root.findViewById(R.id.progressBar).setVisibility(View.GONE);
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        });
        return root;
    }
}