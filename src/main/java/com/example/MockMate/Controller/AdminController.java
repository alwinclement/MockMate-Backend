package com.example.MockMate.Controller;

import com.example.MockMate.DTO.AdminStatsDto;
import com.example.MockMate.DTO.AdminUserDto;
import com.example.MockMate.DTO.UpdateRoleRequest;
import com.example.MockMate.Service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")   // applies to every method in this controller
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<AdminUserDto> updateRole(
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(adminService.updateUserRole(userId, request.getRole()));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}