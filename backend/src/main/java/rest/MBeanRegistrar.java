//package rest;
//
//import mbeans.ClickStats;
//import mbeans.MissRatio;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.ejb.Singleton;
//import jakarta.ejb.Startup;
//import javax.management.MBeanServer;
//import javax.management.NotificationListener;
//import javax.management.ObjectName;
//import java.lang.management.ManagementFactory;
//
//@Startup
//@Singleton
//public class MBeanRegistrar {
//    private ClickStats clickStats;
//    private MissRatio missRatio;
//
//
//    private ObjectName clickStatsName;
//    @PostConstruct
//    public void init() {
//        try {
//            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
//
//            clickStats = new ClickStats();
//            missRatio = new MissRatio(clickStats);
//            clickStatsName = new ObjectName("java.mbeans:type=ClickStats");
//
//            mbs.registerMBean(clickStats, new ObjectName("java.mbeans:type=ClickStats"));
//            mbs.registerMBean(missRatio, new ObjectName("java.mbeans:type=MissRatio"));
//
//            NotificationListener listener = (notification, handback) -> {
//                System.out.println("Received notification: " + notification.getMessage());
//            };
//            mbs.addNotificationListener(clickStatsName, listener, null, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public ClickStats getClickStats() {
//        return clickStats;
//    }
//
//    public MissRatio getMissRatio() {
//        return missRatio;
//    }
//}
package rest;

import jakarta.annotation.PreDestroy;
import mbeans.ClickStats;
import mbeans.MissRatio;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

@Startup
@Singleton
public class MBeanRegistrar {
    private ClickStats clickStats;
    private MissRatio missRatio;
    private ObjectName clickStatsName;
    private ObjectName missRatioName;
    private MBeanServer mbs;

    @PostConstruct
    public void init() {
        try {
            mbs = ManagementFactory.getPlatformMBeanServer();

            clickStatsName = new ObjectName("java.mbeans:type=ClickStats");
            missRatioName = new ObjectName("java.mbeans:type=MissRatio");

            // Удаляем старые бины если они есть
            if (mbs.isRegistered(clickStatsName)) {
                mbs.unregisterMBean(clickStatsName);
            }
            if (mbs.isRegistered(missRatioName)) {
                mbs.unregisterMBean(missRatioName);
            }

            clickStats = new ClickStats();
            missRatio = new MissRatio(clickStats);

            mbs.registerMBean(clickStats, clickStatsName);
            mbs.registerMBean(missRatio, missRatioName);

            System.out.println("MBeans registered successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mbs != null) {
                if (mbs.isRegistered(clickStatsName)) {
                    mbs.unregisterMBean(clickStatsName);
                }
                if (mbs.isRegistered(missRatioName)) {
                    mbs.unregisterMBean(missRatioName);
                }
                System.out.println("MBeans unregistered successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ClickStats getClickStats() {
        return clickStats;
    }

    public MissRatio getMissRatio() {
        return missRatio;
    }
}
