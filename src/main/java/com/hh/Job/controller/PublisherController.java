package com.hh.Job.controller;


import com.hh.Job.domain.Publisher;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.service.PublisherService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1")
public class PublisherController {

    private final PublisherService publisherService;
    public PublisherController(PublisherService publisherService) {

        this.publisherService = publisherService;
    }

    @PostMapping("/publishers")
    @APImessage("create a publisher")
    public ResponseEntity<Publisher> createPublisher(@RequestBody Publisher publisher) {
        Publisher createdPublisher = publisherService.createPublisher(publisher);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPublisher);
    }

    @PutMapping("/publishers/{id}")
    @APImessage("update a publisher")
    public ResponseEntity<Publisher> updatePublisher(@RequestBody Publisher publisher, @PathVariable("id") Long id) throws IdInvalidException {
        publisher.setId(id);
        Publisher updatedPublisher = publisherService.updatePublisher(publisher);
        if(updatedPublisher == null) {
            throw new IdInvalidException("publisher voi id" + publisher.getId() + " khong ton tai");
        }
        return ResponseEntity.ok(updatedPublisher);
    }

    @GetMapping("publishers/{id}")
    @APImessage("fetch publisher by ID")
    public ResponseEntity<Publisher> fetchPublisher(@PathVariable("id") Long id) throws IdInvalidException {
        Publisher pub = publisherService.fetchPublisherById(id);
        if(pub == null) {
            throw new IdInvalidException("publisher voi id" + id + " not found");
        }
        return ResponseEntity.ok(pub);
    }

    @DeleteMapping("/publishers/{id}")
    @APImessage("Delete an publisher")
    public ResponseEntity<Object> deletePublisher(@PathVariable("id") Long id) throws IdInvalidException {
        Publisher publisher = publisherService.fetchPublisherById(id);
        if(publisher == null) {
            throw new IdInvalidException("publisher voi id" + id + " not found");

        }
        publisherService.deletePublisher(id);
        return ResponseEntity.ok().body("delete publisher successfully");
    }

    @GetMapping("/publishers")
    @APImessage("fetch all publisher")
    public ResponseEntity<ResultPaginationDTO>  getAllPublishers(
            @Filter Specification<Publisher> spec,
            Pageable pageable
            ){
        ResultPaginationDTO result = publisherService.fetchAllPublishers(spec,pageable);
        return  ResponseEntity.ok(result);
    }
}
