package com.hh.Job.service;


import com.hh.Job.domain.BorrowTransaction;
import com.hh.Job.domain.User;
import com.hh.Job.domain.Voucher;
import com.hh.Job.domain.VoucherUser;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.borrow.ResBorrowDTO;
import com.hh.Job.domain.response.voucher.VoucherDTO;
import com.hh.Job.repository.OrderRepository;
import com.hh.Job.repository.UserRepository;
import com.hh.Job.repository.VoucherRepository;
import com.hh.Job.repository.VoucherUserRepository;
import com.hh.Job.util.error.IdInvalidException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoucherService {

    private final VoucherUserRepository voucherUserRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public VoucherService(VoucherUserRepository voucherUserRepository,
                          VoucherRepository voucherRepository,
                          UserRepository userRepository,
                          OrderRepository orderRepository
                          ) {
        this.voucherUserRepository = voucherUserRepository;
        this.voucherRepository = voucherRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // ==========================
    // CRUD cơ bản cho Voucher
    // ==========================

    /**
     * Tạo voucher mới.
     * Kiểm tra nếu mã voucher đã tồn tại thì báo lỗi.
     */

    public VoucherDTO createVoucher(VoucherDTO.CreateVoucherDTO dto)throws IdInvalidException {
        if (voucherRepository.existsByCode((dto.getCode()))) {
            throw new IdInvalidException("Mã voucher đã tồn tại");
        }

        Voucher voucher = new Voucher();
        voucher.setCode(dto.getCode().toUpperCase());
        voucher.setDescription(dto.getDescription());
        voucher.setDiscountType(dto.getDiscountType());
        voucher.setDiscountValue(dto.getDiscountValue());
        voucher.setMinOrderAmount(dto.getMinOrderAmount());
        voucher.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        voucher.setQuantity(dto.getQuantity());
        voucher.setStartDate(dto.getStartDate());
        voucher.setEndDate(dto.getEndDate());

        return mapToDTO(voucherRepository.save(voucher));
    }


    /**
     * Cập nhật thông tin của voucher theo id.
     */

    public VoucherDTO updateVoucher(Long id, VoucherDTO.UpdateVoucherDTO dto) throws IdInvalidException {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy voucher"));

        if (dto.getDescription() != null) voucher.setDescription(dto.getDescription());
        if (dto.getMinOrderAmount() != null) voucher.setMinOrderAmount(dto.getMinOrderAmount());
        if (dto.getMaxDiscountAmount() != null) voucher.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        if (dto.getQuantity() != null) voucher.setQuantity(dto.getQuantity());
        if (dto.getStartDate() != null) voucher.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) voucher.setEndDate(dto.getEndDate());
        if (dto.getIsActive() != null) voucher.setIsActive(dto.getIsActive());

        return mapToDTO(voucherRepository.save(voucher));
    }

    /**
     * Lấy voucher theo ID
     */

    public Optional<Voucher> fetchVoucherById(Long id){
        return voucherRepository.findById(id);
    }

    /**
     * Xóa voucher theo ID.
     * Không cho phép xóa nếu voucher đã được sử dụng.
     */

    public void deleteVoucher(Long id) throws IdInvalidException {
        Optional<Voucher> optionalVoucher = fetchVoucherById(id);

        if (optionalVoucher.isEmpty()) {
            throw new IdInvalidException("Voucher không tồn tại");
        }

        Voucher voucher = optionalVoucher.get(); // lấy voucher thực

        // kiểm tra xem voucher được dùng chưa
        int useCount = voucherRepository.countUsedByVoucher(voucher);
        if (useCount > 0) {
            throw new IdInvalidException("Không thể xóa voucher đã được sử dụng");
        }

        voucherRepository.delete(voucher);
    }

    /**
     * Lấy danh sách voucher có phân trang + lọc bằng Specification.
     */

    public ResultPaginationDTO fetchAllVoucher(Specification<Voucher> spec, Pageable pageable) {
        Page<Voucher> pageBr = voucherRepository.findAll(spec, pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(pageBr.getTotalElements());
        meta.setPages(pageBr.getTotalPages());

        List<VoucherDTO> listVoucher = pageBr.getContent()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(listVoucher);

        return result;
    }

    // ==========================
    // Lịch sử sử dụng voucher
    // ==========================

    /**
     * Lấy danh sách lịch sử sử dụng voucher (VoucherUser)
     */

    public ResultPaginationDTO getUsageHistory(Specification<VoucherUser> spec,Pageable pageable) {
        Page<VoucherUser> pageVoucherUser = voucherUserRepository.findAll(pageable);

        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(pageVoucherUser.getTotalPages());
        meta.setTotal(pageVoucherUser.getTotalElements());

        result.setMeta(meta);
        result.setResult(pageVoucherUser.getContent().stream()
                .map(this::convertToResVoucherUserDTO)
                .collect(Collectors.toList()));

        return result;
    }


    // ==========================
    // Gán voucher cho user
    // ==========================

    /**
     * Gán một voucher cho một user cụ thể.
     */
    public VoucherDTO.VoucherUserDTO assignVoucherToUser(Long voucherId, Long userId)throws IdInvalidException {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy voucher"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy user"));

        VoucherUser voucherUser = new VoucherUser();
        voucherUser.setVoucher(voucher);
        voucherUser.setUser(user);

        return mapToVoucherUserDTO(voucherUserRepository.save(voucherUser));
    }


    // ==========================
    // Trạng thái & kích hoạt voucher
    // ==========================

    /**
     * Lấy voucher theo mã code (không phân biệt hoa thường).
     */

    public VoucherDTO getVoucherByCode(String code) throws IdInvalidException {
        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy voucher với mã: " + code));
        return mapToDTO(voucher);
    }


    /**
     * Lấy danh sách voucher đang active và còn hạn.
     */

    public List<VoucherDTO> getActiveVouchers() {
        Instant now = Instant.now();
        List<Voucher> vouchers = voucherRepository.findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(now, now);

        return vouchers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Vô hiệu hóa (deactivate) voucher.
     */

    public void deactivateVoucher(Long id) throws IdInvalidException {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy voucher"));

        voucher.setIsActive(false);
        voucherRepository.save(voucher);
    }


    /**
     * Cập nhật trạng thái voucher (kích hoạt / vô hiệu hóa).
     * - Không cho phép kích hoạt nếu voucher đã hết hạn.
     * - Không làm gì nếu trạng thái không thay đổi.
     */

    public void updateVoucherStatus(Long id, boolean active) throws IdInvalidException {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Voucher not found with id " + id));

        // Nếu đang cố bật lại voucher nhưng nó đã hết hạn
        if (active && voucher.getEndDate() != null && voucher.getEndDate().isBefore(Instant.now())) {
            throw new IdInvalidException("Không thể kích hoạt lại voucher đã hết hạn");
        }

        // Nếu voucher đã ở đúng trạng thái, thì không cần thay đổi
        if (voucher.getIsActive() == active) {
            throw new IdInvalidException("Voucher đã " + (active ? "được kích hoạt" : "bị vô hiệu hóa") + " rồi");
        }

        voucher.setIsActive(active);
        voucherRepository.save(voucher);
    }

    // ==========================
    // Mapper & helper
    // ==========================


    /**
     * Convert VoucherUser → ResVoucherUserDTO (dùng trong lịch sử sử dụng).
     */

    private VoucherDTO.ResVoucherUserDTO convertToResVoucherUserDTO(VoucherUser voucherUser) {
        Voucher voucher = voucherUser.getVoucher();
        Instant now = Instant.now();
        Boolean isExpired = voucher.getEndDate() != null && now.isAfter(voucher.getEndDate());

        VoucherDTO.ResVoucherUserDTO dto = new VoucherDTO.ResVoucherUserDTO();
        dto.setId(voucherUser.getId());
        dto.setVoucherId(voucher.getId());
        dto.setVoucherCode(voucher.getCode());
        dto.setVoucherDescription(voucher.getDescription());
        dto.setDiscountType(voucher.getDiscountType());
        dto.setDiscountValue(voucher.getDiscountValue());
        dto.setMinOrderAmount(voucher.getMinOrderAmount());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setUserId(voucherUser.getUser().getId());
        dto.setUserName(voucherUser.getUser().getName());
        dto.setOrderId(voucherUser.getOrder() != null ? voucherUser.getOrder().getId() : null);
        dto.setUsedAt(voucherUser.getUsedAt());
        dto.setIsUsed(voucherUser.getUsedAt() != null);
        dto.setVoucherStartDate(voucher.getStartDate());
        dto.setVoucherEndDate(voucher.getEndDate());
        dto.setIsExpired(isExpired);

        return dto;
    }

    /**
     * Convert entity Voucher → DTO.
     */

    private VoucherDTO mapToDTO(Voucher voucher) {
        VoucherDTO dto = new VoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setDiscountType(voucher.getDiscountType());
        dto.setDiscountValue(voucher.getDiscountValue());
        dto.setMinOrderAmount(voucher.getMinOrderAmount());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setQuantity(voucher.getQuantity());
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setIsActive(voucher.getIsActive());
        dto.setCreatedAt(voucher.getCreatedAt());
        dto.setUpdatedAt(voucher.getUpdatedAt());
        return dto;
    }
    /**
     * Convert VoucherUser → DTO.VoucherUserDTO (để trả về thông tin user gắn với voucher).
     */

    private VoucherDTO.VoucherUserDTO mapToVoucherUserDTO(VoucherUser voucherUser) {
        VoucherDTO.VoucherUserDTO dto = new VoucherDTO.VoucherUserDTO();
        dto.setId(voucherUser.getId());
        dto.setVoucher(mapToDTO(voucherUser.getVoucher()));
        dto.setUserId(voucherUser.getUser().getId());
        dto.setOrderId(voucherUser.getOrder() != null ? voucherUser.getOrder().getId() : null);
        dto.setUsedAt(voucherUser.getUsedAt());
        return dto;
    }
}
