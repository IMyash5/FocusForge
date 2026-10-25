
ALTER TABLE task
ADD COLUMN user_id BIGINT;

ALTER TABLE task
ADD CONSTRAINT fk_task_user
FOREIGN KEY (user_id)
REFERENCES app_users(id);

CREATE INDEX idx_task_user_id
ON task(user_id);
