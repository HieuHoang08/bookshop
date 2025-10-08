package com.hh.Job.service;


import com.hh.Job.domain.Address;
import com.hh.Job.domain.User;
import com.hh.Job.domain.response.user.ResCreateUserDTO;
import com.hh.Job.domain.response.user.ResUpdateUserDTO;
import com.hh.Job.domain.response.user.ResUserDTO;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.user.UserDTO;
import com.hh.Job.repository.AddressRepository;
import com.hh.Job.repository.UserRepository;
import com.hh.Job.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;


    public UserService(UserRepository userRepository, AddressRepository addressRepository) {

        this.userRepository = userRepository;

        this.addressRepository = addressRepository;
    }

    public User handleCreateUser(User user) {
        Address address = user.getAddress();

        if (address != null && address.getId() == null) {
            // Lưu địa chỉ trước nếu là mới
            address = addressRepository.save(address);
            user.setAddress(address);
        }

        return userRepository.save(user);
    }


    public void handleDeleteUser(Long id) {

        this.userRepository.deleteById(id);
    }

    public UserDTO getUserDTOById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        // Tạo DTO
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAge(user.getAge());

        // Nếu user có address thì gán dữ liệu
        if (user.getAddress() != null) {
            dto.setStreet(user.getAddress().getStreet());
            dto.setWard(user.getAddress().getWard());
            dto.setCity(user.getAddress().getCity());
            dto.setCountry(user.getAddress().getCountry());
            dto.setPostalCode(user.getAddress().getPostalCode());
        }

        return dto;
    }

//    public List<User> fetchAllUsers(Pageable pageable) {
//        Page<User> pageUser = this.userRepository.findAll(pageable);
//        return pageUser.getContent();
//    }
    public User fetchUserById(Long id) {
        Optional<User> user = this.userRepository.findById(id);
        if (user.isPresent()) {

            return user.get();
        }
        return null;
    }


    public User handleUpdateUser(User user) {
        User crrUser = this.fetchUserById(user.getId());
        if (crrUser != null) {

            crrUser.setName(user.getName());
            crrUser.setPassword(user.getPassword());
            crrUser.setEmail(user.getEmail());

            //update
            crrUser = this.userRepository.save(crrUser);
        }
        return crrUser;
    }

    public User handleGetUserByUsername(String username) {

        return this.userRepository.findByEmail(username);
    }
// phan trang theo pageable
//    public ResultPaginationDTO fetchAllUser(Pageable pageable) {
//        Page<User> pageUser = this.userRepository.findAll(pageable);
//        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
//        Meta meta = new Meta();
//
//        meta.setPage(pageUser.getNumber() + 1);
//        meta.setPageSize(pageUser.getSize());
//        meta.setTotal(pageUser.getTotalElements());
//        meta.setPages(pageUser.getTotalPages());
//
//        resultPaginationDTO.setMeta(meta);
//        resultPaginationDTO.setResult(pageUser.getContent());
//        return resultPaginationDTO;
//    }

    // phan trang theo specification(filter)

    public ResultPaginationDTO fetchAllUser(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageUser.getTotalElements());
        meta.setPages(pageUser.getTotalPages());

        resultPaginationDTO.setMeta(meta);

        List<ResUserDTO> listUser = pageUser.getContent()
                .stream()
                .map(item -> new ResUserDTO(
                        item.getId(),
                        item.getName(),
                        item.getEmail(),
                        item.getGender(),
                        item.getAddress(),
                        item.getAge(),
                        item.getPhone(),
                        item.getCreatedAt(),
                        item.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        resultPaginationDTO.setResult(listUser);
        return resultPaginationDTO;
    }

    public boolean isEmailExists(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResCreateUserDTO convertToRestCreateUserDTO (User user) {
        ResCreateUserDTO res = new ResCreateUserDTO();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setAddress(user.getAddress());
        res.setPhone(user.getPhone());
        res.setGender(user.getGender());
        res.setCreatedAt(user.getCreatedAt());
        return res;
    }

    public ResUserDTO convertToRestUserDTO(User user) {
        ResUserDTO res = new ResUserDTO();

        res.setId(user.getId());
        res.setEmail(user.getEmail());
        res.setName(user.getName());
        res.setAge(user.getAge());
        res.setAddress(user.getAddress());
        res.setPhone(user.getPhone());
        res.setGender(user.getGender());
        res.setCreatedAt(user.getCreatedAt());
        res.setUpdatedAt(user.getUpdatedAt());

        return res;
    }


    public ResUpdateUserDTO convertToRestUpdateUserDTO(User user) {
        ResUpdateUserDTO res = new ResUpdateUserDTO();

        res.setId(user.getId());
        res.setName(user.getName());         // ✅ sửa lại cho khớp DTO
        res.setAddress(user.getAddress());
        res.setPhone(user.getPhone());
        res.setAge(user.getAge());
        res.setGender(user.getGender());
        res.setUpdatedAt(user.getUpdatedAt());

        return res;
    }


    public void updateUserToken(String username, String token) {
        User crrUser = this.handleGetUserByUsername(username);
        if (crrUser != null) {
            crrUser.setRefreshToken(token);
            this.userRepository.save(crrUser);
        }

    }


    public User getUserByRefreshTokenAndEmail(String refresh_token, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refresh_token, email);
    }
}

