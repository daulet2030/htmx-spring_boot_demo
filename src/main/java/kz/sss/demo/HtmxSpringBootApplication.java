package kz.sss.demo;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Stream;

@SpringBootApplication
public class HtmxSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(HtmxSpringBootApplication.class, args);
    }

}

@Component
class Initializer {
    private final TodoRepository repository;

    Initializer(TodoRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    void reset() {
        this.repository.deleteAll();
        Stream.of("Learn HTMX", "Learn Spring View Component").forEach(t -> this.repository.save(new Todo(null, t)));
    }
}

@Controller
class TodoController {
    private final TodoRepository repository;


    TodoController(TodoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/todos")
    String getAll(Model model) {
        model.addAttribute("todos", this.repository.findAll());
        return "todos";
    }

    @DeleteMapping(value = "/todos/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    String delete(@PathVariable("id") String id, Model model) {
        this.repository.deleteById(id);
        return "";
    }

    @PostMapping("/todos")
    HtmxResponse create(@RequestParam("new-todo") String title, Model model) {
        repository.save(new Todo(null, title));
        model.addAttribute("todos", this.repository.findAll());

        return HtmxResponse.builder().view("todos :: todo-list").view("todos :: todos-form").build();
    }
}

interface TodoRepository extends CrudRepository<Todo, String> {
}

record Todo(@Id String id, String title) {
}