package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.domain.Type;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 타입 간 참조 무결성 및 유효기간 일관성을 검증하고 '안전한' 보정안을 제안하는 서비스.
 */
@Singleton
public class IntegrityAnalysisService {
    private final TypeList typeList;

    public record AnalysisResult(boolean valid, String message, List<ResolutionProposal> proposals) {}

    public record ResolutionProposal(String title, String description, double newStart, double newEnd, boolean targetIsOwner) {}

    @Inject
    IntegrityAnalysisService(TypeList typeList) {
        this.typeList = typeList;
    }

    public AnalysisResult analyze(Type owner, String referencedId) {
        // 1. 참조 대상 타입의 가용한 전체 연속 구간(Coverage) 계산
        List<Type> refVersions = typeList.getValue().stream()
                .filter(t -> t.id().equals(referencedId))
                .sorted(Comparator.comparingDouble(Type::effectDateTime))
                .collect(Collectors.toList());

        if (refVersions.isEmpty()) {
            return new AnalysisResult(false, "Referenced type '" + referencedId + "' does not exist.", new ArrayList<>());
        }

        double coverageStart = -1;
        double coverageEnd = -1;
        for (Type v : refVersions) {
            if (v.expireDateTime() <= owner.effectDateTime() || v.effectDateTime() >= owner.expireDateTime()) continue;
            if (coverageStart == -1) {
                coverageStart = v.effectDateTime();
                coverageEnd = v.expireDateTime();
            } else if (Math.abs(v.effectDateTime() - coverageEnd) < 1.0) {
                coverageEnd = v.expireDateTime();
            }
        }

        boolean startOk = coverageStart != -1 && coverageStart <= owner.effectDateTime() + 0.1;
        boolean endOk = coverageEnd != -1 && coverageEnd >= owner.expireDateTime() - 0.1;

        if (startOk && endOk) return new AnalysisResult(true, null, null);

        // 2. 안전한 해결책(Proposals) 취합
        List<ResolutionProposal> proposals = new ArrayList<>();
        
        // 제안 A: 소유자 타입의 기간을 참조 대상에 맞춰 축소 (가장 안전)
        double recStart = Math.max(owner.effectDateTime(), coverageStart == -1 ? owner.effectDateTime() : coverageStart);
        double recEnd = Math.min(owner.expireDateTime(), coverageEnd == -1 ? owner.expireDateTime() : coverageEnd);
        
        if (isSafeToAdjustOwner(owner, recStart, recEnd)) {
            proposals.add(new ResolutionProposal(
                "Adjust Owner Period",
                "Change '" + owner.id() + "' to [" + DateFormatter.format(recStart) + " ~ " + DateFormatter.format(recEnd) + "] to match reference.",
                recStart, recEnd, true
            ));
        }

        // 제안 B: 참조 대상 타입의 기간을 소유자에 맞춰 확장 (버전 중첩이 없을 때만)
        if (refVersions.size() == 1) { // 단일 버전일 때만 단순 확장 제안 (안전성 확보 용이)
            Type ref = refVersions.get(0);
            if (isSafeToExpandReference(ref, owner.effectDateTime(), owner.expireDateTime())) {
                proposals.add(new ResolutionProposal(
                    "Extend Referenced Type",
                    "Expand '" + referencedId + "' to [" + DateFormatter.format(owner.effectDateTime()) + " ~ " + DateFormatter.format(owner.expireDateTime()) + "].",
                    owner.effectDateTime(), owner.expireDateTime(), false
                ));
            }
        }

        String msg = "The referenced type '" + referencedId + "' is only available from " 
                + DateFormatter.format(coverageStart) + " ~ " + DateFormatter.format(coverageEnd) + ".";
        
        return new AnalysisResult(false, msg, proposals);
    }

    /** 소유자 타입의 기간을 변경해도 자식 타입들이 고립되지 않는지 검사 */
    private boolean isSafeToAdjustOwner(Type owner, double newStart, double newEnd) {
        return typeList.getValue().stream()
                .filter(t -> owner.id().equals(t.parent()))
                .allMatch(child -> child.effectDateTime() >= newStart - 0.1 && child.expireDateTime() <= newEnd + 0.1);
    }

    /** 참조 대상의 기간을 확장해도 다른 버전과 겹치지 않는지 검사 */
    private boolean isSafeToExpandReference(Type ref, double targetStart, double targetEnd) {
        return typeList.getValue().stream()
                .filter(t -> t.id().equals(ref.id()) && !t.version().equals(ref.version()))
                .noneMatch(other -> targetStart < other.expireDateTime() && targetEnd > other.effectDateTime());
    }
}
