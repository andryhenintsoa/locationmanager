package mg.henkinn.locationmanager.fakes

import android.app.Dialog
import android.content.Context
import mg.henkinn.locationmanager.provider.dialogprovider.DialogProvider
import org.mockito.Mock
import org.mockito.MockitoAnnotations


open class MockDialogProvider(private val message: String) : DialogProvider() {
    @Mock
    var dialog: Dialog? = null
    fun message(): String {
        return message
    }

    override fun getDialog(context: Context): Dialog? {
        return dialog
    }

    init {
        MockitoAnnotations.initMocks(this)
    }
}