
import { useEffect, useState } from "react";
import Auth from "./Auth";
import { api } from "./api";
import "./App.css";

const navigation = [
  { label: "Overview", icon: "◫" },
  { label: "My Tasks", icon: "☷" },
  { label: "Upcoming", icon: "▦" },
];

function mapTask(task) {
  const priority = (task.priority || "MEDIUM").toLowerCase();

  return {
    id: task.id,
    title: task.title,
    description: task.description || "",
    category: "Task",
    priority: priority.charAt(0).toUpperCase() + priority.slice(1),
    dueDate: task.dueDate || null,
    due: task.dueDate ? formatDueDate(task.dueDate) : "No due date",
    completed: Boolean(task.completed),
  };
}

function formatDueDate(dateString) {
  const parts = dateString.split("-").map(Number);

  if (parts.length !== 3 || parts.some(Number.isNaN)) {
    return dateString;
  }

  const [year, month, day] = parts;
  const date = new Date(year, month - 1, day);

  return date.toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
  });
}

function isUpcoming(task) {
  if (!task.dueDate || task.completed) return false;

  const today = new Date();
  const todayString = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0"),
  ].join("-");

  return task.dueDate >= todayString;
}

async function fetchDashboardData(token) {
  const [page, stats] = await Promise.all([
    api.getTasks(token, { page: 0, size: 100 }),
    api.getStats(token).catch(() => null),
  ]);

  const records = Array.isArray(page) ? page : page?.content ?? [];

  return {
    tasks: records.map(mapTask),
    stats,
  };
}

