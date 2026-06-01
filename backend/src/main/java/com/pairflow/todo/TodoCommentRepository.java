package com.pairflow.todo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoCommentRepository extends JpaRepository<TodoComment, String> {

    List<TodoComment> findByTodoIdOrderByCreatedAtAsc(String todoId);

    void deleteByTodoId(String todoId);
}
