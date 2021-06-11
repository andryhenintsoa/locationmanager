package mg.henkinn.locationmanager.provider.dialogprovider

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface


class SimpleMessageDialogProvider(private val message: String) : DialogProvider(),
    DialogInterface.OnClickListener {
    fun message(): String {
        return message
    }

    override fun getDialog(context: Context): Dialog {
        return AlertDialog.Builder(context)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(android.R.string.ok, this)
            .setNegativeButton(android.R.string.cancel, this)
            .create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                if (dialogListener != null) dialogListener!!.onPositiveButtonClick()
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                if (dialogListener != null) dialogListener!!.onNegativeButtonClick()
            }
        }
    }
}