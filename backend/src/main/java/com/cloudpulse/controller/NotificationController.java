package com.cloudpulse.controller;

import com.cloudpulse.dto.ApiResponse;
import com.cloudpulse.dto.NotificationDto;
import com.cloudpulse.service.AlertService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final AlertService alertService;

    public NotificationController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationDto>>> getNotifications(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationDto> notifications = alertService.getNotifications(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Notifications retrieved", notifications));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getRecentNotifications() {
        List<NotificationDto> list = alertService.getRecentNotifications();
        return ResponseEntity.ok(ApiResponse.ok("Recent notifications retrieved", list));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        long count = alertService.getUnreadNotificationCount();
        return ResponseEntity.ok(ApiResponse.ok("Unread count", Map.of("unreadCount", count)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable String id) {
        alertService.markNotificationAsRead(id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        alertService.markAllNotificationsAsRead();
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}