function App() {
  // Keep every React hook above the conditional return.
  const [session, setSession] = useState(() => {
    try {
      const saved = sessionStorage.getItem("focusforge-session");
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  const [tasks, setTasks] = useState([]);
  const [stats, setStats] = useState(null);
  const [tasksLoading, setTasksLoading] = useState(false);
  const [savingTask, setSavingTask] = useState(false);
  const [tasksError, setTasksError] = useState("");
  const [activeNav, setActiveNav] = useState("Overview");
  const [filter, setFilter] = useState("All");
  const [search, setSearch] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [newTitle, setNewTitle] = useState("");
const [newDescription, setNewDescription] = useState("");
const [newPriority, setNewPriority] = useState("MEDIUM");
const [newDueDate, setNewDueDate] = useState("");

  const token = session?.accessToken;

  // Load the signed-in user's tasks and statistics.
  useEffect(() => {
    if (!token) {
      setTasks([]);
      setStats(null);
      setTasksLoading(false);
      return;
    }

    let cancelled = false;

    async function loadTasks() {
      setTasksLoading(true);
      setTasksError("");

      try {
        const data = await fetchDashboardData(token);

        if (!cancelled) {
          setTasks(data.tasks);
          setStats(data.stats);
        }
      } catch (error) {
        if (!cancelled) {
          setTasksError(error.message || "Could not load your tasks.");
          setTasks([]);
          setStats(null);
        }
      } finally {
        if (!cancelled) {
          setTasksLoading(false);
        }
      }
    }

    loadTasks();

    return () => {
      cancelled = true;
    };
  }, [token]);

  function handleLogin(result) {
    sessionStorage.setItem(
      "focusforge-session",
      JSON.stringify(result)
    );
    setSession(result);
  }

  function handleLogout() {
    sessionStorage.removeItem("focusforge-session");
    setSession(null);
    setTasks([]);
    setStats(null);
    setTasksError("");
    setActiveNav("Overview");
    setFilter("All");
    setSearch("");
    setNewTitle("");
    setShowForm(false);
  }

  async function refreshTasks() {
    const data = await fetchDashboardData(token);
    setTasks(data.tasks);
    setStats(data.stats);
    setTasksError("");
  }

  async function toggleTask(id) {
    const task = tasks.find((item) => item.id === id);
    if (!task || savingTask) return;

    setSavingTask(true);
    setTasksError("");

    try {
      await api.updateTask(token, id, {
        title: task.title,
        description: task.description,
        dueDate: task.dueDate,
        priority: task.priority.toUpperCase(),
        completed: !task.completed,
      });

      await refreshTasks();
    } catch (error) {
      setTasksError(error.message || "Could not update the task.");
    } finally {
      setSavingTask(false);
    }
  }

  
async function addTask(event) {
  event.preventDefault();

  const title = newTitle.trim();
  if (!title || savingTask) return;

  setSavingTask(true);
  setTasksError("");

  try {
    await api.createTask(token, {
      title,
      description: newDescription.trim(),
      priority: newPriority,
      dueDate: newDueDate || null,
    });

    await refreshTasks();

    setNewTitle("");
    setNewDescription("");
    setNewPriority("MEDIUM");
    setNewDueDate("");
    setShowForm(false);
    setActiveNav("My Tasks");
    setFilter("All");
  } catch (error) {
    setTasksError(error.message || "Could not create the task.");
  } finally {
    setSavingTask(false);
  }
}


  async function deleteTask(id) {
    if (savingTask) return;

    setSavingTask(true);
    setTasksError("");

    try {
      await api.deleteTask(token, id);
      await refreshTasks();
    } catch (error) {
      setTasksError(error.message || "Could not delete the task.");
    } finally {
      setSavingTask(false);
    }
  }

  // Render authentication only after all hooks are declared.
  if (!session?.accessToken) {
    return <Auth onLogin={handleLogin} />;
  }

  const completed =
    stats?.completedTasks ??
    tasks.filter((task) => task.completed).length;

  const totalTasks = stats?.totalTasks ?? tasks.length;

  const pending =
    stats?.pendingTasks ?? Math.max(0, totalTasks - completed);

  const progress =
    stats?.completionPercentage ??
    (totalTasks ? Math.round((completed / totalTasks) * 100) : 0);

  const visibleTasks = tasks.filter((task) => {
    const matchesFilter =
      filter === "All" ||
      (filter === "Completed" && task.completed) ||
      (filter === "Pending" && !task.completed);

    const matchesSearch = task.title
      .toLowerCase()
      .includes(search.toLowerCase());

    const matchesPage =
      activeNav !== "Upcoming" || isUpcoming(task);

    return matchesFilter && matchesSearch && matchesPage;
  });

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <a
          className="brand"
          href="#overview"
          onClick={() => setActiveNav("Overview")}
        >
          <span className="brand-mark">F</span>
          <span>
            focus<span className="brand-accent">forge</span>
            <small>YOUR SPACE TO FOCUS</small>
          </span>
        </a>

        <div className="workspace-label">WORKSPACE</div>

        <nav className="nav-list">
          {navigation.map((item) => (
            <button
              key={item.label}
              className={`nav-item ${
                activeNav === item.label ? "active" : ""
              }`}
              onClick={() => setActiveNav(item.label)}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.label}
              {item.label === "My Tasks" && (
                <span className="nav-count">{pending}</span>
              )}
            </button>
          ))}
        </nav>

        <div className="sidebar-bottom">
          <div className="focus-card">
            <div className="focus-card-icon">✦</div>
            <strong>Stay in your zone</strong>
            <p>
              Small steps every day lead to remarkable progress.
            </p>
            <div className="mini-progress">
              <span style={{ width: `${progress}%` }} />
            </div>
            <small>{progress}% of tasks completed</small>
          </div>

          <div className="profile">
            <div className="avatar">
              {(session.email || "Y").charAt(0).toUpperCase()}
            </div>
            <div className="profile-info">
              <strong>{session.email || "My Workspace"}</strong>
              <span>Personal account</span>
            </div>
            <button
              type="button"
              className="logout-button"
              onClick={handleLogout}
              title="Log out"
              aria-label="Log out"
            >
              ↪
            </button>
          </div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">
            Workspace <span>/</span> <strong>{activeNav}</strong>
          </div>
          <div className="topbar-right">
            <span className="status-dot" />
            <span className="status-text">Your space, your pace</span>
            <div className="top-avatar">
              {(session.email || "Y").charAt(0).toUpperCase()}
            </div>
          </div>
        </header>

        <section className="dashboard">
          <div className="welcome-row">
            <div>
              <div className="eyebrow">
                <span /> YOUR PRODUCTIVITY SPACE
              </div>
              <h1>
                {activeNav === "Overview"
                  ? "Good things take focus."
                  : activeNav}
              </h1>
              <p className="subtitle">
                {activeNav === "Upcoming"
                  ? "A quick look at what's coming up next."
                  : "Organize your day. Make progress on what matters."}
              </p>
            </div>

            <button
              className="primary-button"
              onClick={() => setShowForm(!showForm)}
              disabled={savingTask}
            >
              <span>＋</span> New task
            </button>
          </div>

          {tasksError && (
            <div className="error-banner" role="alert">
              {tasksError}
              <button
                type="button"
                onClick={() => setTasksError("")}
                aria-label="Dismiss error"
              >
                ×
              </button>
            </div>
          )}

          
{showForm && (
  <form className="new-task-form" onSubmit={addTask}>
    <input
      autoFocus
      value={newTitle}
      onChange={(event) => setNewTitle(event.target.value)}
      placeholder="Task title"
      maxLength={100}
      required
      disabled={savingTask}
    />

    <textarea
      value={newDescription}
      onChange={(event) => setNewDescription(event.target.value)}
      placeholder="Description (optional)"
      maxLength={1000}
      rows={3}
      disabled={savingTask}
    />

    <div className="task-form-options">
      <label>
        <span>Priority</span>
        <select
          value={newPriority}
          onChange={(event) => setNewPriority(event.target.value)}
          disabled={savingTask}
        >
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
        </select>
      </label>

      <label>
        <span>Due date</span>
        <input
          type="date"
          value={newDueDate}
          onChange={(event) => setNewDueDate(event.target.value)}
          disabled={savingTask}
        />
      </label>
    </div>

    <button
      className="primary-button"
      type="submit"
      disabled={savingTask || !newTitle.trim()}
    >
      {savingTask ? "Saving..." : "Add task"}
    </button>

    <button
      className="cancel-button"
      type="button"
      onClick={() => setShowForm(false)}
      disabled={savingTask}
    >
      Cancel
    </button>
  </form>
)}


          <div className="date-line">
            <span>◷</span> YOUR DAY AT A GLANCE
          </div>

          <div className="stats-grid">
            <article className="stat-card">
              <div className="stat-top">
                <span>Total tasks</span>
                <span className="stat-icon violet">☷</span>
              </div>
              <div className="stat-number">
                {totalTasks}
                <span className="stat-unit"> tasks</span>
              </div>
              <div className="stat-foot">Everything on your plate</div>
            </article>

            <article className="stat-card">
              <div className="stat-top">
                <span>In progress</span>
                <span className="stat-icon blue">◴</span>
              </div>
              <div className="stat-number">
                {pending}
                <span className="stat-unit"> tasks</span>
              </div>
              <div className="stat-foot">
                <span className="foot-dot blue-dot" />
                Ready for your attention
              </div>
            </article>

            <article className="stat-card">
              <div className="stat-top">
                <span>Completed</span>
                <span className="stat-icon green">✓</span>
              </div>
              <div className="stat-number">
                {completed}
                <span className="stat-unit"> tasks</span>
              </div>
              <div className="stat-foot">
                <span className="foot-dot green-dot" />
                Nice work, keep going
              </div>
            </article>

            <article className="stat-card progress-stat">
              <div className="stat-top">
                <span>Daily progress</span>
                <span className="stat-icon orange">↗</span>
              </div>
              <div className="stat-number">
                {progress}
                <span className="stat-unit">%</span>
              </div>
              <div className="progress-track">
                <span style={{ width: `${progress}%` }} />
              </div>
              <div className="stat-foot">One step at a time</div>
            </article>
          </div>

          <section className="tasks-panel">
            <div className="panel-heading">
              <div>
                <h2>
                  {activeNav === "Upcoming" ? "Coming up" : "Your tasks"}
                  <span className="task-total">{visibleTasks.length}</span>
                </h2>
                <p>Keep your momentum going.</p>
              </div>

              <label className="search-box">
                <span>⌕</span>
                <input
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  placeholder="Search tasks..."
                  aria-label="Search tasks"
                />
                <kbd>⌘ K</kbd>
              </label>
            </div>

            <div className="task-toolbar">
              <div className="filter-tabs">
                {["All", "Pending", "Completed"].map((item) => (
                  <button
                    key={item}
                    className={filter === item ? "selected" : ""}
                    onClick={() => setFilter(item)}
                  >
                    {item}
                  </button>
                ))}
              </div>
              <span className="sort-label">
                ↕ <span>Recently added</span>
              </span>
            </div>

            <div className="task-list">
              {tasksLoading ? (
                <div className="empty-state">
                  <strong>Loading your tasks...</strong>
                  <p>Fetching your FocusForge workspace.</p>
                </div>
              ) : visibleTasks.map((task) => (
                <article
                  className={`task-row ${
                    task.completed ? "task-completed" : ""
                  }`}
                  key={task.id}
                >
                  <button
                    className={`task-checkbox ${
                      task.completed ? "checked" : ""
                    }`}
                    onClick={() => toggleTask(task.id)}
                    disabled={savingTask}
                    aria-label={
                      task.completed
                        ? "Mark task incomplete"
                        : "Mark task complete"
                    }
                  >
                    {task.completed ? "✓" : ""}
                  </button>

                  <div className="task-main">
                    <strong>{task.title}</strong>
                    {task.description && (
  <p className="task-description">{task.description}</p>
)}
                    <div className="task-meta">
                      <span className="category-dot" />
                      {task.category}
                      <span className="meta-separator">·</span>
                      <span>◷ {task.due}</span>
                    </div>
                  </div>

                  <span
                    className={`priority priority-${task.priority.toLowerCase()}`}
                  >
                    <span /> {task.priority}
                  </span>

                  <button
                    className="delete-task"
                    onClick={() => deleteTask(task.id)}
                    disabled={savingTask}
                    aria-label={`Delete ${task.title}`}
                    title="Delete task"
                  >
                    ×
                  </button>
                </article>
              ))}

              {!tasksLoading && visibleTasks.length === 0 && (
                <div className="empty-state">
                  <span>✦</span>
                  <strong>
                    {tasksError ? "Tasks could not be loaded" : "No tasks found"}
                  </strong>
                  <p>
                    {tasksError
                      ? "Check your backend connection and try again."
                      : "Try another filter or add a new task to get started."}
                  </p>
                </div>
              )}
            </div>

            <div className="panel-footer">
              <span>
                Showing <strong>{visibleTasks.length}</strong> of{" "}
                <strong>{tasks.length}</strong> loaded tasks
              </span>
              <span className="footer-note">
                <span /> All progress counts
              </span>
            </div>
          </section>

          <footer className="app-footer">
            <span>
              FOCUSFORGE <span className="footer-version">/ 01</span>
            </span>
            <span>
              BUILT FOR THE WORK THAT MATTERS{" "}
              <span className="footer-spark">✳</span>
            </span>
          </footer>
        </section>
      </main>
    </div>
  );
}

export default App;
