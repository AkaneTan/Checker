package org.akanework.checker

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.widget.TextView

class InfoDialogFragment : DialogFragment() {

    @SuppressLint("InflateParams")
    @Deprecated("Deprecated in Java")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it, R.style.CheckerDialogTheme)
            // Get the layout inflater.
            val inflater = activity.layoutInflater;

            // Inflate and set the layout for the dialog.
            // Pass null as the parent view because it's going in the dialog
            // layout.
            val inflatedLayout = inflater.inflate(R.layout.dialog_info, null)
            inflatedLayout.findViewById<TextView>(R.id.version).text = BuildConfig.VERSION_NAME
            builder.setView(inflatedLayout)
            val alert = builder.create()
            alert
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
