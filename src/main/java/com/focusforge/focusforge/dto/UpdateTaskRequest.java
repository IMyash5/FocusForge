
package com.focusforge.focusforge.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateTaskRequest {

    @NotBlank(message = "Task title cannot be empty or blank")
    private String title;

    private boolean completed;

    public UpdateTaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
