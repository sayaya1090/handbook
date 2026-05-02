package dev.sayaya.handbook.client.domain;

/**
 * 에이전트 실행 완료 정보 값 객체.
 *
 * <p><b>책임:</b> complete 커맨드의 요약(summary), 실행ID(executionId), 아티팩트 정보(artifact)를 운반한다.</p>
 * <p><b>의존관계:</b> <ul><li>없음 (순수 도메인 VO)</li></ul></p>
 */
public class CompleteInfo {
    private final String summary;
    private final String executionId;
    private final ArtifactInfo artifact;

    public CompleteInfo(String summary, String executionId, ArtifactInfo artifact) {
        this.summary = summary;
        this.executionId = executionId;
        this.artifact = artifact;
    }

    public String summary() { return summary; }
    public String executionId() { return executionId; }
    /** null이면 아티팩트 없음 */
    public ArtifactInfo artifact() { return artifact; }
    public boolean hasArtifact() { return artifact != null; }

    /**
     * 아티팩트(실행 결과물) 정보.
     *
     * <p><b>책임:</b> 아티팩트 요약과 변경 목록을 운반한다.</p>
     */
    public static class ArtifactInfo {
        private final String summary;
        private final ChangeEntry[] changes;

        public ArtifactInfo(String summary, ChangeEntry[] changes) {
            this.summary = summary;
            this.changes = changes != null ? changes : new ChangeEntry[0];
        }

        public String summary() { return summary; }
        public ChangeEntry[] changes() { return changes; }
        public int changeCount() { return changes.length; }
    }

    /**
     * 개별 변경 항목.
     *
     * <p><b>책임:</b> 변경의 종류(type), 대상(target), 설명(description)을 운반한다.</p>
     */
    public static class ChangeEntry {
        private final String type;
        private final String target;
        private final String description;

        public ChangeEntry(String type, String target, String description) {
            this.type = type;
            this.target = target;
            this.description = description;
        }

        public String type() { return type; }
        public String target() { return target; }
        public String description() { return description; }
    }
}
