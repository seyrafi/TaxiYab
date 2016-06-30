package com.taxiyab;

import android.app.Dialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.os.AsyncTask;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;

public class WaitDialog extends Dialog {
	private Context context;
	private static AsyncTask task;
	private static WaitDialog dlg;
	private boolean cancelable = true;
	
	private static void closePrev(){
		if (dlg != null)
			dlg.dismiss();
	}
	
	private static void setConfig(){
		WindowManager.LayoutParams lp = dlg.getWindow().getAttributes();  
		lp.dimAmount = 0.0f; // Dim level. 0.0 - no dim, 1.0 - completely opaque
		dlg.getWindow().setAttributes(lp);
		dlg.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	}

	
	public static void show(Context context, AsyncTask task){
		try{
			closePrev();
			dlg = new WaitDialog(context, android.R.style.Theme_Translucent_NoTitleBar, task, true);
			setConfig();
			dlg.show();
		}catch(Exception e){
		}
	}
	
	public static void show(Context context, AsyncTask task, boolean cancelable){
		try{
			closePrev();
			dlg = new WaitDialog(context, android.R.style.Theme_Translucent_NoTitleBar, task, cancelable);
			setConfig();
			dlg.show();
		}catch(Exception e){
		}
	}
	
	public static void show(Context context,int theme,  AsyncTask task){
		closePrev();
		dlg = new WaitDialog(context, theme, task, true);
		setConfig();
		dlg.show();
	}
	
	public static void show(Context context,int theme,  AsyncTask task, boolean cancelable){
		closePrev();
		dlg = new WaitDialog(context, theme, task, cancelable);
		setConfig();
		dlg.show();
	}
	
	public static void close(){
		if (dlg != null){
			dlg.dismiss();
			dlg = null;
			task = null;
		}
	}
	
	public WaitDialog(Context context, AsyncTask task, boolean cancelable) {
        super(context);
        this.context = context;
        this.task = task;
        this.cancelable = cancelable;
        SetParams();
	}
	
	public WaitDialog(Context context, int theme, AsyncTask task, boolean cancelable) {
        super(context, theme);
        this.context = context;
        this.task = task;
        this.cancelable = cancelable;
        SetParams();
	}
	
	private void SetParams(){
		setContentView(R.layout.dialog_wait);
    	getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    	setCancelable(false);
    	if (cancelable){
    		ProgressBar progressBar1 = (ProgressBar)findViewById(R.id.progressBar1);
    		registerForContextMenu(progressBar1);
    	}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.progressBar1){
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		    //menu.setHeaderTitle(name);
		    String[] menuItems = context.getResources().getStringArray(R.array.progressbar_menu_items);
		    for (int i = 0; i<menuItems.length; i++) {
		      menu.add(Menu.NONE, i, i, menuItems[i]);
		    }
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem menuItem) {
		if (task != null)
			task.cancel(true);
		close();
		return true;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  /*AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	  int menuItemIndex = item.getItemId();
	  String[] menuItems = context.getResources().getStringArray(R.array.basket_menu_items);
	  String menuItemName = menuItems[menuItemIndex];*/
	  if (task != null)
		  task.cancel(true);
	  return true;
	}
}
