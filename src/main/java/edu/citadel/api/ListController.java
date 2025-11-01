package edu.citadel.api;

import edu.citadel.dal.ListEntityRepository;
import edu.citadel.dal.model.ListEntity;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/list")
public class ListController {

    private final ListEntityRepository repository;

    @Autowired
    public ListController(final ListEntityRepository repository) {
        this.repository = repository;
    }

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListEntity> createList() {
        ListEntity list = new ListEntity();

        return ResponseEntity.ok(repository.save(list));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ListEntity> deleteList(@PathVariable Long id) {
        return repository.findById(id).map(list -> {
            Hibernate.initialize(list.getListItems()); // initialize the list
            repository.delete(list);
            return ResponseEntity.ok(list);
        }).orElse(ResponseEntity.notFound().build());
    }

}
