package com.taxiyab.Service;

import android.content.Context;
import android.os.AsyncTask;

import com.taxiyab.Model.DestStructureBase;
import com.taxiyab.WaitDialog;

import java.util.ArrayList;

/**
 * Created by MehrdadS on 6/22/2016.
 */
public class MyAsyncTask  extends AsyncTask<Void, Integer, MyAsyncTask.MyAsyncTaskResultBase> {
    public interface MyAsyncTaskJob { public MyAsyncTaskResultBase perform(MyAsyncTaskParameterBase parameter); }
    public interface MyAsyncTaskPostJob { public void perform(MyAsyncTaskResultBase result); }
    public interface MyAsyncTaskSuccessHandler { public void handler(MyAsyncTaskResultBase result); }
    public interface MyAsyncTaskUIHandler { public void handle(MyAsyncTaskResultBase result); }

    public static class MyAsyncTaskResultBase {
        public String errorMessage = null;
        public Object obj = null;

        public MyAsyncTaskResultBase() {
        }

        public MyAsyncTaskResultBase(String errorMessage){
            this.errorMessage = errorMessage;
        }

        public MyAsyncTaskResultBase(Object obj){
            this.obj = obj;
        }
    }
    public static class MyAsyncTaskParameterBase {
        public Object obj = null;
        public MyAsyncTaskParameterBase(){
        }
        public MyAsyncTaskParameterBase(Object obj){
            this.obj = obj;
        }
    }
    public static class MyAsyncTaskResultGooglePlaceAPI extends MyAsyncTask.MyAsyncTaskResultBase {
        public ArrayList<DestStructureBase> list;
        public MyAsyncTaskResultGooglePlaceAPI(ArrayList<DestStructureBase> list){
            this.list = list;
        }
    }
    public static class MyAsyncTaskParameterGooglePlaceAPI extends MyAsyncTaskParameterBase{
        String query = null;
        public MyAsyncTaskParameterGooglePlaceAPI(){
        }
        public MyAsyncTaskParameterGooglePlaceAPI(String query){
            this.query = query;
        }
    }


    Context rootObject;
    MyAsyncTaskJob job = null;
    MyAsyncTaskPostJob postJob = null;
    MyAsyncTaskParameterBase parameter = null;
    MyAsyncTaskSuccessHandler successHandler = null;
    MyAsyncTaskUIHandler failHandler = null;
    MyAsyncTaskUIHandler lockHandler = null;
    MyAsyncTaskUIHandler releaseHandler = null;
    String debugThreadName = "MyAsyncTask";
    private boolean lockInternallyHandled = false;

    public static MyAsyncTask DoJob(Context rootObject,
                                    MyAsyncTaskJob job,
                                    MyAsyncTaskPostJob postJob,
                                    MyAsyncTaskParameterBase parameter,
                                    MyAsyncTaskSuccessHandler successHandler,
                                    MyAsyncTaskUIHandler failHandler,
                                    MyAsyncTaskUIHandler lockHandler,
                                    MyAsyncTaskUIHandler releaseHandler,
                                    String debugThreadName){
        return  (MyAsyncTask)(new MyAsyncTask(rootObject, job, postJob, parameter, successHandler, failHandler, lockHandler, releaseHandler, debugThreadName).execute());
    }

    public MyAsyncTask(Context rootObject,
                       MyAsyncTaskJob job,
                       MyAsyncTaskPostJob postJob,
                       MyAsyncTaskParameterBase parameter,
                       MyAsyncTaskSuccessHandler successHandler,
                       MyAsyncTaskUIHandler failHandler,
                       MyAsyncTaskUIHandler lockHandler,
                       MyAsyncTaskUIHandler releaseHandler,
                       String debugThreadName){
        this.rootObject = rootObject;
        this.job = job;
        this.postJob = postJob;
        this.parameter = parameter;
        this.successHandler = successHandler;
        this.failHandler = failHandler;
        this.lockHandler = lockHandler;
        this.releaseHandler = releaseHandler;
        this.debugThreadName = debugThreadName;
    }

    @Override
    protected void onPreExecute() {
        if (lockHandler != null)
            lockHandler.handle(null);
        else {
            lockInternallyHandled = true;
            WaitDialog.show(rootObject, this);
        }
        super.onPreExecute();
    }

    @Override
    protected MyAsyncTaskResultBase doInBackground(Void... arg0) {
        Thread.currentThread().setName(debugThreadName);

        if (job != null)
            return job.perform(parameter);
        else
            return new MyAsyncTaskResultBase("no task to be done");
    }
    protected void onPostExecute(MyAsyncTaskResultBase result) {
        if (lockInternallyHandled)
            WaitDialog.close();
        else if (releaseHandler != null)
            releaseHandler.handle(null);

        if (postJob != null)
            postJob.perform(result);
    }
}
