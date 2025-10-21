package com.spotseeker.copliot.service;

import com.spotseeker.copliot.dto.*;
import com.spotseeker.copliot.exception.ResourceNotFoundException;
import com.spotseeker.copliot.model.*;
import com.spotseeker.copliot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventAnalyticsService {

    private final EventRepository eventRepository;
    private final PartnerRepository partnerRepository;
    private final TicketRepository ticketRepository;
    private final TicketPackageRepository ticketPackageRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final WithdrawalRepository withdrawalRepository;

    public EventOverviewDto getEventOverview(Long userId, Long eventId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Long totalTickets = ticketRepository.countByEventId(eventId);
        Long usedTickets = ticketRepository.countUsedByEventId(eventId);

        List<TicketPackage> packages = ticketPackageRepository.findByEvent(event);
        BigDecimal totalRevenue = packages.stream()
                .map(pkg -> pkg.getPrice().multiply(BigDecimal.valueOf(pkg.getSoldCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate attendance breakdown (simplified)
        EventOverviewDto.AttendanceBreakdown breakdown = EventOverviewDto.AttendanceBreakdown.builder()
                .onlineTickets(totalTickets.intValue())
                .spotseekerInvites(0)
                .specialInvites(0)
                .build();

        // Calculate sales summary
        Integer totalReleaseCount = packages.stream()
                .mapToInt(TicketPackage::getReleaseCount)
                .sum();

        Double completionPercentage = totalReleaseCount > 0
                ? (totalTickets.doubleValue() / totalReleaseCount) * 100
                : 0.0;

        EventOverviewDto.SalesSummary salesSummary = EventOverviewDto.SalesSummary.builder()
                .totalAmount(totalRevenue)
                .ticketsSold(totalTickets.intValue())
                .completionPercentage(completionPercentage)
                .build();

        // Get alerts (simplified)
        List<EventOverviewDto.AlertDto> alerts = new ArrayList<>();

        return EventOverviewDto.builder()
                .totalAttendees(usedTickets.intValue())
                .totalTicketsSold(totalTickets.intValue())
                .totalRevenue(totalRevenue)
                .alerts(alerts)
                .attendanceBreakdown(breakdown)
                .salesSummary(salesSummary)
                .build();
    }

    public LiveStatsDto getLiveStats(Long userId, Long eventId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Long totalTickets = ticketRepository.countByEventId(eventId);
        Long insideCount = ticketRepository.countUsedByEventId(eventId);
        Long toComeCount = totalTickets - insideCount;

        // Get attendance by package
        List<TicketPackage> packages = ticketPackageRepository.findByEvent(event);
        List<LiveStatsDto.PackageAttendance> attendanceByPackage = packages.stream()
                .map(pkg -> {
                    Long pkgUsedCount = ticketRepository.countUsedByPackageId(pkg.getId());
                    Long pkgTotalCount = (long) pkg.getSoldCount();
                    Double completion = pkgTotalCount > 0
                            ? (pkgUsedCount.doubleValue() / pkgTotalCount) * 100
                            : 0.0;

                    return LiveStatsDto.PackageAttendance.builder()
                            .packageId(pkg.getId().toString())
                            .packageName(pkg.getName())
                            .totalTickets(pkg.getSoldCount())
                            .insideCount(pkgUsedCount.intValue())
                            .toComeCount((int)(pkgTotalCount - pkgUsedCount))
                            .completionPercentage(completion)
                            .build();
                })
                .collect(Collectors.toList());

        // Get fraud alerts
        List<FraudAlert> fraudAlerts = fraudAlertRepository.findByEventAndResolvedFalse(event);
        List<LiveStatsDto.FraudulentAlert> fraudAlertDtos = fraudAlerts.stream()
                .collect(Collectors.groupingBy(FraudAlert::getType, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> LiveStatsDto.FraudulentAlert.builder()
                        .type(entry.getKey().name().toLowerCase())
                        .title(formatAlertTitle(entry.getKey()))
                        .description(formatAlertDescription(entry.getKey()))
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());

        // Simplified demographics
        LiveStatsDto.AudienceDemographics demographics = LiveStatsDto.AudienceDemographics.builder()
                .genderBreakdown(LiveStatsDto.GenderBreakdown.builder()
                        .male(0)
                        .female(0)
                        .build())
                .ageDistribution(LiveStatsDto.AgeDistribution.builder()
                        .age18_25(0)
                        .age26_35(0)
                        .age36_45(0)
                        .age46_55(0)
                        .age56Plus(0)
                        .build())
                .build();

        return LiveStatsDto.builder()
                .totalAttendees(totalTickets.intValue())
                .insideCount(insideCount.intValue())
                .toComeCount(toComeCount.intValue())
                .attendanceByPackage(attendanceByPackage)
                .scanInsights(LiveStatsDto.ScanInsights.builder()
                        .timelineData(new ArrayList<>())
                        .build())
                .fraudulentAlerts(fraudAlertDtos)
                .audienceDemographics(demographics)
                .build();
    }

    public FinanceSalesDto getFinanceSales(Long userId, Long eventId, String period) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        List<TicketPackage> packages = ticketPackageRepository.findByEvent(event);

        BigDecimal totalRevenue = packages.stream()
                .map(pkg -> pkg.getPrice().multiply(BigDecimal.valueOf(pkg.getSoldCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sales by package
        List<FinanceSalesDto.SalesByPackage> salesByPackage = packages.stream()
                .map(pkg -> {
                    BigDecimal revenue = pkg.getPrice().multiply(BigDecimal.valueOf(pkg.getSoldCount()));
                    Double percentage = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                            ? revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                            : 0.0;

                    return FinanceSalesDto.SalesByPackage.builder()
                            .packageName(pkg.getName())
                            .revenue(revenue)
                            .percentage(percentage)
                            .color(getColorForPackage(pkg.getId().intValue()))
                            .build();
                })
                .collect(Collectors.toList());

        // Package details
        List<FinanceSalesDto.PackageDetail> packageDetails = packages.stream()
                .map(pkg -> FinanceSalesDto.PackageDetail.builder()
                        .packageId(pkg.getId().toString())
                        .packageName(pkg.getName())
                        .totalRevenue(pkg.getPrice().multiply(BigDecimal.valueOf(pkg.getSoldCount())))
                        .ticketsSold(pkg.getSoldCount())
                        .totalTickets(pkg.getReleaseCount())
                        .ticketPrice(pkg.getPrice())
                        .startDate(pkg.getStartDateTime().toString())
                        .endDate(pkg.getEndDateTime().toString())
                        .countdown(null)
                        .build())
                .collect(Collectors.toList());

        return FinanceSalesDto.builder()
                .totalRevenue(totalRevenue)
                .salesByPackage(salesByPackage)
                .packageDetails(packageDetails)
                .build();
    }

    public FinanceBreakdownDto getFinanceBreakdown(Long userId, Long eventId) {
        Partner partner = partnerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));

        Event event = eventRepository.findByIdAndPartner(eventId, partner)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        List<TicketPackage> packages = ticketPackageRepository.findByEvent(event);
        BigDecimal totalRevenue = packages.stream()
                .map(pkg -> pkg.getPrice().multiply(BigDecimal.valueOf(pkg.getSoldCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Withdrawal> withdrawals = withdrawalRepository.findByEvent(event);
        BigDecimal totalWithdrawals = withdrawals.stream()
                .filter(w -> w.getStatus() == Withdrawal.WithdrawalStatus.TRANSFERRED)
                .map(Withdrawal::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal availableFunds = totalRevenue.subtract(totalWithdrawals);

        List<FinanceBreakdownDto.WithdrawalDto> withdrawalDtos = withdrawals.stream()
                .map(w -> FinanceBreakdownDto.WithdrawalDto.builder()
                        .id(w.getId().toString())
                        .purpose(w.getNote())
                        .amount(w.getAmount())
                        .date(w.getCreatedAt().toString())
                        .status(w.getStatus().name().toLowerCase())
                        .build())
                .collect(Collectors.toList());

        return FinanceBreakdownDto.builder()
                .totalRevenue(totalRevenue)
                .availableFunds(availableFunds)
                .totalWithdrawals(totalWithdrawals)
                .revenueTimeline(new ArrayList<>())
                .withdrawals(withdrawalDtos)
                .build();
    }

    private String formatAlertTitle(FraudAlert.AlertType type) {
        return switch (type) {
            case MULTIPLE_ENTRY -> "Multiple Entry Attempt";
            case FAKE_QR -> "Fake QR Code Detected";
            case SUSPICIOUS_DEVICE -> "Suspicious Device Activity";
        };
    }

    private String formatAlertDescription(FraudAlert.AlertType type) {
        return switch (type) {
            case MULTIPLE_ENTRY -> "Same ticket scanned multiple times";
            case FAKE_QR -> "Invalid or tampered QR code";
            case SUSPICIOUS_DEVICE -> "Unusual scanning patterns detected";
        };
    }

    private String getColorForPackage(int index) {
        String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40"};
        return colors[index % colors.length];
    }
}
