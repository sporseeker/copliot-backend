package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.WithdrawalRequestDto;
import com.spotseeker.copliot.dto.WithdrawalResponseDto;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.*;
import com.spotseeker.copliot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final EventRepository eventRepository;
    private final PartnerRepository partnerRepository;
    private final TicketPackageRepository ticketPackageRepository;

    @Transactional
    public WithdrawalResponseDto requestWithdrawal(Long userId, Long eventId, WithdrawalRequestDto request) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Calculate available funds
        List<TicketPackage> packages = ticketPackageRepository.findByEvent(event);
        BigDecimal totalRevenue = packages.stream()
                .map(pkg -> pkg.getPrice().multiply(BigDecimal.valueOf(pkg.getSoldCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Withdrawal> existingWithdrawals = withdrawalRepository.findByEvent(event);
        BigDecimal totalWithdrawn = existingWithdrawals.stream()
                .filter(w -> w.getStatus() != Withdrawal.WithdrawalStatus.REJECTED)
                .map(Withdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal availableFunds = totalRevenue.subtract(totalWithdrawn);

        if (request.getAmount().compareTo(availableFunds) > 0) {
            throw new IllegalStateException("Insufficient funds for withdrawal");
        }

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setEvent(event);
        withdrawal.setPartner(partner);
        withdrawal.setAmount(request.getAmount());
        withdrawal.setNote(request.getNote());
        withdrawal.setStatus(Withdrawal.WithdrawalStatus.PENDING);

        if (request.getBankDetails() != null) {
            withdrawal.setBankName(request.getBankDetails().getBankName());
            withdrawal.setAccountNumber(request.getBankDetails().getAccountNumber());
            withdrawal.setAccountName(request.getBankDetails().getAccountName());
        } else {
            // Use partner's default bank details
            withdrawal.setBankName(partner.getBankName());
            withdrawal.setAccountNumber(partner.getAccountNumber());
            withdrawal.setAccountName(partner.getAccountHolderName());
        }

        withdrawal = withdrawalRepository.save(withdrawal);

        return WithdrawalResponseDto.builder()
                .withdrawalId(withdrawal.getId().toString())
                .status(withdrawal.getStatus().name().toLowerCase())
                .processedAt(LocalDateTime.now().toString())
                .build();
    }
}
