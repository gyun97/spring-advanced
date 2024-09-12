package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private CommentService commentService;
    @Mock
    private ManagerService managerService;

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        }); // InvalidRequestException로 수정(Lv 2 - 7)

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() { // Lv 3 - 12 : 담당자 확인 조건 추가 후 코드 수정
        // given
        long todoId = 1L;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        Todo todo = new Todo("title", "contents", "sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(managerService.isManager(any(Todo.class), any(User.class))).willReturn(true); // 현재 유저가 해당 todo 담당자 확인 true

        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
        assertEquals(comment.getId(), result.getId());
        assertEquals(comment.getContents(), result.getContents());
        assertEquals(user.getId(), result.getUser().getId());
        assertEquals(user.getEmail(), result.getUser().getEmail());
    }

    // Lv 3 - 12 :  일정 담당자 확인 예외 발생 테스트 코드
    @Test
    void 댓글_저장_시_사용자가_담당자가_아닐_경우_예외_발생() {
        // Given
        long todoId = 1L;
        User user = new User("email", "password", UserRole.USER);
        AuthUser authUser = new AuthUser(user.getId(), user.getEmail(), UserRole.USER);

        Todo todo = new Todo("title", "title", "sunny", user);

        CommentSaveRequest request = new CommentSaveRequest("contents");
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerService.isManager(any(Todo.class), any(User.class))).willReturn(false);

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);

        });
        assertEquals("해당 할일의 담당자가 아니면 댓글을 작성할 권한이 없습니다.", exception.getMessage());
    }
}