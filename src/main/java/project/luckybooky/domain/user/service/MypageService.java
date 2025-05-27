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
import project.luckybooky.global.apiPayload.error.dto.ErrorCode;
import project.luckybooky.global.apiPayload.error.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public MypageResponseDTO getMyPage() {

        String email = AuthenticatedUserUtils.getAuthenticatedUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        int ticketCount = ticketRepository.countTicketByUserId(user.getId());

        return MypageConverter.toDto(user, ticketCount);
    }
}
