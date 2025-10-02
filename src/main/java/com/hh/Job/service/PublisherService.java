package com.hh.Job.service;


import com.hh.Job.domain.Publisher;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.publisher.ResPublisherDTO;
import com.hh.Job.repository.PublisherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PublisherService {

    private final PublisherRepository publisherRepository;
    public PublisherService(PublisherRepository publisherRepository) {
        this.publisherRepository = publisherRepository;
    }

    public Publisher createPublisher(Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    public Publisher updatePublisher(Publisher publisher) {
        Optional<Publisher> optionalPublisher = publisherRepository.findById(publisher.getId());
        if (optionalPublisher.isPresent()) {
            Publisher current = optionalPublisher.get();
            current.setName(publisher.getName());
            current.setAddress(publisher.getAddress());
            current.setPhone(publisher.getPhone());
            current.setEmail(publisher.getEmail());
            return publisherRepository.save(current);
        }
        return null;
    }

    public void deletePublisher(Long id) {
        publisherRepository.deleteById(id);
    }

    public Publisher fetchPublisherById(Long id) {
        Optional<Publisher> optionalPublisher = publisherRepository.findById(id);
        if (optionalPublisher.isPresent()) {
            return optionalPublisher.get();
        }
        return null;
    }

    public ResultPaginationDTO fetchAllPublishers(Specification<Publisher> spec, Pageable pageable) {
        Page<Publisher> pagePub = this.publisherRepository.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1); // số trang hiện tại
        meta.setPageSize(pageable.getPageSize());  // số phần tử mỗi trang
        meta.setTotal(pagePub.getTotalElements()); // tổng số bản ghi
        meta.setPages(pagePub.getTotalPages());    // tổng số trang (đặt tên field là pages thay vì page cho rõ nghĩa)

        List<ResPublisherDTO> listPub = pagePub.getContent().stream()
                .map(publisher -> new ResPublisherDTO(
                        publisher.getId(),
                        publisher.getName(),
                        publisher.getEmail(),
                        publisher.getAddress(),
                        publisher.getPhone()
                )).collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(listPub);

        return result;
    }


}
