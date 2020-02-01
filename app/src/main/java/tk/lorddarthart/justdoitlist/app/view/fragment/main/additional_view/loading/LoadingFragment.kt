package tk.lorddarthart.justdoitlist.app.view.fragment.main.additional_view.loading

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tk.lorddarthart.justdoitlist.app.model.pojo.main.ToDoItemDayModel
import tk.lorddarthart.justdoitlist.app.model.pojo.main.ToDoItemModel
import tk.lorddarthart.justdoitlist.app.view.fragment.base.BaseFragment
import tk.lorddarthart.justdoitlist.app.view.fragment.main.additional_view.error.ErrorFragment
import tk.lorddarthart.justdoitlist.app.view.fragment.main.additional_view.no_to_do.NoToDoFragment
import tk.lorddarthart.justdoitlist.databinding.FragmentLoadingBinding
import tk.lorddarthart.justdoitlist.util.comparators.CompareObjectsToDoItemDayModel
import tk.lorddarthart.justdoitlist.util.comparators.CompareObjectsToDoItemModel
import tk.lorddarthart.justdoitlist.util.constants.DateFormatsTemplates
import tk.lorddarthart.justdoitlist.util.constants.cloudfirestorestructure.CloudFirestoreMainPage
import tk.lorddarthart.justdoitlist.util.constants.cloudfirestorestructure.todo.year.month.day.list.CloudFirestoreToDoYearMonthDay
import tk.lorddarthart.justdoitlist.util.constants.cloudfirestorestructure.todo.year.month.day.list.todoofday.CloudFirestoreToDoDayItem
import tk.lorddarthart.justdoitlist.util.converters.DayTitleConverter
import tk.lorddarthart.justdoitlist.util.helper.logDebug
import tk.lorddarthart.justdoitlist.util.helper.logError
import tk.lorddarthart.justdoitlist.util.navigation.NavAction
import java.text.SimpleDateFormat
import java.util.*

class LoadingFragment : BaseFragment(), LoadingFragmentView {
    private lateinit var loadingFragmentBinding: FragmentLoadingBinding
    private val toDoMonth: MutableList<MutableList<ToDoItemDayModel>> = mutableListOf()

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        loadingFragmentBinding = FragmentLoadingBinding.inflate(inflater, container, false)
        val fromDatabaseToTimestamp = SimpleDateFormat(DateFormatsTemplates.fromDatabaseToTimestamp)
        val tododay = mutableListOf<ToDoItemDayModel>()
        if (FirebaseAuth.getInstance().currentUser != null) {
            FirebaseFirestore.getInstance().collection(CloudFirestoreMainPage.field_todo_name)
                    .document(FirebaseAuth.getInstance().currentUser!!.uid).collection(
                            SimpleDateFormat(DateFormatsTemplates.mYear).format(System.currentTimeMillis()).toString())
                    .get().addOnSuccessListener { years ->
                        if (!years.isEmpty) {
                            for (year in years.documents) {
                                year.reference.collection(
                                        SimpleDateFormat(DateFormatsTemplates.mMonth).format(System.currentTimeMillis()).toString()
                                ).get().addOnSuccessListener { month ->
                                    if (!month.isEmpty) {
                                        tododay.clear()
                                        for ((z, day) in month.documents.withIndex()) {
                                            val mFromTimestampToTitle = SimpleDateFormat(
                                                    DateFormatsTemplates.getFromTimestampToTitle(
                                                            toCalendar(fromDatabaseToTimestamp.parse(
                                                                    day.reference.id + "." + month.documents[0].reference.parent.id + "." + year.reference.parent.id
                                                            )
                                                            ), activity)
                                            )
                                            val title: String = DayTitleConverter.convertToPreferred(
                                                    mFromTimestampToTitle,
                                                    fromDatabaseToTimestamp.parse(day.reference.id + "." + month.documents[0].reference.parent.id + "." + year.reference.parent.id)
                                            )
                                            day.reference.collection(
                                                    CloudFirestoreToDoYearMonthDay.field_todoofday_name
                                            ).get().addOnSuccessListener { tasks ->
                                                val todo: MutableList<ToDoItemModel> = arrayListOf()
                                                for (newTask in tasks.documents) {
                                                    todo.add(
                                                            ToDoItemModel(newTask.getString(CloudFirestoreToDoDayItem.field_comment_name),
                                                                    newTask.getBoolean(CloudFirestoreToDoDayItem.field_completed_name),
                                                                    newTask.getLong(CloudFirestoreToDoDayItem.field_notify_name),
                                                                    newTask.getLong(CloudFirestoreToDoDayItem.field_priority_name),
                                                                    newTask.getLong(CloudFirestoreToDoDayItem.field_timestamp_name),
                                                                    newTask.getString(CloudFirestoreToDoDayItem.field_title_name)
                                                            )
                                                    )
                                                    todo.sortWith(CompareObjectsToDoItemModel)
                                                }
                                                tododay.add(ToDoItemDayModel(title, todo))
                                                tododay.sortWith(CompareObjectsToDoItemDayModel)
                                                if (z == month.documents.lastIndex) {
                                                    with(activity.baseActivityPresenter.toDoList) {
                                                        clear()
                                                        addAll(tododay)
                                                    }
                                                    NavAction.OpenNavAction.openInBase(fragment = ErrorFragment())
                                                }
                                            }.addOnFailureListener {
                                                Log.e(TAG, "Problem: ", it)
                                            }
                                        }
                                    } else {
                                        noTasks()
                                    }
                                }.addOnFailureListener {
                                    onFailure(it)
                                }
                            }
                        } else {
                            noTasks()
                        }
                    }.addOnFailureListener {
                        onFailure(it)
                    }
        }
        return loadingFragmentBinding.root
    }

    private fun toCalendar(date: Date): Calendar {
        val cal = Calendar.getInstance()
        cal.time = date
        return cal
    }

    private fun noTasks() {
        logDebug { "Currently no tasks" }

        if (this@LoadingFragment.isVisible) {
            NavAction.OpenNavAction.openInBase(fragment = NoToDoFragment())
        }
    }

    private fun onFailure(e: Exception) {
        logError(e) { "data request to Firebase failed. You know why? Oh, this is simple: " }

        if (this@LoadingFragment.isVisible) {
            NavAction.OpenNavAction.openInBase(fragment = ErrorFragment())
        }
    }

    override fun initListeners() {

    }

    override fun start() {

    }
}
