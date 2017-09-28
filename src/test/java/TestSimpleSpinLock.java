import com.ezconcurrent.SimpleSpinLock;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by sancheng on 9/28/2017.
 */
public class TestSimpleSpinLock {

    @Test
    public void testSpinLock()  {
        final SimpleSpinLock lock = new SimpleSpinLock();
        final StringBuffer sb = new StringBuffer();

        Thread t1 = new Thread() {
            public void run()  {
                lock.lock();
                sb.append("1");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                sb.append("1");
                lock.release();
            }
        };

        Thread t2= new Thread() {
            public void run()  {
                lock.lock();
                sb.append("2");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                sb.append("2");

                lock.release();
            }
        };

        t1.start();
        t2.start();
        //assert sequential consistency

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        Assert.assertTrue(sb.toString().equals("1122") || sb.toString().equals("2211"));

    }
}
