
package com.example.demo.Controller;

import com.example.demo.config.JwtUtil;
import com.example.demo.services.SubAdminManagementService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/admin/subadmins")
public class SubAdminManagementController {

    private final SubAdminManagementService subAdminService;
    private final JwtUtil jwtUtil;

    public SubAdminManagementController(
            SubAdminManagementService subAdminService,
            JwtUtil jwtUtil) {

        this.subAdminService = subAdminService;
        this.jwtUtil = jwtUtil;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubAdmin(
            @PathVariable Long id,
            @RequestHeader(
                value = "Authorization",
                required = false
            ) String authorizationHeader) {

        // Check Authorization header
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "message", "Superadmin authentication required"
                    ));
        }

        String token = authorizationHeader.substring(7).trim();

        // Only Superadmin can delete Subadmins
        if (!jwtUtil.isSuperAdminToken(token)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                        "success", false,
                        "message", "Only Superadmin can delete Subadmins"
                    ));
        }

        try {

            subAdminService.deleteSubAdmin(id);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Subadmin deleted successfully",
                    "deletedId", id
            ));

        } catch (NoSuchElementException ex) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                        "success", false,
                        "message", ex.getMessage()
                    ));

        } catch (IllegalArgumentException ex) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", ex.getMessage()
                    ));
        }
    }
}
