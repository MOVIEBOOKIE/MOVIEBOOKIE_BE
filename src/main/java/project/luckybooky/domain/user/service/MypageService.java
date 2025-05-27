package project.luckybooky.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.luckybooky.domain.ticket.repository.TicketRepository;
import project.luckybooky.domain.user.converter.MypageConverter;
import project.luckybooky.domain.user.dto.response.MypageResponseDTO;
import project.luckybooky.domain.user.entity.User;
import project.luckybooky.domain.user.repository.UserRepository;
import project.luckybooky.domain.user.util.AuthenticatedUserUtils;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public MypageResponseDTO getMyPage() {

        // 1) 현재 로그인한 사용자의 이메일
        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();

        // 2) DB 조회 (없으면 예외)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        int ticketCount = ticketRepository.countTicketByUserId(user.getId());

        // 3) DTO 변환
        return MypageConverter.toDto(user, ticketCount);
    }
}
