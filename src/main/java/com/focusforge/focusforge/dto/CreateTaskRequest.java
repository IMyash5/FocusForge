
package com.focusforge.focusforge.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateTaskRequest {

    @NotBlank(message = "Task title cannot be empty or blank")
    private String title;

    public CreateTaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
