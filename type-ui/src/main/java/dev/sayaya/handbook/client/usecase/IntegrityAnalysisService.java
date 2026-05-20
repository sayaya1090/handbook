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

    public record AnalysisResult(boolean valid, String refId, double coverageStart, double coverageEnd, List<ResolutionProposal> proposals) {}

    public enum ProposalType { ADJUST_OWNER, EXTEND_REFERENCE }
    public record ResolutionProposal(ProposalType type, String targetId, double newStart, double newEnd, boolean targetIsOwner) {}

    @Inject
    IntegrityAnalysisService(TypeList typeList) {
        this.typeList = typeList;
    }

    public AnalysisResult analyze(Type owner, String referencedId) {
        return analyze(owner, referencedId, typeList.getValue());
    }

    public AnalysisResult analyze(Type owner, String referencedId, java.util.Collection<Type> typesContext) {
        // 1. 참조 대상 타입의 가용한 전체 연속 구간(Coverage) 계산
        List<Type> refVersions = typesContext.stream()
                .filter(t -> t.id().equals(referencedId))
                .sorted(Comparator.comparingDouble(Type::effectDateTime))
                .collect(Collectors.toList());

        if (refVersions.isEmpty()) {
            return new AnalysisResult(false, referencedId, -1, -1, new ArrayList<>());
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

        if (startOk && endOk) return new AnalysisResult(true, null, 0, 0, null);

        // 2. 안전한 해결책(Proposals) 취합
        List<ResolutionProposal> proposals = new ArrayList<>();
        
        // 제안 A: 소유자 타입의 기간을 참조 대상에 맞춰 축소 (가장 안전)
        double recStart = Math.max(owner.effectDateTime(), coverageStart == -1 ? owner.effectDateTime() : coverageStart);
        double recEnd = Math.min(owner.expireDateTime(), coverageEnd == -1 ? owner.expireDateTime() : coverageEnd);
        
        if (isSafeToAdjustOwner(owner, recStart, recEnd, typesContext)) {
            proposals.add(new ResolutionProposal(
                ProposalType.ADJUST_OWNER,
                owner.id(),
                recStart, recEnd, true
            ));
        }

        // 제안 B: 참조 대상 타입의 기간을 소유자에 맞춰 확장 (버전 중첩이 없을 때만)
        if (refVersions.size() == 1) { // 단일 버전일 때만 단순 확장 제안 (안전성 확보 용이)
            Type ref = refVersions.get(0);
            if (isSafeToExpandReference(ref, owner.effectDateTime(), owner.expireDateTime(), typesContext)) {
                proposals.add(new ResolutionProposal(
                    ProposalType.EXTEND_REFERENCE,
                    referencedId,
                    owner.effectDateTime(), owner.expireDateTime(), false
                ));
            }
        }
        
        return new AnalysisResult(false, referencedId, coverageStart, coverageEnd, proposals);
    }

    /** 돌연변이(유효기간, 속성 변경 등) 시점에 정방향/역방향 정합성을 미리 교차 검증한다. */
    public List<AnalysisResult> analyzeForMutation(Type mutatedType) {
        return analyzeForMutation(mutatedType, mutatedType);
    }

    public List<AnalysisResult> analyzeForMutation(Type mutatedType, Type originalType) {
        // 1. 역방향 참조 검사 (ID 변경 시 참조 단절 감지)
        List<AnalysisResult> conflicts = new ArrayList<>();
        if (!mutatedType.id().equals(originalType.id())) {
            for (Type other : typeList.getValue()) {
                if (other.attributes() == null) continue;
                java.util.Set<String> refs = new java.util.HashSet<>();
                for (dev.sayaya.handbook.domain.Attribute attr : other.attributes()) {
                    extractReferences(attr.type(), refs);
                }
                if (refs.contains(originalType.id())) {
                    // ID 변경으로 인한 참조 깨짐 충돌
                    conflicts.add(new AnalysisResult(false, originalType.id(), -1, -1, new ArrayList<>()));
                }
            }
        }

        // 2. 정방향/역방향 정합성 검사 (기간 중심)
        List<Type> virtualContext = new ArrayList<>();
        for (Type t : typeList.getValue()) {
            if (!t.key().equals(mutatedType.key())) virtualContext.add(t);
        }
        virtualContext.add(mutatedType);

        if (mutatedType.attributes() != null) {
            java.util.Set<String> refs = new java.util.HashSet<>();
            for (dev.sayaya.handbook.domain.Attribute attr : mutatedType.attributes()) {
                extractReferences(attr.type(), refs);
            }
            for (String refId : refs) {
                AnalysisResult res = analyze(mutatedType, refId, virtualContext);
                if (!res.valid()) conflicts.add(res);
            }
        }

        for (Type other : virtualContext) {
            if (other.key().equals(mutatedType.key()) || other.attributes() == null) continue;
            
            java.util.Set<String> otherRefs = new java.util.HashSet<>();
            for (dev.sayaya.handbook.domain.Attribute attr : other.attributes()) {
                extractReferences(attr.type(), otherRefs);
            }
            if (otherRefs.contains(mutatedType.id())) {
                AnalysisResult res = analyze(other, mutatedType.id(), virtualContext);
                if (!res.valid()) conflicts.add(res);
            }
        }
        return conflicts;
    }

    public static void extractReferences(dev.sayaya.handbook.domain.AttributeType attrType, java.util.Set<String> refs) {
        if (attrType == null) return;
        if ("document".equals(attrType.type()) && attrType.referencedType() != null) {
            refs.add(attrType.referencedType());
        }
        extractReferences(attrType.elementType(), refs);
        extractReferences(attrType.keyType(), refs);
        extractReferences(attrType.valueType(), refs);
    }

    /** 소유자 타입의 기간을 변경해도 자식 타입들이 고립되지 않는지 검사 */
    private boolean isSafeToAdjustOwner(Type owner, double newStart, double newEnd, java.util.Collection<Type> context) {
        return context.stream()
                .filter(t -> owner.id().equals(t.parent()))
                .allMatch(child -> child.effectDateTime() >= newStart - 0.1 && child.expireDateTime() <= newEnd + 0.1);
    }

    /** 참조 대상의 기간을 확장해도 다른 버전과 겹치지 않는지 검사 */
    private boolean isSafeToExpandReference(Type ref, double targetStart, double targetEnd, java.util.Collection<Type> context) {
        return context.stream()
                .filter(t -> t.id().equals(ref.id()) && !t.version().equals(ref.version()))
                .noneMatch(other -> targetStart < other.expireDateTime() && targetEnd > other.effectDateTime());
    }

    /** 타입 삭제 시점에 다른 타입들이 이 타입을 참조하고 있는지 검사한다. */
    public List<AnalysisResult> analyzeForDeletion(Type typeToDelete) {
        List<AnalysisResult> conflicts = new ArrayList<>();
        for (Type other : typeList.getValue()) {
            if (other.key().equals(typeToDelete.key()) || other.attributes() == null) continue;
            
            java.util.Set<String> refs = new java.util.HashSet<>();
            for (dev.sayaya.handbook.domain.Attribute attr : other.attributes()) {
                extractReferences(attr.type(), refs);
            }
            if (refs.contains(typeToDelete.id())) {
                conflicts.add(new AnalysisResult(false, typeToDelete.id(), -1, -1, new ArrayList<>()));
            }
        }
        return conflicts;
    }
}
