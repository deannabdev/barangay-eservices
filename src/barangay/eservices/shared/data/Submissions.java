package barangay.eservices;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** Data records for document request and report submissions stored in the local database. */
final class Submissions {

    private Submissions() {}

    enum SubmissionType {
        DOCUMENT_REQUEST("Document Request"),
        REPORT("Report");

        private final String label;

        SubmissionType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    enum SubmissionStatus {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        REJECTED("Rejected");

        private final String label;

        SubmissionStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /** A submission record for a document request or report. */
    static final class SubmissionRecord {
        final String kind;           // "REPORT" or "DOCUMENT"
        final String title;
        final String reference;
        final SubmissionStatus status;
        final LocalDateTime submittedAt;
        final String submittedBy;
        final String category;       // nullable for documents
        final String location;       // nullable for documents
        final String details;        // nullable for documents
        final String photoPath;      // nullable for documents

        SubmissionRecord(String kind, String title, String reference, SubmissionStatus status,
                         LocalDateTime submittedAt, String submittedBy, String category,
                         String location, String details, String photoPath) {
            this.kind = kind;
            this.title = title;
            this.reference = reference;
            this.status = status;
            this.submittedAt = submittedAt;
            this.submittedBy = submittedBy;
            this.category = category;
            this.location = location;
            this.details = details;
            this.photoPath = photoPath;
        }

        public String getReference() {
            return reference;
        }

        public SubmissionStatus getStatus() {
            return status;
        }

        public long getSubmittedAt() {
            return submittedAt.toInstant(ZoneOffset.UTC).toEpochMilli();
        }

        public String getSubmittedBy() {
            return submittedBy;
        }

        public String getSubmittedAtFormatted() {
            return submittedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
        }
    }

    /** Generates a unique submission reference (e.g. BS-A1B2C3D4). */
    static String generateReference() {
        return "BS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
