package com.example.collaborativetodo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collaborativetodo.data.dtos.Task
import com.example.collaborativetodo.data.dtos.User
import com.example.collaborativetodo.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val total: Int = 0
)

data class TaskDetailState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

data class UserSearchState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CreateTaskState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class TaskViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _taskListState = MutableStateFlow(TaskListState())
    val taskListState: StateFlow<TaskListState> = _taskListState.asStateFlow()

    private val _sharedTaskListState = MutableStateFlow(TaskListState())
    val sharedTaskListState: StateFlow<TaskListState> = _sharedTaskListState.asStateFlow()

    private val _taskDetailState = MutableStateFlow(TaskDetailState())
    val taskDetailState: StateFlow<TaskDetailState> = _taskDetailState.asStateFlow()

    private val _userSearchState = MutableStateFlow(UserSearchState())
    val userSearchState: StateFlow<UserSearchState> = _userSearchState.asStateFlow()

    private val _createTaskState = MutableStateFlow(CreateTaskState())
    val createTaskState: StateFlow<CreateTaskState> = _createTaskState.asStateFlow()

    fun loadTasks(page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            _taskListState.value = _taskListState.value.copy(isLoading = true)

            try {
                val result = taskRepository.getTasks(page, pageSize)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null) {
                        _taskListState.value = TaskListState(
                            tasks = if (page == 1) response.tasks else _taskListState.value.tasks + response.tasks,
                            page = response.page,
                            hasMore = response.hasMore,
                            total = response.total
                        )
                    } else {
                        _taskListState.value = _taskListState.value.copy(
                            error = "Failed to load tasks: No data received",
                            isLoading = false
                        )
                    }
                } else {
                    _taskListState.value = _taskListState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to load tasks",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _taskListState.value = _taskListState.value.copy(
                    error = e.message ?: "Failed to load tasks",
                    isLoading = false
                )
            }
        }
    }

    fun loadSharedTasks(page: Int = 1, pageSize: Int = 20) {
        viewModelScope.launch {
            _sharedTaskListState.value = _sharedTaskListState.value.copy(isLoading = true)

            try {
                val result = taskRepository.getSharedTasks(page, pageSize)
                if (result.isSuccess) {
                    val response = result.getOrNull()
                    if (response != null) {
                        _sharedTaskListState.value = TaskListState(
                            tasks = if (page == 1) response.tasks else _sharedTaskListState.value.tasks + response.tasks,
                            page = response.page,
                            hasMore = response.hasMore,
                            total = response.total
                        )
                    } else {
                        _sharedTaskListState.value = _sharedTaskListState.value.copy(
                            error = "Failed to load shared tasks: No data received",
                            isLoading = false
                        )
                    }
                } else {
                    _sharedTaskListState.value = _sharedTaskListState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to load shared tasks",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _sharedTaskListState.value = _sharedTaskListState.value.copy(
                    error = e.message ?: "Failed to load shared tasks",
                    isLoading = false
                )
            }
        }
    }

    fun loadTask(id: String) {
        viewModelScope.launch {
            _taskDetailState.value = TaskDetailState(isLoading = true)

            try {
                val result = taskRepository.getTask(id)
                if (result.isSuccess) {
                    val task = result.getOrNull()
                    if (task != null) {
                        _taskDetailState.value = TaskDetailState(task = task)
                    } else {
                        _taskDetailState.value = TaskDetailState(
                            error = "Failed to load task: No data received"
                        )
                    }
                } else {
                    _taskDetailState.value = TaskDetailState(
                        error = result.exceptionOrNull()?.message ?: "Failed to load task"
                    )
                }
            } catch (e: Exception) {
                _taskDetailState.value = TaskDetailState(
                    error = e.message ?: "Failed to load task"
                )
            }
        }
    }

    fun createTask(
        title: String,
        description: String? = null,
        dueDate: String? = null,
        priority: Int = 0,
        category: String? = null
    ) {
        viewModelScope.launch {
            _createTaskState.value = CreateTaskState(isLoading = true)

            try {
                val result = taskRepository.createTask(title, description, dueDate, priority, category)
                if (result.isSuccess) {
                    val task = result.getOrNull()
                    if (task != null) {
                        _createTaskState.value = CreateTaskState(isSuccess = true)
                        loadTasks(1) // Refresh list
                    } else {
                        _createTaskState.value = CreateTaskState(
                            error = "Failed to create task: No data received"
                        )
                    }
                } else {
                    _createTaskState.value = CreateTaskState(
                        error = result.exceptionOrNull()?.message ?: "Failed to create task"
                    )
                }
            } catch (e: Exception) {
                _createTaskState.value = CreateTaskState(
                    error = e.message ?: "Failed to create task"
                )
            }
        }
    }

    fun updateTask(
        id: String,
        title: String? = null,
        description: String? = null,
        isCompleted: Boolean? = null,
        dueDate: String? = null,
        priority: Int? = null,
        category: String? = null
    ) {
        viewModelScope.launch {
            _taskDetailState.value = _taskDetailState.value.copy(isLoading = true)

            try {
                val result = taskRepository.updateTask(id, title, description, isCompleted, dueDate, priority, category)
                if (result.isSuccess) {
                    val task = result.getOrNull()
                    if (task != null) {
                        _taskDetailState.value = TaskDetailState(
                            task = task,
                            isSuccess = true
                        )
                        loadTasks(_taskListState.value.page) // Refresh list
                    } else {
                        _taskDetailState.value = _taskDetailState.value.copy(
                            error = "Failed to update task: No data received",
                            isLoading = false
                        )
                    }
                } else {
                    _taskDetailState.value = _taskDetailState.value.copy(
                        error = result.exceptionOrNull()?.message ?: "Failed to update task",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _taskDetailState.value = _taskDetailState.value.copy(
                    error = e.message ?: "Failed to update task",
                    isLoading = false
                )
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            try {
                val result = taskRepository.deleteTask(id)
                if (result.isSuccess) {
                    loadTasks(1) // Refresh list
                } else {
                    // Handle error if needed
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun shareTask(taskId: String, email: String, permission: String = "view") {
        viewModelScope.launch {
            try {
                val result = taskRepository.shareTask(taskId, email, permission)
                if (result.isSuccess) {
                    // Success handling
                } else {
                    // Error handling
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            if (query.length < 2) {
                _userSearchState.value = UserSearchState()
                return@launch
            }

            _userSearchState.value = UserSearchState(isLoading = true)

            try {
                val result = taskRepository.searchUsers(query)
                if (result.isSuccess) {
                    val users = result.getOrNull()
                    if (users != null) {
                        _userSearchState.value = UserSearchState(users = users)
                    } else {
                        _userSearchState.value = UserSearchState(
                            error = "Failed to search users: No data received"
                        )
                    }
                } else {
                    _userSearchState.value = UserSearchState(
                        error = result.exceptionOrNull()?.message ?: "Failed to search users"
                    )
                }
            } catch (e: Exception) {
                _userSearchState.value = UserSearchState(
                    error = e.message ?: "Failed to search users"
                )
            }
        }
    }

    fun clearTaskDetail() {
        _taskDetailState.value = TaskDetailState()
    }

    fun clearError() {
        _taskListState.value = _taskListState.value.copy(error = null)
        _sharedTaskListState.value = _sharedTaskListState.value.copy(error = null)
        _taskDetailState.value = _taskDetailState.value.copy(error = null)
        _userSearchState.value = _userSearchState.value.copy(error = null)
        _createTaskState.value = _createTaskState.value.copy(error = null)
    }

    fun resetCreateTaskState() {
        _createTaskState.value = CreateTaskState()
    }

    fun resetTaskDetailSuccess() {
        _taskDetailState.value = _taskDetailState.value.copy(isSuccess = false)
    }
}