
package studentpal.model.connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.codec.AsCodecFactory;
import studentpal.model.task.TaskDefinition;
import studentpal.model.task.TaskFactory;

public class PhoneConnection {
  private final static Logger logger = LoggerFactory.getLogger(PhoneConnection.class);
  
  private IoSession ioSession;
  private String boundPhoneNo;
  
  private HashMap<Integer, TaskDefinition> taskMap;
  private Timer schduleTaskTimer;
  private int gTaskSeq = 0;
  
  public final static String ATTR_TAGNAME = "PhoneConnection";
  
  public PhoneConnection(IoSession session, String boundPhoneNo) {
    this.ioSession = session;
    this.boundPhoneNo = boundPhoneNo;
    
    gTaskSeq = 0;
    taskMap = new HashMap<Integer, TaskDefinition>();
  }
  
//  public Integer getNextTaskId() {
//    taskId++;
//    return new Integer(taskId);
//  }
  
  protected synchronized void addTaskDef(TaskDefinition task) {
    gTaskSeq++;
    task.setTaskId(gTaskSeq);
    task.setConnection(this);
    
    taskMap.put(Integer.valueOf(gTaskSeq), task);
  }
  
  protected synchronized void removeTaskDef(TaskDefinition task) {
    if (task != null) {
      removeTaskDef(task.getTaskId());
      task = null;
    }
  }
  
  protected synchronized void removeTaskDef(int taskId) {
    if (taskMap.containsKey(taskId)) {
      taskMap.remove(taskId);
    }
  }
  
  public synchronized TaskDefinition retrieveTaskDef(int taskId) {
    logger.info("Retrieving Task of ID "+taskId+ " for mobileNo: "+boundPhoneNo);
    
    TaskDefinition result = null;
    if (taskMap.containsKey(taskId)) {
      result = taskMap.get(taskId);
    }
    return result;
  }
  
  public synchronized void addScheduledTask(TimerTask task, long delay) {
    if (schduleTaskTimer == null) {
      schduleTaskTimer = new Timer();  
    }
    schduleTaskTimer.schedule(task, delay);
  }

  public void executeTaskDef(TaskDefinition task) {
    if (task == null) return;
    
    try {
      addTaskDef(task);

      String requestStr = task.getRequestStr();
      sendMessage(requestStr);

      /*
       * Add this task to a schedule timeout queue, this task will not return
       * until response arrived or timeout happens
       */
      if (true) {
        addScheduledTask(task.getTimeoutTask(), TaskDefinition.TIMEOUT_SECONDS);
        task.waitForReply();
      }

    } catch (Exception ex) {
      logger.warn(ex.toString());
      finishTaskDef(task);
    }
  }
  
  public void finishTaskDef(TaskDefinition task) {
    if (task != null) {
      removeTaskDef(task);
      task.finish();
      task = null;
    }
  }
  
  public String getBoundPhoneNo() {
    return boundPhoneNo;
  }

  public void setBoundPhoneNo(String boundPhoneNo) {
    this.boundPhoneNo = boundPhoneNo;
  }
  
  public void sendMessage(String msg) {
    boolean debug = true;
    if (msg==null || msg.isEmpty()) return;
    if (!debug) {
      //encode msg via ResponseMsgEncoder before sending
      //will add msg length header at the most front
      ioSession.write(msg);  
    } else {
      try {
        sendMessage(msg.getBytes(AsCodecFactory.CHARSET_NAME));
      } catch (UnsupportedEncodingException e) {
        logger.warn(e.toString());
      } catch (IOException e) {
        logger.warn(e.toString());
      }
    }
  }
  
  private void sendMessage(byte[] msgbuff) throws IOException {
    IoBuffer iobuff = IoBuffer.allocate(1024);
    iobuff.setAutoExpand(true);
    iobuff.clear();
    iobuff.put(msgbuff);
    iobuff.shrink();
    iobuff.flip();

    ioSession.write(iobuff);
  }

}
