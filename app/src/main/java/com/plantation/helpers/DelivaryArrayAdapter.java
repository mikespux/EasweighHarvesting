package com.plantation.helpers;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.plantation.R;

import java.util.ArrayList;

public class DelivaryArrayAdapter extends ArrayAdapter<Delivary> {

	Context context;
	int layoutResourceId;
	ArrayList<Delivary> students = new ArrayList<Delivary>();

	public DelivaryArrayAdapter(Context context, int layoutResourceId,
								ArrayList<Delivary> studs) {
		super(context, layoutResourceId, studs);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.students = studs;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item = convertView;
		StudentWrapper StudentWrapper = null;

		if (item == null) {
			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			item = inflater.inflate(layoutResourceId, parent, false);
			StudentWrapper = new StudentWrapper();
			StudentWrapper.number = item.findViewById(R.id.tv_number);
			StudentWrapper.deldate = item.findViewById(R.id.tv_date);
			StudentWrapper.totalkgs = item.findViewById(R.id.txtTotalKgs);
			StudentWrapper.print = item.findViewById(R.id.btnPrint);

			item.setTag(StudentWrapper);
		} else {
			StudentWrapper = (StudentWrapper) item.getTag();
		}

		Delivary student = students.get(position);
		StudentWrapper.number.setText(student.getName());
		StudentWrapper.deldate.setText(student.getAge());
		StudentWrapper.totalkgs.setText(student.getAddress());

		StudentWrapper.print.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(context, "Edit", Toast.LENGTH_LONG).show();
			}
		});


		return item;

	}

	static class StudentWrapper {
		TextView number;
		TextView deldate;
		TextView totalkgs;
		Button print;

	}

}
