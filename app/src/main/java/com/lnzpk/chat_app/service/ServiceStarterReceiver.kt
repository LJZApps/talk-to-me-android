package com.lnzpk.chat_app.service

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class ServiceStarterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val statusJob = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val statusJobInfo = JobInfo.Builder(2, ComponentName(context, StatusJob::class.java))
            // only add if network access is required
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
        statusJob.schedule(statusJobInfo)

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobInfo = JobInfo.Builder(1, ComponentName(context, NotificationJob::class.java))
            // only add if network access is required
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
        jobScheduler.schedule(jobInfo)

        //Toast.makeText(context, "Broadcast is called!", Toast.LENGTH_SHORT).show()
        //Log.i("Autostart", "started")
    }
}