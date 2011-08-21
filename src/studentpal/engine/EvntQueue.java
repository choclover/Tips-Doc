package studentpal.engine;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import studentpal.model.message.Message;

public class EvntQueue {
  private final static Logger logger = LoggerFactory.getLogger(EvntQueue.class);

  static final int MAX_EVNT_NUM = 10;
  static Vector<Message> events = new Vector<Message>(MAX_EVNT_NUM);
  static {
    for (int i = 0; i < MAX_EVNT_NUM; i++) {
      events.addElement(null);
    }
  }
  static int header = -1;
  static int tail = -1;

  static boolean empty = true;
  static boolean full = false;

  static Object lock = new Object();

  public static void put(Message evnt) {
    synchronized (lock) {
      int idx = header + 1;
      if (idx == MAX_EVNT_NUM) {
        // loop back when reaching end
        idx = 0;
      }
      if (idx == tail) {
        if (!full) {
          // clear empty flag
          // empty = false;

          // set full flag
          full = true;

        } else {
          // queue is full, drop the event
          return;
        }
      }

      logger.info("EvntQueue::put - idx: " + idx + "\tobj: " + evnt);
      events.setElementAt(evnt, idx);
      empty = false; // hemerr

      header = idx;

      // if (tail == -1) {
      // tail = idx;
      // }
      // lock.notify();
    }
  }

  public static Message get() throws InterruptedException {
    synchronized (lock) {
      if (empty) {
        // if ( Log.debug )
        // Log.message("EvntQueue::get - Queue empty, wait ...");
        // lock.wait();

        return null;
      }

      // int idx = tail+1; //hemerr
      // //tail++;
      // if (tail != header) {
      // tail++; //hemerr
      // }
      // if (tail == MAX_EVNT_NUM) {
      // tail = 0;
      // }

      tail = (tail + 1) % MAX_EVNT_NUM;
      if (tail == header) {
        empty = true;
      }

      int idx = tail;

      logger.info("EvntQueue::get - idx: " + idx + "\tobj: "
          + events.elementAt(idx));
      full = false; // hemerr
      return (Message) events.elementAt(idx);
    }
  }

  public static boolean isEmpty() {
    synchronized (lock) {
      // if (!empty)
      // Log.message("EvntQueue NOT empty!");

      return empty;
    }
  }
}
