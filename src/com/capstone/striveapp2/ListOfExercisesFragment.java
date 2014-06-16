package com.capstone.striveapp2;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ListOfExercisesFragment extends Fragment implements View.OnClickListener {
	
	//private final int REQUEST_CODE = 3;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.exercise_list_frag, container, false);
		Button joggingButton = (Button) view.findViewById(R.id.joggingButton);
		Button bikingButton = (Button) view.findViewById(R.id.bikingButton);
		joggingButton.setOnClickListener(this);
		bikingButton.setOnClickListener(this);
		return view; 
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.joggingButton:
			Intent intent = new Intent(getActivity(), JoggingActivity.class);
			getActivity().startActivity(intent);
			break;
		case R.id.bikingButton:
			Intent intent2 = new Intent(getActivity(), BikingActivity.class);
			getActivity().startActivity(intent2);
			break;
		default:
		}
		
	}
}
