
package com.focusforge.focusforge.repository;

import com.focusforge.focusforge.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
