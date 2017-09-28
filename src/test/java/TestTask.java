import com.ezconcurrent.Task;
import org.junit.Test;

/**
 * Created by sancheng on 9/28/2017.
 */
public class TestTask {

    @Test
    public void testTaskExec()  {
        Task task = new Task().join(new Task(()->{System.out.println("task1");})).join(new Task(()->{System.out.println("task2");})).join(new Task(()->{System.out.println("task3");}));
        task.exec();
    }
}
