package studentpal.engine;

import java.util.Vector;

import studentpal.model.message.Message;

public class EvntPool {

  static final int MAX_EVNT_NUM = 15;
  static Vector<Message> events;
  static {
    events = new Vector<Message>(MAX_EVNT_NUM);
    try {
      for (int i = 0; i < MAX_EVNT_NUM; i++) {
        events.addElement(new Message());
      }
    } catch (Exception e) {
      e.printStackTrace();
      // if ( Log.debug ) Log.message("ObjPool::Constructor - " +
      // e.getMessage());
    }
  }

  // get a free event
  public static Message get() {
    synchronized (events) {
      for (int i = 0; i < MAX_EVNT_NUM; i++) {
        Object obj = events.elementAt(i);
        if (obj != null) {
          // if ( Log.debug ) Log.message("EvntPool::get - idx: " + i +
          // "\tobj: " + obj);
          events.setElementAt(null, i);
          return (Message) obj;
        }
      }
      return null;
    }
  }

  // put back an event
  public static void put(Message evnt) {
    synchronized (events) {
      for (int i = 0; i < MAX_EVNT_NUM; i++) {
        Object tmp = events.elementAt(i);
        if (tmp == null) {
          // if ( Log.debug ) Log.message("EvntPool::put - idx: " + i +
          // "\tobj: " + evnt);
          events.setElementAt(evnt, i);
          break;
        }
      }
    }
  }
}
