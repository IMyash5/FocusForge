
package com.focusforge.focusforge.repository;

import com.focusforge.focusforge.model.Task;
import com.focusforge.focusforge.model.TaskPriority;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTitleContainingIgnoreCase(String title);

    List<Task> findByCompleted(boolean completed);

    List<Task> findByPriority(TaskPriority priority);

    List<Task> findByCompletedAndPriority(
            boolean completed,
            TaskPriority priority);

    @Query("""
        SELECT t FROM Task t
        WHERE (:search IS NULL OR :search = ''
               OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:completed IS NULL OR t.completed = :completed)
          AND (:priority IS NULL OR t.priority = :priority)
        """)
    List<Task> findByFilters(
            @Param("search") String search,
            @Param("completed") Boolean completed,
            @Param("priority") TaskPriority priority);

    @Query(
        value = """
            SELECT t FROM Task t
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:completed IS NULL OR t.completed = :completed)
              AND (:priority IS NULL OR t.priority = :priority)
            """,
        countQuery = """
            SELECT COUNT(t) FROM Task t
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:completed IS NULL OR t.completed = :completed)
              AND (:priority IS NULL OR t.priority = :priority)
            """
    )
    Page<Task> findByFilters(
            @Param("search") String search,
            @Param("completed") Boolean completed,
            @Param("priority") TaskPriority priority,
            Pageable pageable);

    @Query(
        value = """
            SELECT t FROM Task t
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:completed IS NULL OR t.completed = :completed)
              AND (:priority IS NULL OR t.priority = :priority)
            ORDER BY
                CASE WHEN t.dueDate IS NULL THEN 1 ELSE 0 END ASC,
                t.dueDate ASC,
                t.id ASC
            """,
        countQuery = """
            SELECT COUNT(t) FROM Task t
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:completed IS NULL OR t.completed = :completed)
              AND (:priority IS NULL OR t.priority = :priority)
            """
    )
    Page<Task> findByFiltersDueDateAscending(
            @Param("search") String search,
            @Param("completed") Boolean completed,
            @Param("priority") TaskPriority priority,
            Pageable pageable);

    @Query(
        value = """
            SELECT t FROM Task t
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:completed IS NULL OR t.completed = :completed)
              AND (:priority IS NULL OR t.priority = :priority)
            ORDER BY
                CASE WHEN t.dueDate IS NULL THEN 1 ELSE 0 END ASC,
                t.dueDate DESC,
                t.id ASC
            """,
        countQuery = """
            SELECT COUNT(t) FROM Task t
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:completed IS NULL OR t.completed = :completed)
              AND (:priority IS NULL OR t.priority = :priority)
            """
    )
    Page<Task> findByFiltersDueDateDescending(
            @Param("search") String search,
            @Param("completed") Boolean completed,
            @Param("priority") TaskPriority priority,
            Pageable pageable);
}
