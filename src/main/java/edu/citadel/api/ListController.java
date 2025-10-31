package edu.citadel.api;

import edu.citadel.dal.ListEntityRepository;
import edu.citadel.dal.model.ListEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
