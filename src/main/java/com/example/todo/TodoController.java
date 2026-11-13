package com.example.todo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class TodoController {

    // In-memory "database" - no external DB needed
    private final List<Todo> todos = new CopyOnWriteArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    // Value comes from env var APP_MESSAGE (set via Kubernetes ConfigMap)
    @Value("${app.message}")
    private String message;

    @GetMapping("/hello")
    public Map<String, String> hello() throws Exception {
        // hostname = pod name in Kubernetes -> shows load balancing across replicas
        return Map.of("message", message,
                      "host", InetAddress.getLocalHost().getHostName());
    }

    @GetMapping("/todos")
    public List<Todo> list() {
        return todos;
    }

    @PostMapping("/todos")
    public ResponseEntity<?> add(@RequestBody Todo input) {
        if (input.getTitle() == null || input.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "title is required"));
        }
        Todo todo = new Todo(counter.incrementAndGet(), input.getTitle(), input.isDone());
        todos.add(todo);
        return ResponseEntity.status(HttpStatus.CREATED).body(todo);
    }
}
