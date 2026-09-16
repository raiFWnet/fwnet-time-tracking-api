CREATE TABLE time_record_corrections (
                                         id UUID PRIMARY KEY,
                                         time_record_id UUID NOT NULL,
                                         corrected_by_user_id UUID NOT NULL,
                                         reason VARCHAR(500) NOT NULL,
                                         previous_work_date DATE NOT NULL,
                                         new_work_date DATE NOT NULL,
                                         previous_recorded_at TIMESTAMPTZ NOT NULL,
                                         new_recorded_at TIMESTAMPTZ NOT NULL,
                                         created_at TIMESTAMPTZ NOT NULL,

                                         CONSTRAINT fk_time_record_corrections_time_record
                                             FOREIGN KEY (time_record_id)
                                                 REFERENCES time_records(id)
                                                 ON DELETE RESTRICT,

                                         CONSTRAINT fk_time_record_corrections_corrected_by_user
                                             FOREIGN KEY (corrected_by_user_id)
                                                 REFERENCES users(id)
                                                 ON DELETE RESTRICT
);

CREATE INDEX idx_time_record_corrections_time_record_id
    ON time_record_corrections (time_record_id);

CREATE INDEX idx_time_record_corrections_corrected_by_user_id
    ON time_record_corrections (corrected_by_user_id);