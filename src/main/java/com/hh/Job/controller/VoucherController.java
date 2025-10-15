package com.hh.Job.controller;


import com.hh.Job.domain.Voucher;
import com.hh.Job.domain.VoucherUser;
import com.hh.Job.domain.response.ResultPaginationDTO;
import com.hh.Job.domain.response.voucher.VoucherDTO;
import com.hh.Job.service.VoucherService;
import com.hh.Job.util.annotation.APImessage;
import com.hh.Job.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class VoucherController {

    private VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    // 🟢 [POST] /api/v1/vouchers
    // 👉 Tạo mới một voucher (admin hoặc nhân viên quản lý có thể sử dụng)
    @PostMapping("/vouchers")
    @APImessage("create a vouchers")
    public ResponseEntity<VoucherDTO> createVoucher(@RequestBody VoucherDTO.CreateVoucherDTO dto) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(voucherService.createVoucher(dto));
    }

    // 🟡 [PUT] /api/v1/vouchers/{id}
    // 👉 Cập nhật thông tin một voucher theo ID (số lượng, ngày bắt đầu/kết thúc, mô tả...)
    @PutMapping("/vouchers/{id}")
    @APImessage("update a vouchers")
    public ResponseEntity<VoucherDTO> updateVoucher(
            @PathVariable("id") Long id,
            @RequestBody VoucherDTO.UpdateVoucherDTO dto)
            throws IdInvalidException {
        return ResponseEntity.ok(voucherService.updateVoucher(id, dto));
    }

    // 🔵 [GET] /api/v1/vouchers/{id}
    // 👉 Lấy thông tin chi tiết của một voucher theo ID
    @GetMapping("/vouchers/{id}")
    @APImessage("fetch user byid")
    public ResponseEntity<Optional<Voucher>> fetchVoucherById(@PathVariable("id") Long id)
            throws IdInvalidException {
        if(id == null) {
            throw new IdInvalidException("id is null");
        }
        return ResponseEntity.ok().body(voucherService.fetchVoucherById(id));
    }


    // 🔵 [GET] /api/v1/vouchers
    // 👉 Lấy danh sách tất cả các voucher (có thể lọc, phân trang)
    @GetMapping("/vouchers")
    @APImessage("fetch all voucher")
    public ResponseEntity<ResultPaginationDTO> fetchAllVouchers(
            @Filter Specification<Voucher> spec,
            Pageable pageable
            ) {
        ResultPaginationDTO result = voucherService.fetchAllVoucher(spec, pageable);
        return  ResponseEntity.ok(result);
    }


    // 🔴 [DELETE] /api/v1/vouchers/{id}
    // 👉 Xóa voucher theo ID (chỉ xóa nếu voucher chưa được sử dụng)
    @DeleteMapping("/vouchers/{id}")
    @APImessage("Delete voucher successfully")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) throws IdInvalidException {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok().build();
    }

    // User voucher


    //🟢 [POST] /api/v1/vouchers/{voucherId}/assign/{userId}
    // 👉 Gán voucher cho một user cụ thể (ví dụ: tặng voucher cho người dùng)
    @PostMapping("/vouchers/{voucherId}/assign/{userId}")
    @APImessage("voucher user use")
    public ResponseEntity<VoucherDTO.VoucherUserDTO> assignVoucherToUser(
            @PathVariable Long voucherId,
            @PathVariable Long userId)throws IdInvalidException {
        return ResponseEntity.ok(voucherService.assignVoucherToUser(voucherId, userId));
    }

    // 🔵 [GET] /api/v1/vouchers/usage-history
    // 👉 Lấy lịch sử sử dụng voucher của người dùng (hoặc toàn hệ thống)
    @GetMapping("/vouchers/usage-history")
    @APImessage("Fetch voucher usage history")
    public ResponseEntity<ResultPaginationDTO> getUsageHistory(
            @Filter Specification<VoucherUser> spec,
            Pageable pageable) {
        ResultPaginationDTO result = voucherService.getUsageHistory(spec, pageable);
        return ResponseEntity.ok(result);
    }

    // 🔵 [GET] /api/v1/vouchers/active-list
    // 👉 Lấy danh sách tất cả voucher đang hoạt động (isActive = true)
    @GetMapping("/vouchers/active-list")
    @APImessage("Fetch all active vouchers")
    public ResponseEntity<List<VoucherDTO>> getActiveVouchers() {
        List<VoucherDTO> activeVouchers = voucherService.getActiveVouchers();
        return ResponseEntity.ok(activeVouchers);
    }

    // 🔵 [GET] /api/v1/vouchers/search?code=ABC123
    // 👉 Tìm voucher theo mã code
    @GetMapping("/vouchers/search")
    @APImessage("Search voucher by code")
    public ResponseEntity<VoucherDTO> getVoucherByCode(@RequestParam("code") String code)
            throws IdInvalidException {
        return ResponseEntity.ok(voucherService.getVoucherByCode(code));
    }

    // 🔴 [PUT] /api/v1/vouchers/deactivate/{id}
    // 👉 Vô hiệu hóa (tắt) một voucher — người dùng không thể áp dụng khi đặt hàng
    @PutMapping("/vouchers/deactivate/{id}")
    @APImessage("Deactivate voucher successfully")
    public ResponseEntity<Void> deactivateVoucher(@PathVariable Long id) throws IdInvalidException {
        voucherService.deactivateVoucher(id);
        return ResponseEntity.ok().build();
    }

    // 🟢 [PUT] /api/v1/vouchers/activate/{id}
    // 👉 Kích hoạt lại voucher (bật lại voucher đã bị tắt)
    @PutMapping("/vouchers/activate/{id}")
    @APImessage("Voucher activated successfully")
    public ResponseEntity<Void> activateVoucher(@PathVariable Long id) throws IdInvalidException {
        voucherService.updateVoucherStatus(id, true);
        return ResponseEntity.ok().build();
    }

}
