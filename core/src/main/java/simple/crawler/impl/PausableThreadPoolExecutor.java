/*
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package simple.crawler.impl;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class PausableThreadPoolExecutor extends ScheduledThreadPoolExecutor
{
   private boolean isPaused;
   
   private ReentrantLock pauseLock = new ReentrantLock();
   
   private Condition unpaused = pauseLock.newCondition();
   
   public PausableThreadPoolExecutor(int corePoolSize)
   {
      super(corePoolSize);
   }
   
   @Override
   protected void beforeExecute(Thread t, Runnable r) {
      super.beforeExecute(t, r);
      pauseLock.lock();
      try 
      {
         while(isPaused)
         {
            unpaused.await();
         }
      } 
      catch(InterruptedException e) 
      {
         t.interrupt();
      }
      finally
      {
         pauseLock.unlock();
      }
   }
   
   public void pause() {
      pauseLock.lock();
      try {
         isPaused = true;
      } finally {
         pauseLock.unlock();
      }
   }
   
   public void resume() {
      pauseLock.lock();
      try {
         isPaused = false;
         unpaused.signalAll();
      } finally {
         pauseLock.unlock();
      }
   }
}
