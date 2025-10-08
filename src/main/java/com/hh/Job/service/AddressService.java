package com.hh.Job.service;

import com.hh.Job.domain.Address;
import com.hh.Job.domain.Category;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.address.ResAddressDTO;
import com.hh.Job.domain.response.category.ResCategoryDTO;
import com.hh.Job.repository.AddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }

    public Address createAddress(Address address) {
        return addressRepository.save(address);
    }

    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }

    public Address updateAddress(Address address) {
        Optional<Address> optionalAddress = addressRepository.findById(address.getId());
        if (optionalAddress.isPresent()) {
            Address currentAddress = optionalAddress.get();
            currentAddress.setStreet(address.getStreet());
            currentAddress.setWard(address.getWard());
            currentAddress.setCity(address.getCity());
            currentAddress.setCountry(address.getCountry());
            currentAddress.setPostalCode(address.getPostalCode());
            return addressRepository.save(currentAddress);
        }
        return null;
    }

    public ResultPaginationDTO fetchAllAddress(Specification<Address> spec, Pageable pageable) {
        Page<Address> pageAddress = this.addressRepository.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1); // số trang hiện tại
        meta.setPageSize(pageable.getPageSize());   // số phần tử mỗi trang
        meta.setTotal(pageAddress.getTotalElements()); // tổng số bản ghi
        meta.setPages(pageAddress.getTotalPages());    // tổng số trang

        List<ResAddressDTO> listAddress = pageAddress.getContent().stream()
                .map(address -> new ResAddressDTO(
                        address.getId(),
                        address.getStreet(),
                        address.getWard(),
                        address.getCity(),
                        address.getCountry(),
                        address.getPostalCode()
                ))
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(listAddress);

        return result;
    }

}
