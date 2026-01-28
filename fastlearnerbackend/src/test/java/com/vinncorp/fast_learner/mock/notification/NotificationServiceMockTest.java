package com.vinncorp.fast_learner.mock.notification;

import com.vinncorp.fast_learner.exception.BadRequestException;
import com.vinncorp.fast_learner.exception.EntityNotFoundException;
import com.vinncorp.fast_learner.exception.InternalServerException;
import com.vinncorp.fast_learner.mock.user.UserTestData;
import com.vinncorp.fast_learner.models.notification.Notification;
import com.vinncorp.fast_learner.models.user.User;
import com.vinncorp.fast_learner.repositories.notification.NotificationRepository;
import com.vinncorp.fast_learner.repositories.user.UserRepository;
import com.vinncorp.fast_learner.response.notification.NotificationWithPaginationResponse;
import com.vinncorp.fast_learner.services.notification.NotificationService;
import com.vinncorp.fast_learner.services.user.IUserService;
import com.vinncorp.fast_learner.util.Message;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceMockTest {

    private static String EMAIL = "qasim@mailinator.com";
    static String UNIQUE_ID = "unique123";
    static Long receiverId = 1L;
    @InjectMocks
    NotificationService notificationService;
    @Mock
    IUserService userService;
    @Mock
    NotificationRepository notificationRepository;
    @Mock
    UserRepository repository;

    private User receiver;
    List<Long> notificationIds;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        notificationService = new NotificationService(notificationRepository, userService);
        receiver = new User();
        receiver.setId(1L);
        receiver.setEmail(EMAIL);
        notificationIds = List.of(1L, 2L, 3L);

    }

    @Test
    @DisplayName("Should register and send notifications successfully when user exists")
    void register_Success() throws EntityNotFoundException, IOException {

        List<Notification> notifications = List.of(
                Notification.builder().id(1L).content("Notification 1").isRead(false).receiverIds(Collections.singletonList(receiverId)).build(),
                Notification.builder().id(2L).content("Notification 2").isRead(false).receiverIds(Collections.singletonList(receiverId)).build()
        );

        when(notificationRepository.findByReceiverId(receiverId)).thenReturn(notifications);

        when(notificationRepository.totalUnreadNotifications(receiverId)).thenReturn(2L);
        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        SseEmitter emitter = notificationService.register(EMAIL, UNIQUE_ID);

        assertNotNull(emitter);
        assertFalse(emitters.containsKey(UserTestData.userData().getId() + "_" + UNIQUE_ID));
        verify(userService, times(1)).findByEmail(EMAIL);
        verify(notificationRepository, times(1)).findByReceiverId(receiverId);
        verify(notificationRepository, times(1)).totalUnreadNotifications(receiverId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found by email")
    void register_UserNotFound() throws EntityNotFoundException {
        when(userService.findByEmail(EMAIL)).thenReturn(null);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                notificationService.register(EMAIL, UNIQUE_ID)
        );

        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    @DisplayName("Should remove emitter on timeout")
    void register_EmitterTimeout() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        SseEmitter emitter = notificationService.register(EMAIL, UNIQUE_ID);
        assertNotNull(emitter);

        emitter.onTimeout(() -> {
            emitters.remove(receiver.getId() + "_" + UNIQUE_ID);
        });

        assertFalse(emitters.containsKey(UserTestData.userData().getId() + "_" + UNIQUE_ID));
    }

    @Test
    @DisplayName("Fetch Top 5 Notifications - Valid Receiver ID")
    void fetchTop5NotificationsByUser_ValidReceiverId() {

        List<Notification> mockNotifications = List.of(
                new Notification().builder().id(1L).content("Notification 1").isRead(false).receiverIds(Collections.singletonList(receiverId)).build(),
                new Notification().builder().id(2L).content("Notification 2").isRead(false).receiverIds(Collections.singletonList(receiverId)).build()

        );
        Long mockUnreadCount = 3L;

        when(notificationRepository.findByReceiverId(receiverId)).thenReturn(mockNotifications);
        when(notificationRepository.totalUnreadNotifications(receiverId)).thenReturn(mockUnreadCount);

        List<Notification> result = notificationService.fetchTop5NotificationsByUser(receiverId);

        assertNotNull(result);
        assertEquals(mockNotifications.size(), result.size());
        result.forEach(notification -> assertEquals(mockUnreadCount, notification.getTotalUnread()));
        verify(notificationRepository, times(1)).findByReceiverId(receiverId);
        verify(notificationRepository, times(1)).totalUnreadNotifications(receiverId);
    }

    @Test
    @DisplayName("Fetch Top 5 Notifications - No Notifications Found")
    void fetchTop5NotificationsByUser_NoNotificationsFound() {

        List<Notification> emptyList = Collections.emptyList();
        Long mockUnreadCount = 0L;

        when(notificationRepository.findByReceiverId(receiverId)).thenReturn(emptyList);
        when(notificationRepository.totalUnreadNotifications(receiverId)).thenReturn(mockUnreadCount);

        List<Notification> result = notificationService.fetchTop5NotificationsByUser(receiverId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository, times(1)).findByReceiverId(receiverId);
        verify(notificationRepository, times(1)).totalUnreadNotifications(receiverId);
    }

    @Test
    @DisplayName("Fetch Top 5 Notifications - Invalid Receiver ID")
    void fetchTop5NotificationsByUser_InvalidReceiverId() {

        Long invalidReceiverId = -1L;
        when(notificationRepository.findByReceiverId(invalidReceiverId)).thenReturn(Collections.emptyList());
        when(notificationRepository.totalUnreadNotifications(invalidReceiverId)).thenReturn(0L);

        List<Notification> result = notificationService.fetchTop5NotificationsByUser(invalidReceiverId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository, times(1)).findByReceiverId(invalidReceiverId);
        verify(notificationRepository, times(1)).totalUnreadNotifications(invalidReceiverId);
    }

    @Test
    @DisplayName("Fetch Top 5 Notifications - Exception Handling")
    void fetchTop5NotificationsByUser_ExceptionHandling() {

        when(notificationRepository.findByReceiverId(receiverId)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> notificationService.fetchTop5NotificationsByUser(receiverId));
        verify(notificationRepository, times(1)).findByReceiverId(receiverId);
    }

    @Test
    @DisplayName("Should mark notifications as read successfully")
    void shouldMarkNotificationsAsReadSuccessfully() throws InternalServerException {

        String email = EMAIL;

        assertDoesNotThrow(() -> notificationService.readANotification(notificationIds, email));
        verify(notificationRepository, times(1)).readANotification(notificationIds);
    }

    @Test
    @DisplayName("Should throw InternalServerException when marking notifications as read fails")
    void shouldThrowInternalServerExceptionWhenMarkingNotificationsAsReadFails() {

        doThrow(new RuntimeException("Database error")).when(notificationRepository).readANotification(notificationIds);

        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                notificationService.readANotification(notificationIds, EMAIL)
        );

        assertEquals("A notification read functionality not performed.", exception.getMessage());
        verify(notificationRepository, times(1)).readANotification(notificationIds);
    }

    @Test
    @DisplayName("Should successfully mark all notifications as read when user exists")
    void readAllNotificationByUser_Success() throws EntityNotFoundException, BadRequestException, InternalServerException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        doNothing().when(notificationRepository).readAllNotificationByReceiverId(UserTestData.userData().getId());

        assertDoesNotThrow(() -> notificationService.readAllNotificationByUser(EMAIL));

        verify(notificationRepository, times(1)).readAllNotificationByReceiverId(UserTestData.userData().getId());
    }

    @Test
    @DisplayName("Should throw InternalServerException when exception occurs in repo")
    void readAllNotificationByUser_InternalServerException() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        doThrow(new RuntimeException("Database error")).when(notificationRepository).readAllNotificationByReceiverId(UserTestData.userData().getId());

        InternalServerException exception = assertThrows(InternalServerException.class, () ->
                notificationService.readAllNotificationByUser(EMAIL)
        );

        assertEquals("All notification read functionality not performed.", exception.getMessage());
        verify(notificationRepository, times(1)).readAllNotificationByReceiverId(UserTestData.userData().getId());
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void readAllNotificationByUser_NullEmail() throws EntityNotFoundException, BadRequestException {
        assertThrows(BadRequestException.class, () ->
                notificationService.readAllNotificationByUser(null)
        );

        verify(userService, never()).findByEmail(anyString());
        verify(notificationRepository, never()).readAllNotificationByReceiverId(anyLong());
    }

    @Test
    @DisplayName("Should fetch notifications with valid pagination parameters")
    void fetchNotificationsWithValidPagination() throws EntityNotFoundException, BadRequestException {
        // Arrange
        when(userService.findByEmail(EMAIL)).thenReturn(receiver);
        Page<Notification> notifications = new PageImpl<>(Collections.singletonList(new Notification()));
        when(notificationRepository.findByReceiverIdWithPagination(receiver.getId(), PageRequest.of(0, 10)))
                .thenReturn(notifications);

        Message<NotificationWithPaginationResponse> response = notificationService.fetchAllNotificationByPagination(EMAIL, 0, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Fetching notification is successful.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getNotificationList().size());
        assertEquals(1, response.getData().getNoOfElements());
        assertEquals(1, response.getData().getNoOfPages());
    }

    @Test
    @DisplayName("Should fetch notifications when only one page exists")
    void fetchNotificationsWhenOnlyOnePageExists() throws EntityNotFoundException, BadRequestException {

        when(userService.findByEmail(EMAIL)).thenReturn(receiver);
        Page<Notification> notifications = new PageImpl<>(Collections.singletonList(new Notification()));
        when(notificationRepository.findByReceiverIdWithPagination(receiver.getId(), PageRequest.of(0, 1)))
                .thenReturn(notifications);

        Message<NotificationWithPaginationResponse> response = notificationService.fetchAllNotificationByPagination(EMAIL, 0, 1);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Fetching notification is successful.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getNotificationList().size());
        assertEquals(1, response.getData().getNoOfElements());
        assertEquals(1, response.getData().getNoOfPages());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when no notifications found")
    void noNotificationsFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(receiver);
        when(notificationRepository.findByReceiverIdWithPagination(receiver.getId(), PageRequest.of(0, 10)))
                .thenReturn(Page.empty());

        assertThrows(EntityNotFoundException.class, () ->
                        notificationService.fetchAllNotificationByPagination(EMAIL, 0, 10),
                "No notification found for the logged in user."
        );
    }

    @Test
    @DisplayName("Should throw BadRequestException when invalid page number")
    void invalidPageNumber() throws EntityNotFoundException, BadRequestException {

        when(userService.findByEmail(EMAIL)).thenReturn(receiver);

        Page<Notification> notifications = new PageImpl<>(Collections.singletonList(new Notification()));
        when(notificationRepository.findByReceiverIdWithPagination(receiver.getId(), PageRequest.of(0, 10)))
                .thenReturn(notifications);

        assertThrows(BadRequestException.class, () ->
                        notificationService.fetchAllNotificationByPagination(EMAIL, -1, 10),
                "Page no or Page size cannot be negative."
        );
    }

    @Test
    @DisplayName("Should throw BadRequestException when invalid page size")
    void invalidPageSize() throws BadRequestException, EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(receiver);

        Page<Notification> notifications = new PageImpl<>(Collections.singletonList(new Notification()));
        when(notificationRepository.findByReceiverIdWithPagination(receiver.getId(), PageRequest.of(0, 10)))
                .thenReturn(notifications);

        assertThrows(BadRequestException.class, () ->
                        notificationService.fetchAllNotificationByPagination(EMAIL, 0, -10),
                "Page no or Page size cannot be negative."
        );
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user email is invalid")
    void userNotFound() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found."));

        assertThrows(EntityNotFoundException.class, () ->
                        notificationService.fetchAllNotificationByPagination(EMAIL, 0, 10),
                "User not found."
        );
    }

    @Test
    @DisplayName("Should successfully remove emitter when user exists")
    void shouldRemoveEmitterWhenUserExists() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        this.emitters.put(UserTestData.userData().getId() + "_" + UNIQUE_ID, new SseEmitter());

        notificationService.deleteEmitter(EMAIL, UNIQUE_ID);
        this.emitters.remove(receiver.getId() + "_" + UNIQUE_ID);
        assertFalse(this.emitters.containsKey(UserTestData.userData().getId() + "_" + UNIQUE_ID));
        verify(userService, times(1)).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("Should handle case when emitter does not exist but user exists")
    void shouldHandleWhenEmitterDoesNotExistButUserExists() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());

        notificationService.deleteEmitter(EMAIL, UNIQUE_ID);

        verify(userService, times(1)).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user does not exist")
    void shouldThrowEntityNotFoundExceptionWhenUserDoesNotExist() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenThrow(new EntityNotFoundException("User not found."));

        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            notificationService.deleteEmitter(EMAIL, UNIQUE_ID);
        });

        assertEquals("User not found.", thrown.getMessage());
        verify(userService, times(1)).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("Should handle case when uniqueID is null")
    void shouldHandleUniqueIDIsNull() throws EntityNotFoundException {

        when(userService.findByEmail(EMAIL)).thenReturn(UserTestData.userData());
        this.emitters.put(UserTestData.userData().getId() + "_" + null, new SseEmitter());

        notificationService.deleteEmitter(EMAIL, null);
        this.emitters.remove(receiver.getId() + "_" + null);
        assertFalse(this.emitters.containsKey(UserTestData.userData().getId() + "_" + null));
        verify(userService, times(1)).findByEmail(EMAIL);
    }
}
