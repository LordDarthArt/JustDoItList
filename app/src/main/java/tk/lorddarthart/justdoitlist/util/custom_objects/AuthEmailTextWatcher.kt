package tk.lorddarthart.justdoitlist.util.custom_objects

import android.text.Editable
import android.text.TextWatcher

class AuthEmailTextWatcher(val action: (string: String, start: Int, before: Int, count: Int) -> Unit): TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        // do nothing
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // do nothing
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        action(s.toString(), start, before, count)
    }
}