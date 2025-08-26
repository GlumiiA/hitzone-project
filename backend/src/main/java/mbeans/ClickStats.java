package mbeans;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import java.util.concurrent.atomic.AtomicLong;


public class ClickStats extends NotificationBroadcasterSupport implements ClickStatsMBean {
    private int totalClicks = 0;
    private int misses = 0;
    private final AtomicLong sequenceNumber = new AtomicLong(1);

    public ClickStats() {
        this.notificationInfo = new MBeanNotificationInfo[] {
                new MBeanNotificationInfo(
                        new String[]{"clicks.multipleOfTen"},
                        Notification.class.getName(),
                        "Notification when click count reaches multiple of 10"
                )
        };
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return this.notificationInfo;
    }

    @Override
    public synchronized void addClick(boolean isHit) {
        totalClicks++;
        if (!isHit) {
            misses++;
        }

        if (totalClicks % 10 == 0 && totalClicks != 0) {
            Notification notification = new Notification(
                    "clicks.multipleOfTen",
                    this,
                    sequenceNumber.getAndIncrement(),
                    System.currentTimeMillis(),
                    "Пользователь установил " + totalClicks + " точек"
            );
            sendNotification(notification);
        }
    }

    @Override
    public int getTotalClicks() {
        return totalClicks;
    }

    @Override
    public int getMisses() {
        return misses;
    }

    private final MBeanNotificationInfo[] notificationInfo;
}