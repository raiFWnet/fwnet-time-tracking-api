CREATE TABLE time_records (
                              id UUID PRIMARY KEY,
                              user_id UUID NOT NULL,
                              work_date DATE NOT NULL,
                              record_type VARCHAR(30) NOT NULL,
                              recorded_at TIMESTAMPTZ NOT NULL,
                              source VARCHAR(20) NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL,

                              CONSTRAINT fk_time_records_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE RESTRICT,

                              CONSTRAINT uk_time_records_user_work_date_record_type
                                  UNIQUE (user_id, work_date, record_type)
);

CREATE INDEX idx_time_records_user_work_date
    ON time_records (user_id, work_date);