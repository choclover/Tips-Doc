package studentpal.model.task;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.message.Message;


public class TaskFactory {
  final static Logger logger = LoggerFactory.getLogger(TaskFactory.class);
  
  private static TaskFactory instance = null;
  
  public static TaskFactory getInstance() {
    if (instance == null) {
      instance = new TaskFactory();
    }
    return instance;
  }
  
  public TaskDefinition createTask(String taskName) throws JSONException {
    return createTask(taskName, null);
  }
  
  public TaskDefinition createTask(String taskName, String param) throws JSONException {
    TaskDefinition task = null;
    if (taskName.equals(Message.TASKNAME_GetAppList)) {
      task = new GetAppListTask();
      
    } else if (taskName.equals(Message.TASKNAME_SetAppAccessCategory)) {
      task = new SetAppAccessCategoryTask();
    }
    
    if (task != null) {
      task.setTaskParams(param);
    }
    
    return task;
  }
  
  
}
