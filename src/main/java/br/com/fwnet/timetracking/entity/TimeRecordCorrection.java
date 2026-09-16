package br.com.fwnet.timetracking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_record_corrections")
public class TimeRecordCorrection {

    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "time_record_id", nullable = false)
    private TimeRecord timeRecord;

    @ManyToOne(optional = false)
    @JoinColumn(name = "corrected_by_user_id", nullable = false)
    private User correctedByUser;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "previous_work_date", nullable = false)
    private LocalDate previousWorkDate;

    @Column(name = "new_work_date", nullable = false)
    private LocalDate newWorkDate;

    @Column(name = "previous_recorded_at", nullable = false)
    private OffsetDateTime previousRecordedAt;

    @Column(name = "new_recorded_at", nullable = false)
    private OffsetDateTime newRecordedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public TimeRecordCorrection() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TimeRecord getTimeRecord() {
        return timeRecord;
    }

    public void setTimeRecord(TimeRecord timeRecord) {
        this.timeRecord = timeRecord;
    }

    public User getCorrectedByUser() {
        return correctedByUser;
    }

    public void setCorrectedByUser(User correctedByUser) {
        this.correctedByUser = correctedByUser;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getPreviousWorkDate() {
        return previousWorkDate;
    }

    public void setPreviousWorkDate(LocalDate previousWorkDate) {
        this.previousWorkDate = previousWorkDate;
    }

    public LocalDate getNewWorkDate() {
        return newWorkDate;
    }

    public void setNewWorkDate(LocalDate newWorkDate) {
        this.newWorkDate = newWorkDate;
    }

    public OffsetDateTime getPreviousRecordedAt() {
        return previousRecordedAt;
    }

    public void setPreviousRecordedAt(OffsetDateTime previousRecordedAt) {
        this.previousRecordedAt = previousRecordedAt;
    }

    public OffsetDateTime getNewRecordedAt() {
        return newRecordedAt;
    }

    public void setNewRecordedAt(OffsetDateTime newRecordedAt) {
        this.newRecordedAt = newRecordedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}