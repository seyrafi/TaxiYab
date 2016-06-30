package com.taxiyab.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class MyToast extends Toast{

	static List<Toast> list = new ArrayList<Toast>();
	public MyToast(Context context) {
		super(context);
	}

	private static void setOnView(Toast toast, View view){
		if (view == null){
			return;
		}
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		//int x = location[0];
		int y = location[1];
		toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, y);
	}
	
	public static Toast makeText(Context context, CharSequence text, int duration, boolean keepPreviousToasts){
		if (!keepPreviousToasts)
			cancelAll();
		Toast toast = Toast.makeText(context, text, duration);
		if (toast != null)
			list.add(toast);
		return toast;
	}

	public static Toast makeText(Context context, CharSequence text, int duration){
		return makeText(context, text, duration, false);
	}

	public static Toast makeText(Context context, CharSequence text, int duration, View view){
		Toast toast  = makeText(context, text, duration, false);
		if (toast != null)
			setOnView(toast, view);
		return toast;
	}

	public static Toast makeText(Context context, int resId, int duration, boolean keepPreviousToasts){
		if (!keepPreviousToasts)
			cancelAll();
		Toast toast = Toast.makeText(context, resId, duration);
		if (toast != null)
			list.add(toast);
		return toast;
	}

	public static Toast makeText(Context context, int resId, int duration){
		return makeText(context, resId, duration, false);
	}
	
	public static void cancelAll(){
		for(Toast toast: list)
			if (toast != null)
				toast.cancel();
		list.clear();
	}
}
