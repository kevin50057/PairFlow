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
@Table(name = "todo_comments", indexes = @Index(name = "idx_comment_todo", columnList = "todoId"))
public class TodoComment extends BaseEntity {

    @Column(nullable = false)
    private String todoId;

    @Column(nullable = false)
    private String authorId;

    @Column(nullable = false, length = 1000)
    private String content;
}
