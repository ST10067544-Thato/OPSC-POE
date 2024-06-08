package com.isaiahvonrundstedt.fokus.features.event

import androidx.lifecycle.*
import com.isaiahvonrundstedt.fokus.database.repository.TaskRepository
import com.isaiahvonrundstedt.fokus.features.task.Task
import com.isaiahvonrundstedt.fokus.features.task.TaskPackage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    private val _tasks: LiveData<List<TaskPackage>> = repository.fetchLiveData()

    val dates: MediatorLiveData<List<LocalDate>> = MediatorLiveData()
    val tasks: MediatorLiveData<List<TaskPackage>> = MediatorLiveData()
    val tasksEmpty: LiveData<Boolean> = Transformations.map(tasks) { it.isNullOrEmpty() }

    val today: LocalDate
        get() = LocalDate.now()
    val currentMonth: YearMonth
        get() = YearMonth.now()

    var selectedDate: LocalDate = today
        set(value) {
            field = value
            tasks.value =
                _tasks.value?.filter { it.task.dueDate!!.toLocalDate() == selectedDate }
        }

    var startMonth: YearMonth = currentMonth.minusMonths(1)
    var endMonth: YearMonth = currentMonth.plusMonths(1)

    init {
        tasks.addSource(_tasks) { items ->
            tasks.value = items.filter { it.task.dueDate!!.toLocalDate() == selectedDate }
        }
        dates.addSource(_tasks) { items ->
            dates.value = items.map { it.task.dueDate!!.toLocalDate() }.distinct()
        }
    }

    fun insert(task: Task) = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        repository.insert(task)
    }

    fun remove(task: Task) = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        repository.remove(task)
    }

    fun update(task: Task) = viewModelScope.launch(Dispatchers.IO + NonCancellable) {
        repository.update(task)
    }
}
