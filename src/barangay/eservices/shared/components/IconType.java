package components;
import java.awt.*;

public enum IconType {
    DOCUMENT {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.DOCUMENT); }
    },
    REPORT {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_CIRCLE); }
    },
    TRUCK {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.TRUCK); }
    },
    SHOP {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.BUILDING); }
    },
    SOS {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_TRIANGLE); }
    },
    ROAD {
        @Override public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 3, size / 6, size / 3, size * 2 / 3);
            g.fillRect(size / 6, size / 3, size * 2 / 3, size / 3);
        }
    },
    TRASH {
        @Override public void draw(Graphics2D g, int size, Color color) { drawReportIcon(g, size, color, ReportsComponents.ReportIcon.Type.TRASH); }
    },
    LIGHT {
        @Override public void draw(Graphics2D g, int size, Color color) { drawReportIcon(g, size, color, ReportsComponents.ReportIcon.Type.STREETLIGHT_OFF); }
    },
    WATER {
        @Override public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillOval(size / 4, size / 4, size / 2, size / 3);
            g.fillOval(size / 3, size / 3, size / 3, size / 3);
        }
    },
    SHIELD {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.SHIELD); }
    },
    HEALTH {
        @Override public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 3, size / 4, size / 3, size / 2);
            g.fillRect(size / 4, size / 2 - size / 8, size / 2, size / 4);
        }
    },
    VAN {
        @Override public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 4, size / 3, size / 2, size / 3);
            g.fillRect(size / 2, size / 4, size / 3, size / 4);
            g.fillOval(size / 4 + size / 10, size * 2 / 3, size / 6, size / 6);
            g.fillOval(size / 2 + size / 10, size * 2 / 3, size / 6, size / 6);
        }
    },
    AMBULANCE {
        @Override public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 4, size / 3, size / 2, size / 3);
            g.fillRect(size / 2, size / 4, size / 3, size / 4);
            g.fillOval(size / 4 + size / 10, size * 2 / 3, size / 6, size / 6);
            g.fillOval(size / 2 + size / 10, size * 2 / 3, size / 6, size / 6);
            g.fillRect(size / 2 + size / 12, size / 3 - size / 12, size / 8, size / 6);
        }
    },
    PICKUP {
        @Override public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.fillRect(size / 6, size / 3, size * 2 / 3, size / 3);
            g.fillRect(size / 2, size / 4, size / 3, size / 4);
            g.fillOval(size / 4, size * 2 / 3, size / 5, size / 5);
            g.fillOval(size * 3 / 4 - size / 10, size * 2 / 3, size / 5, size / 5);
        }
    },
    PHONE {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.PHONE); }
    },
    ARROW_LEFT {
        @Override public void draw(Graphics2D g, int size, Color color) { drawTransportIcon(g, size, color, TransportComponents.TransportIcon.Type.ARROW_LEFT); }
    },
    WARNING {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_TRIANGLE); }
    },
    MAGNIFIER {
        @Override public void draw(Graphics2D g, int size, Color color) { drawDirectoryIcon(g, size, color, DirectoryComponents.DirectoryIcon.Type.SEARCH); }
    },
    SEARCH {
        @Override public void draw(Graphics2D g, int size, Color color) { drawDirectoryIcon(g, size, color, DirectoryComponents.DirectoryIcon.Type.SEARCH); }
    },
    STORE {
        @Override public void draw(Graphics2D g, int size, Color color) { drawDirectoryIcon(g, size, color, DirectoryComponents.DirectoryIcon.Type.STORE); }
    },
    STAR {
        @Override public void draw(Graphics2D g, int size, Color color) { drawDirectoryIcon(g, size, color, DirectoryComponents.DirectoryIcon.Type.STAR); }
    },
    USERS {
        @Override public void draw(Graphics2D g, int size, Color color) { drawTransportIcon(g, size, color, TransportComponents.TransportIcon.Type.USERS); }
    },
    BOX {
        @Override public void draw(Graphics2D g, int size, Color color) { drawTransportIcon(g, size, color, TransportComponents.TransportIcon.Type.BOX); }
    },
    CIRCLE_X {
        @Override public void draw(Graphics2D g, int size, Color color) { drawTransportIcon(g, size, color, TransportComponents.TransportIcon.Type.X_CIRCLE); }
    },
    PLUS {
        @Override public void draw(Graphics2D g, int size, Color color) { drawReportIcon(g, size, color, ReportsComponents.ReportIcon.Type.PLUS); }
    },
    CHEVRON {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.CHEVRON); }
    },
    BULB {
        @Override public void draw(Graphics2D g, int size, Color color) { LIGHT.draw(g, size, color); }
    },
    TRIANGLE {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_TRIANGLE); }
    },
    DOC {
        @Override public void draw(Graphics2D g, int size, Color color) { DOCUMENT.draw(g, size, color); }
    },
    GAVEL {
        @Override public void draw(Graphics2D g, int size, Color color) { drawReportIcon(g, size, color, ReportsComponents.ReportIcon.Type.GAVEL); }
    },
    PERSON {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.USER); }
    },
    INFO {
        @Override public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.setStroke(new BasicStroke(Math.max(1f, size / 10f)));
            g.drawOval(size / 6, size / 6, size * 2 / 3, size * 2 / 3);
            g.fillOval(size / 2 - size / 16, size / 3, size / 8, size / 8);
            g.drawLine(size / 2, size / 2, size / 2, size * 2 / 3);
        }
    },
    CLOCK {
        @Override public void draw(Graphics2D g, int size, Color color) {
            g.setColor(color);
            g.setStroke(new BasicStroke(Math.max(1f, size / 10f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawOval(size / 6, size / 6, size * 2 / 3, size * 2 / 3);
            g.drawLine(size / 2, size / 3, size / 2, size / 2);
            g.drawLine(size / 2, size / 2, size * 2 / 3, size * 3 / 5);
        }
    },
    NAV_HOME {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.HOME); }
    },
    NAV_DOCUMENT {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.DOCUMENT); }
    },
    NAV_REPORT {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.ALERT_CIRCLE); }
    },
    NAV_GUIDE {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.BOOK); }
    },
    NAV_PROFILE {
        @Override public void draw(Graphics2D g, int size, Color color) { drawVector(g, size, color, HomeComponents.VectorIcon.Type.USER); }
    };

    public abstract void draw(Graphics2D g, int size, Color color);

    private static void drawVector(Graphics2D g, int size, Color color, HomeComponents.VectorIcon.Type type) {
        new HomeComponents.VectorIcon(type, size, color).paintIcon(null, g, 0, 0);
    }

    private static void drawReportIcon(Graphics2D g, int size, Color color, ReportsComponents.ReportIcon.Type type) {
        new ReportsComponents.ReportIcon(type, size, color).paintIcon(null, g, 0, 0);
    }

    private static void drawTransportIcon(Graphics2D g, int size, Color color, TransportComponents.TransportIcon.Type type) {
        new TransportComponents.TransportIcon(type, size, color).paintIcon(null, g, 0, 0);
    }

    private static void drawDirectoryIcon(Graphics2D g, int size, Color color, DirectoryComponents.DirectoryIcon.Type type) {
        new DirectoryComponents.DirectoryIcon(type, size, color).paintIcon(null, g, 0, 0);
    }

    public void draw(Graphics2D g, int x, int y, int size) {
        Graphics2D icon = (Graphics2D) g.create(x, y, size, size);
        draw(icon, size, g.getColor());
        icon.dispose();
    }
}
