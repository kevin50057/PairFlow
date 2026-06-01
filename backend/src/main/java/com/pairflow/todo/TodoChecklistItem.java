package com.pairflow.todo;

import com.pairflow.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "todo_checklist_items", indexes = @Index(name = "idx_checklist_todo", columnList = "todoId"))
public class TodoChecklistItem extends BaseEntity {

    @Column(nullable = false)
    private String todoId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private int sortOrder = 0;
}
